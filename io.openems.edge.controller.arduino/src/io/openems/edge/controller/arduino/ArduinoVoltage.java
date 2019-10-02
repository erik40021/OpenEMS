package io.openems.edge.controller.arduino;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.arduino.led.api.ArduinoLed;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.controller.api.Controller;
import org.osgi.service.metatype.annotations.Designate;


@Designate( ocd=Config.class, factory=true)
@Component(name="Arduino Voltage Controller", immediate = true, //
configurationPolicy = ConfigurationPolicy.REQUIRE )
public class ArduinoVoltage extends AbstractOpenemsComponent implements OpenemsComponent, Controller {

	private Config config;
	
	@Reference
	protected ComponentManager componentManager;

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
	

	public ArduinoVoltage() {
		super(OpenemsComponent.ChannelId.values(), Controller.ChannelId.values());
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {
		boolean turn_led_on = false;
		ArduinoLed arduino = this.componentManager.getComponent(config.arduino_id());
		
		Integer active_voltage = arduino.getVoltage().value().get();
		int voltage_threshhold = arduino.getVoltageThreshhold();
		
		if(active_voltage == null) {
			//TODO: debug log something
		}
		else if(active_voltage >=  voltage_threshhold) turn_led_on = true;
		 
		//request device to apply the calculated value (through updating the respective channel but that's not our business here ..)
		arduino.applyLedChange(turn_led_on);
	}

}
