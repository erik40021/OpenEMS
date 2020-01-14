package io.openems.edge.simulator.weatherforecast;

import java.io.IOException;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.simulator.datasource.api.SimulatorDatasource;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Simulator.Weatherforecast.Temperature", //
		immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE)
public class WeatherforecastTemperature extends AbstractOpenemsComponent
		implements OpenemsComponent, EventHandler {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		SIMULATED_TEMPERATURE(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT)),
		SIMULATED_PRECIPITATION_PROBABILITY(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT)),
		SIMULATED_APPARENT_TEMPERATURE(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT)),
		SIMULATED_VISIBILITY(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT)),
		SIMULATED_CLOUD_COVER(Doc.of(OpenemsType.FLOAT) //
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
	protected ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected SimulatorDatasource datasource;

	@Activate
	void activate(ComponentContext context, Config config) throws IOException {
		super.activate(context, config.id(), config.alias(), config.enabled());

		// update filter for 'datasource'
		if (OpenemsComponent.updateReferenceFilter(cm, this.servicePid(), "datasource", config.datasource_id())) {
			return;
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	public WeatherforecastTemperature() {
		super(OpenemsComponent.ChannelId.values(), ChannelId.values());
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.updateChannels();
			break;
		}
	}

	private void updateChannels() {
		
		float simulatedTemp = this.datasource.getValue(OpenemsType.FLOAT, "Temp");
		float simulatedPrec = this.datasource.getValue(OpenemsType.FLOAT, "Prec");
		float simulatedAppTemp = this.datasource.getValue(OpenemsType.FLOAT, "AppTemp");
		float simulatedVisib = this.datasource.getValue(OpenemsType.FLOAT, "Visib");
		float simulatedCloud= this.datasource.getValue(OpenemsType.FLOAT, "Cloud");

		this.channel(ChannelId.SIMULATED_TEMPERATURE).setNextValue(simulatedTemp);
		this.channel(ChannelId.SIMULATED_PRECIPITATION_PROBABILITY).setNextValue(simulatedPrec);
		this.channel(ChannelId.SIMULATED_APPARENT_TEMPERATURE).setNextValue(simulatedAppTemp);
		this.channel(ChannelId.SIMULATED_VISIBILITY).setNextValue(simulatedVisib);
		this.channel(ChannelId.SIMULATED_CLOUD_COVER).setNextValue(simulatedCloud);
	}

	@Override
	public String debugLog() {
		return this.channel(ChannelId.SIMULATED_TEMPERATURE).value().asString();
	}
}
