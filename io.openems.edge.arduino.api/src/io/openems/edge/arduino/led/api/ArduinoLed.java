package io.openems.edge.arduino.led.api;

import org.osgi.annotation.versioning.ProviderType;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

@ProviderType
public interface ArduinoLed extends OpenemsComponent{
	
	public enum ChannelId implements io.openems.edge.common.channel.ChannelId{
		/**
		 * Active voltage on arduino serial port.
		 * 
		 * <ul>
		 * <li>Interface: Arduino LED
		 * <li>Type: Integer
		 * <li>Unit: mV
		 * </ul>
		 */
		VOLTAGE(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLIVOLT)), //
		/**
		 * Turn LED light on or off. True: Turn on, False: Turn off
		 * 
		 * <ul>
		 * <li>Interface: Arduino LED
		 * <li>Type: Boolean
		 * </ul>
		 */
		SET_LED(Doc.of(OpenemsType.BOOLEAN) 
				.accessMode(AccessMode.WRITE_ONLY)
				.text("Turn LED light on or off")) //.onInit necessary?
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
	
	public abstract int getVoltageThreshhold();
	
	public abstract void applyLedChange(boolean turn_led_on);
	
	default Channel<Integer> getVoltage() {
		return this.channel(ChannelId.VOLTAGE);
	}
	
	default WriteChannel<Boolean> getSetActivePowerEquals() {
		return this.channel(ChannelId.SET_LED);
	}
	
}
