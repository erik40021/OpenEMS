package io.openems.edge.arduino.led.api;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface arduinoLed extends OpenemsComponent{
	
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
				.text("Turn LED light on or off"))
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
	
	default Channel<Integer> getAllowedDischarge() {
		return this.channel(ChannelId.VOLTAGE);
	}
	
	default WriteChannel<Boolean> getSetActivePowerEquals() {
		return this.channel(ChannelId.SET_LED);
	}
	
}
