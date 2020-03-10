package io.openems.edge.controller.fid.fidvl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.meter.api.SymmetricMeter;
import io.openems.edge.predictor.api.ConsumptionHourlyPredictor;
import io.openems.edge.predictor.api.HourlyPrediction;
import io.openems.edge.predictor.api.ProductionHourlyPredictor;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.fid.FID-VL", 
		immediate = true, 
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class FidVl extends AbstractOpenemsComponent implements Controller, OpenemsComponent{
	
	/**Feed-In Delimiter with Virtual Limit*/
	
	
	//simulation constants
	
	private static final float REAL_ANNUAL_LOAD_MWH = 0.612f * (365/84);
	private static final float REAL_KWP = 12.0f;
	
	private static final int SIMULATED_ANNUAL_LOAD_MWH = 4;
	private static final int SIMULATED_KWP = 6;
	
	private static final float SCALING_FACTOR_LOAD = 1/REAL_ANNUAL_LOAD_MWH*SIMULATED_ANNUAL_LOAD_MWH;
	private static final float SCALING_FACTOR_PV = 1/REAL_KWP*6;
	
	private static final String SIMULATION_DRAW_FILE_PATH = "/home/de424/Documents/BA-Erik/SimulationResults/SimulationDraw-";
	private static final String SIMULATION_CURTAIL_FILE_PATH = "/home/de424/Documents/BA-Erik/SimulationResults/SimulationCurtail-";
	
//	public static final float ETA_BATTERY=0.95f; 	// Wirkungsgrad des Lithium-Batteriespeichers (ohne AC/DC-Wandlung)
//	public static final float ETA_INVERTER=0.94f; 	// Wirkungsgrad des Batteriewechselrichters
	
	private String ess_id;
	private String meter_id;
	private String pv_inverter_id;
	private String load_forecast_id;
	private String pv_production_forecast_id;
	
	private float feedin_limit_share;
	private int max_feedin_power;	//in [-n, -1], n > 1
	private int forecast_horizon_length;

	private LimitFinder limit_finder;
	
	//devices
	private ManagedSymmetricEss ess;
	private SymmetricMeter meter;
	private ManagedSymmetricPvInverter pv_inverter;
	
	//forecasts
	private ConsumptionHourlyPredictor consumption_forecaster;
	private ProductionHourlyPredictor production_forecaster;
	
	//simulation attributes
	private boolean simpleStrategy;
	private String simulation_start_time;
	private int timeout;
	private int current_time_step = 0;
	
	
	private final Logger log = LoggerFactory.getLogger(FidVl.class);

	@Reference
	protected ComponentManager componentManager;
	

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		//simulation properties
		SIMULATED_CURRENT_TIME(Doc.of(OpenemsType.STRING)),
		
		SIMULATION_SCALING_FACTOR_PV(Doc.of(OpenemsType.FLOAT)),
		
		SIMULATION_SCALING_FACTOR_LOAD(Doc.of(OpenemsType.FLOAT));
		
		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}
	
	public String getSimulatedCurrentTime() {
		return this.channel(ChannelId.SIMULATED_CURRENT_TIME).getNextValue().get().toString();
	}

	public FidVl() {
		super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values(), ChannelId.values());
		this.limit_finder = new LimitFinder();
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());

		//devices:
		this.ess_id = config.ess_Id();
		this.meter_id = config.meter_Id();
		this.pv_inverter_id = config.pv_inverter_Id();
		
		//forecasts:
		this.load_forecast_id = config.load_forecast_Id();
		this.pv_production_forecast_id = config.pvproduction_forecast_Id();
		
		//algorithm attributes
		this.feedin_limit_share = config.feedin_limit();
		this.max_feedin_power = (int) (SIMULATED_KWP*1000*this.feedin_limit_share)*-1;
		this.forecast_horizon_length = config.forecast_horizon_length();
		
		//simulation attributes
		this.simpleStrategy = config.simpleStrategy();
		this.simulation_start_time = config.simulation_start_time();
		this.timeout = config.timeout();
		this.channel(ChannelId.SIMULATED_CURRENT_TIME).setNextValue(this.simulation_start_time);
		this.channel(ChannelId.SIMULATION_SCALING_FACTOR_PV).setNextValue(SCALING_FACTOR_PV);
		this.channel(ChannelId.SIMULATION_SCALING_FACTOR_LOAD).setNextValue(SCALING_FACTOR_LOAD);
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}
	
	
	/**
	 * virtual limit operation strategy
	 * 
	 * @throws OpenemsNamedException
	 */
	public void fidVl(ManagedSymmetricEss ess, SymmetricMeter load_meter, ManagedSymmetricPvInverter pv_inverter, 
			int[] load, int[] pv, LocalDateTime start) throws OpenemsNamedException {
		int pv_current = (int) pv_inverter.channel("SimulatedActivePower").getNextValue().get();
		int load_current = (int) load_meter.channel("SimulatedActivePower").getNextValue().get();
		
		
		
		ChargingInstruction charging_instruction = this.limit_finder.buildChargingInstruction(load, pv, pv_current, load_current, ess);

		this.applyResults(charging_instruction.getChargingPower(), pv_current, load_current, charging_instruction.getVirtualLimit(), ess, pv_inverter);
	}
	
	/**
	 * simple operation strategy
	 */
	private void fidSimple(ManagedSymmetricEss ess, SymmetricMeter load_meter, ManagedSymmetricPvInverter pv_inverter) {
		int pv_current = (int) pv_inverter.channel("SimulatedActivePower").getNextValue().get();
		int load_current = (int) load_meter.channel("SimulatedActivePower").getNextValue().get();
		int surplus = pv_current - load_current;
		int charging_power;
		int prelim_charge_discharge = surplus * -1;
		
		if(surplus >= 0) {
			charging_power = Math.max(prelim_charge_discharge, ess.getAllowedCharge().getNextValue().get());
		}
		else {
			charging_power = Math.min(prelim_charge_discharge, ess.getAllowedDischarge().getNextValue().get());
		}
		
		this.applyResults(charging_power, pv_current, load_current, -1, ess, pv_inverter);
	}
	
	/**
	 * collects results and applies them to them to ess and PV
	 * 
	 * @param charging_power 		power that must be drawn from or charged into the ess
	 * @param pv_current 			current power of PV generation
	 * @param load_current			current power of load
	 * @param virtual_power_limit	calculated virtual limit to shave feed-ins
	 * @param ess					reference to energy storage system
	 * @param pv_inverter			reference to inverter of PV
	 */
	private void applyResults(int charging_power, int pv_current, int load_current, int virtual_power_limit, 
			ManagedSymmetricEss ess, ManagedSymmetricPvInverter pv_inverter) {
		int current_surplus = pv_current - load_current;
		
		int potential_grid_feedin = (current_surplus + charging_power) * -1;
		int actual_curtailment = 0;
		int actual_grid_feedin;
		if(potential_grid_feedin < this.max_feedin_power) {
			
			actual_grid_feedin = this.max_feedin_power;
			// curtailing or additional charging is needed -> always try to store energy before curtailing it, 
			// in case that there is less surplus than expected at the end!
			// -> Lazy Curtailment strategy:
			
			int possible_charge_left = ess.getAllowedCharge().getNextValue().get() - charging_power;
			
			int potential_curtailment = potential_grid_feedin - actual_grid_feedin;
			int additional_charging = Math.max(potential_curtailment, possible_charge_left);
			
			charging_power += additional_charging;
			
			actual_curtailment = potential_curtailment - additional_charging;
		}
		else actual_grid_feedin = potential_grid_feedin;
		
		this.log.info("[FID-VL results] active surplus/shortage: "+current_surplus+", VL: "+virtual_power_limit
				+ "\nfeedin/draw: "+actual_grid_feedin+", curtailment: "+actual_curtailment+", charging/discharging: "+charging_power);
		
		try {
			//curtail PV if curtail < 0
			pv_inverter.getActivePowerLimit().setNextWriteValue(pv_current + actual_curtailment); 
			
			//for simulation: store active curtailment power separately
			pv_inverter.channel("SimulatedActiveCurtailment").setNextValue(actual_curtailment);
			int active_draw = 0;
			if(actual_grid_feedin > 0) active_draw = actual_grid_feedin;
			this.writeSimulationDataPoints(active_draw, actual_curtailment);
			
			//inform battery about power to be charged (or discharged)
			ess.applyPower(charging_power, 0);
		} catch (OpenemsNamedException | IOException e) {
			e.printStackTrace();
			this.log.debug("Could not apply and store results");
			return;
		}
	}
	
	/**
	 * run method of controller - gets executed on every cycle to perform feed-in delimiting
	 */
	@Override
	public void run() throws OpenemsNamedException {
		if(this.current_time_step >= this.timeout) {
			return;
		}
		//check if component references have already been collected
		if(this.ess == null) { 
			this.ess = this.componentManager.getComponent(this.ess_id);
			this.meter = this.componentManager.getComponent(this.meter_id);
			this.pv_inverter = this.componentManager.getComponent(this.pv_inverter_id);
			this.consumption_forecaster = this.componentManager.getComponent(this.load_forecast_id);
			this.production_forecaster = this.componentManager.getComponent(this.pv_production_forecast_id);
		}
		if(this.simpleStrategy) {
			// apply simple fid without forecasts
			this.fidSimple(this.ess, this.meter, this.pv_inverter);
			// fid-vl was executed. Increment time step of simulation
			this.nextSimulationTimeStep();
			return;
		}
		HourlyPrediction consumption_forecast = this.consumption_forecaster.get24hPrediction();
		
		HourlyPrediction production_forecast = this.production_forecaster.get24hPrediction();
		
		if(production_forecast == null || consumption_forecast == null) {
			return;
		}
		int[] pvgen_f = convertToInt(production_forecast.getValues(), this.forecast_horizon_length);
		int[] load_f = convertToInt(consumption_forecast.getValues(), this.forecast_horizon_length);
		
		checkNotNull(load_f);
		if(!consumption_forecast.getStart().equals(production_forecast.getStart())) {
			this.log.debug("forecasts not synchronised - aborting handling of start hour ["+consumption_forecast.getStart().getHour()+"]");
			return;
		}
		this.fidVl(this.ess, this.meter, this.pv_inverter, load_f, pvgen_f, consumption_forecast.getStart());
		// fid-vl was executed. Increment time step of simulation
		this.nextSimulationTimeStep();
	}
	
	
	private void checkNotNull(int[] a) {
		for(int i=0; i< a.length; i++) {
			if(a[i] <= 0) {
				a[i] = 0;
			}
		}
	}
	
	private static int[] convertToInt(Integer[] a, int length) {
		int[] b = new int[length];
		for(int i=0; i<b.length; i++) b[i] = a[i];
		return b;
	}
	
	
	/**
	 * initiates next simulation time step. All connected simulation devices can synchronise on this time step
	 */
	private void nextSimulationTimeStep() {
		this.current_time_step++;
		System.out.println("[FID-SIMULATION] current simulation timestep: "+ this.current_time_step);
		
		String current_time = this.channel(ChannelId.SIMULATED_CURRENT_TIME).getNextValue().get().toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm");
		LocalDateTime current_local_date_time = LocalDateTime.parse(current_time, formatter);
		current_local_date_time = current_local_date_time.plusHours(1);
		
		this.channel(ChannelId.SIMULATED_CURRENT_TIME).setNextValue(current_local_date_time.toString());
	}
	
	/**
	 * writes data points of active draw and active curtailment amount to files as results of simulation
	 * @throws IOException
	 */
	private void writeSimulationDataPoints(int active_draw, int active_curtailment) throws IOException {
		File draw = new File(SIMULATION_DRAW_FILE_PATH + this.simulation_start_time + ".txt");
		File curtail = new File(SIMULATION_CURTAIL_FILE_PATH + this.simulation_start_time + ".txt");
		FileWriter fr_draw = new FileWriter(draw, true);
		FileWriter fr_curtail = new FileWriter(curtail, true);
		fr_draw.write(""+active_draw+"\n"); fr_draw.close();
		fr_curtail.write(""+active_curtailment+"\n"); fr_curtail.close();
	}
}