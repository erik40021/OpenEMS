package io.openems.edge.controller.fid.pvprog;

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
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.predictor.api.ConsumptionHourlyPredictor;
import io.openems.edge.predictor.api.HourlyPrediction;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.fid.PvProg", 
		immediate = true, 
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PvProg extends AbstractOpenemsComponent implements Controller, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(PvProg.class);

	@Reference
	protected ComponentManager componentManager;

	private String battery_id;
	private String load_forecast_id;

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		;
		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public PvProg() {
		super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values(), ChannelId.values());
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());

		this.battery_id = config.battery_Id();
		this.load_forecast_id = config.load_forecast_Id();
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		//BatteryInterfaceType** battery = this.componentManager.getComponent(this.battery_id);
		ConsumptionHourlyPredictor forecaster = this.componentManager.getComponent(this.load_forecast_id);
		HourlyPrediction forecast = forecaster.get24hPrediction();
		System.out.println("24h load forecast for start time " + forecast.getStart().toString() + " :");
		for(int v: forecast.getValues()) {
			System.out.println(v);
		}
	}

}