package io.openems.edge.controller.fid.fidvl;

public class ChargingInstruction {

	
	private int charging_power;
	private int virtual_limit;
	
	public ChargingInstruction(int charging_power, int virtual_limit) {
		this.charging_power = charging_power;
		this.virtual_limit = virtual_limit;
	}
	
	public int getChargingPower() {
		return this.charging_power;
	}
	
	public int getVirtualLimit() {
		return this.virtual_limit;
	}
}
