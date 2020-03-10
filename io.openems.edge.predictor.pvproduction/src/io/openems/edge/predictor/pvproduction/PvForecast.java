package io.openems.edge.predictor.pvproduction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.predictor.api.HourlyPrediction;
import io.openems.edge.predictor.api.ProductionHourlyPredictor;
import io.openems.edge.timedata.api.Timedata;

@Designate(ocd = Config.class, factory = true)
@Component(
	name = "Predictor.Pvproduction.Forecast", //
	immediate = true, //
	configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PvForecast extends AbstractOpenemsComponent implements ProductionHourlyPredictor, OpenemsComponent {
	
	private String database_id;
	private String fidvl_id;
	
	private LocalDateTime simulated_start_time;
	private float simulation_scaling_factor = 0.0f;
	
	@Reference
	protected ComponentManager componentManager;

	private final Logger log = LoggerFactory.getLogger(PvForecast.class);
	
	public PvForecast() {
		super(OpenemsComponent.ChannelId.values(), //
				ForecastChannelId.values());
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		
		this.database_id = config.database_id();
		this.fidvl_id = config.fidvl_id();
	}
	
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	protected ComponentManager getComponentManager() {
		return this.componentManager;
	}
	

	@Override
	public HourlyPrediction get24hPrediction() {
		HourlyPrediction forecast;
		try {
			this.updateSimulatedCurrentTime();
			forecast = this.getForecast(this.simulated_start_time);
		} catch (IllegalArgumentException | OpenemsNamedException e) {
			e.printStackTrace();
			return null;
		}
		if(forecast == null) {
			this.log.debug("Unable to provide forecast of PV-production");
			return null;
		}
		return forecast;
		
	}

	/**
	 * requests database to send future PV generation powers for given start time
	 * @throws OpenemsNamedException
	 */
	private HourlyPrediction getForecast(LocalDateTime start_time) throws OpenemsNamedException {
		Timedata db = this.componentManager.getComponent(this.database_id);
		int resolution = 3600;
		Set<ChannelAddress> channels = new TreeSet<>();
		ZonedDateTime fromDate = ZonedDateTime.of(start_time, ZoneId.of("UTC"));
		ZonedDateTime toDate = ZonedDateTime.of(start_time.plusHours(16), ZoneId.of("UTC"));
		ChannelAddress pv_production_channel_adress = ChannelAddress.fromString("pvProductionForecast0/PVGEN_FUTURE_SIMULATED");
		
		channels.add(pv_production_channel_adress);
		
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> response_map = 
				db.queryHistoricData(null, fromDate, toDate, channels, resolution);
		
		response_map.remove(fromDate);	//first queried element is always null: remove it
		Integer[] forecast = this.extractAsIntegerArray(response_map.values(), pv_production_channel_adress);
		if(this.simulation_scaling_factor == 0.0f) {
			try {
				this.simulation_scaling_factor = (float) this.componentManager.getComponent(this.fidvl_id).channel("SimulationScalingFactorPv").getNextValue().get();
			} catch (IllegalArgumentException | OpenemsNamedException e) {
				e.printStackTrace();
				return null;
			}
		}
		return new HourlyPrediction(scale(forecast, this.simulation_scaling_factor), start_time);
	}
	

	/**
	 * reads out simulated current time of FID-VL controller to synchronise with current simulation time step
	 * @throws IllegalArgumentException
	 * @throws OpenemsNamedException
	 */
	private void updateSimulatedCurrentTime() throws IllegalArgumentException, OpenemsNamedException {
		String time = this.componentManager.getComponent(this.fidvl_id).channel("SimulatedCurrentTime").getNextValue().get().toString();
		String cut_time = time.substring(0,13);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH");
		this.simulated_start_time = LocalDateTime.parse(cut_time, formatter);
	}
	
	
	private static Integer[] scale(Integer[] a, float factor) {
		Integer[] b = new Integer[a.length];
		for(int i=0; i<b.length; i++) {
			b[i] = (int) (a[i] * factor);
		}
		return b;
	}

	private Integer[] extractAsIntegerArray(Collection<SortedMap<ChannelAddress, JsonElement>> collection, ChannelAddress channel_address) 
			throws OpenemsNamedException {
		Integer[] a = new Integer[24]; int k = 0;
		for(SortedMap<ChannelAddress, JsonElement> map: collection) {
			a[k] = (int) (map.get(channel_address).getAsFloat()*1000.0f);	//pvgen in database is in kWh: convert to Wh
			k++;
		} return a;
	}
}
