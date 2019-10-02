package io.openems.edge.arduino.led;

import com.fazecast.jSerialComm.SerialPort;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.arduino.bridge.Worker;
import io.openems.edge.arduino.led.api.ArduinoLed;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;

@Designate(ocd = Config.class, factory = true)
@Component(name="Arduino Red LED", immediate = true, 
				configurationPolicy = ConfigurationPolicy.REQUIRE,
				property = { //
						EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE,
						EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE
				})
public class RedArduinoLed extends AbstractOpenemsComponent implements OpenemsComponent, ArduinoLed, EventHandler {

	private Worker worker = null;
	
	private int voltage_threshhold;
	
	
	public RedArduinoLed() {
		super(OpenemsComponent.ChannelId.values(), ArduinoLed.ChannelId.values());
		System.err.println("Instantiated RedArduinoLed Object");
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsNamedException{
		super.activate(context, config.id(), config.alias(), config.enabled());	
		this.voltage_threshhold = config.voltageThreshhold();
		if (config.enabled()) {
			this.worker = new Worker(this, SerialPort.getCommPort(config.portName()));
			System.err.println("initialized worker for port "+ config.portName());
			this.worker.activate(config.id());
			System.err.println("Activated worker for RedArduinoLed Instance");
		} 
	}

	@Deactivate
	protected void deactivate() {
		if (this.worker != null) {
			this.worker.deactivate();
		}
		super.deactivate();
	}
	
	@Override
	public void handleEvent(Event event) {
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			System.err.println("catched TOPIC_CYCLE_BEFORE_PROCESS_IMAGE event. Triggering next run");
			this.worker.triggerNextRun();
			break;
		case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE:
			System.err.println("catched TOPIC_CYCLE_EXECUTE_WRITE event. Triggering writing onto device");
			this.worker.onExecuteWrite();
			break;
		}
	}
	
	@Override
	public String debugLog() {
		return "Voltage:" + this.channel(ArduinoLed.ChannelId.VOLTAGE).value().asString();
	}
	

	@Override
	public void logError(Logger log, String message) {
		super.logError(log, message);
	}
	
	@Override
	public void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	public int getVoltageThreshhold() {
		return this.voltage_threshhold;
	}

	@Override
	public void applyLedChange(boolean turn_led_on) {
		this.channel(ArduinoLed.ChannelId.SET_LED).setNextValue(turn_led_on);
	}
	
}
