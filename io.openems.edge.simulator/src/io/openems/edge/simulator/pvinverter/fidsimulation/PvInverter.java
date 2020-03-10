package io.openems.edge.simulator.pvinverter.fidsimulation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import com.google.gson.JsonElement;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.MeterType;
import io.openems.edge.meter.api.SymmetricMeter;
import io.openems.edge.pvinverter.api.ManagedSymmetricPvInverter;
import io.openems.edge.timedata.api.Timedata;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Simulator.PvInverter.FIDsimulation", //
		immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
				"type=PRODUCTION" //
		})
public class PvInverter extends AbstractOpenemsComponent
		implements ManagedSymmetricPvInverter, SymmetricMeter, OpenemsComponent, EventHandler {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		SIMULATED_ACTIVE_POWER(Doc.of(OpenemsType.INTEGER) //displays active production power (mostly positive for production, negative for idle consumption)
				.unit(Unit.WATT)),
		
		SIMULATED_ACTIVE_CURTAILMENT(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.WATT));

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		public Doc doc() {
			return this.doc;
		}
	}

	@Reference
	protected ComponentManager componentManager;

	private String database_id;
	private String fidvl_id;

	private float simulation_scaling_factor = 0.0f;

	@Activate
	void activate(ComponentContext context, Config config) throws IOException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.database_id = config.database_id();
		this.fidvl_id = config.fidvl_id();
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	public PvInverter() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SymmetricMeter.ChannelId.values(), //
				ManagedSymmetricPvInverter.ChannelId.values(), //
				ChannelId.values() //
		);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.updateChannels();
			break;
		}
	}

	/**
	 * updates all channel of this device in every cycle
	 */
	private void updateChannels() {
		// copy write value to read value
		this.getActivePowerLimit().setNextValue(this.getActivePowerLimit().getNextWriteValueAndReset());

		/*
		 * get and store Simulated Active Power
		 */
		LocalDateTime current_time;
		int simulated_active_power;
		try {
			current_time = this.getSimulatedCurrentTime();
			simulated_active_power = this.queryActivePower(current_time);
		} catch (IllegalArgumentException | OpenemsNamedException e) {
			e.printStackTrace();
			return;
		}
		this.channel(ChannelId.SIMULATED_ACTIVE_POWER).setNextValue(simulated_active_power);

		// Apply Active Power Limit
		Optional<Integer> activePowerLimitOpt = this.getActivePowerLimit().value().asOptional();
		if (activePowerLimitOpt.isPresent()) {
			int activePowerLimit = activePowerLimitOpt.get();
			simulated_active_power = Math.min(simulated_active_power, activePowerLimit);
		}

		this.getActivePower().setNextValue(simulated_active_power);
	}

	/**
	 * queries database to send current active power in PV inverter
	 * @throws OpenemsNamedException
	 */
	private int queryActivePower(LocalDateTime current_time) throws OpenemsNamedException {
		Timedata db = this.componentManager.getComponent(this.database_id);
		int resolution = 3600;
		Set<ChannelAddress> channels = new TreeSet<>();
		ZonedDateTime fromDate = ZonedDateTime.of(current_time.minusSeconds(1), ZoneId.of("UTC"));
		ZonedDateTime toDate = ZonedDateTime.of(current_time.plusSeconds(1), ZoneId.of("UTC"));
		ChannelAddress load_channel_adress = ChannelAddress.fromString("pvProductionForecast0/PVGEN_FUTURE_SIMULATED");
		
		channels.add(load_channel_adress);
		
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> response_map = 
				db.queryHistoricData(null, fromDate, toDate, channels, resolution);
		
		response_map.remove(fromDate.plusSeconds(1).minusHours(1));	//first queried element is always null (for some reason): remove it
		
		if(this.simulation_scaling_factor == 0.0f) {
			this.simulation_scaling_factor = (float) this.componentManager.getComponent(this.fidvl_id).channel("SimulationScalingFactorPv").getNextValue().get();
		}
		
		Integer[] a = new Integer[1]; int k = 0;
		for(SortedMap<ChannelAddress, JsonElement> map: response_map.values()) {
			a[k] = (int) (map.get(load_channel_adress).getAsFloat()*1000.0f);	//pvgen in database is in kWh: convert to Wh
			k++;
		}
		return (int) (a[0] * this.simulation_scaling_factor);
	}
	
	/**
	 * reads out simulated current time of FID-VL controller to synchronise with current simulation time step
	 * @throws IllegalArgumentException
	 * @throws OpenemsNamedException
	 */
	private LocalDateTime getSimulatedCurrentTime() throws IllegalArgumentException, OpenemsNamedException {
		String time = this.componentManager.getComponent(this.fidvl_id).channel("SimulatedCurrentTime").getNextValue().get().toString();
		String cut_time = time.substring(0,13);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH");
		return LocalDateTime.parse(cut_time, formatter);
	}

	@Override
	public String debugLog() {
		return this.getActivePower().value().asString();
	}

	@Override
	public MeterType getMeterType() {
		return MeterType.PRODUCTION;
	}
}
