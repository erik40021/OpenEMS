package io.openems.edge.controller.fid.fidvl;

import io.openems.edge.ess.api.ManagedSymmetricEss;

public class LimitFinder {
	
	/**
	 * builds ChargingInstruction consisting of limit and active charging power to be applied
	 * in order to shave feed-in peaks
	 * @return
	 */
	public ChargingInstruction buildChargingInstruction(int[] load_f, int[] pvgen_f, int pv_current, int load_current, ManagedSymmetricEss ess) {
		
		// power difference in Wh (surplus)
		int[] prelim_surplus = new int[pvgen_f.length + 1];
		for(int i=0; i<prelim_surplus.length; i++) prelim_surplus[i] = 0;
		
		prelim_surplus[0] = pv_current - load_current;
		int max_single_surplus = prelim_surplus[0];
		boolean day_reached = false;
		
		//calculate surplus in time span of prevailing daylight
		for(int i=0; i<pvgen_f.length; i++) {
			prelim_surplus[i+1] = pvgen_f[i] - load_f[i];
			if(pvgen_f[i] > 0) day_reached = true;
			if(day_reached && pvgen_f[i] <= 0) break; //night started, the negatives of it cannot be taken into volume calculation
			if(prelim_surplus[i+1] > max_single_surplus) max_single_surplus = prelim_surplus[i+1];
		}
		//additionally cut last entries of negatives to better estimate volume of surplus of the day
		int i = prelim_surplus.length - 1; 
		while(i >= 1 && prelim_surplus[i] <= 0) {
			prelim_surplus[i] = 0;
			i--;
		}
		int[] surplus = new int[i+1];

		for(int j=0; j<=i; j++) {
			surplus[j] = prelim_surplus[j];
		}
		
		//load current battery conditions of current SoC and allowed charging and discharging
		int allowed_charge = ess.getAllowedCharge().getNextValue().get();
		int allowed_discharge = ess.getAllowedDischarge().getNextValue().get();
		int max_power = ess.getMaxApparentPower().getNextValue().get();
		
		int battery_capacity = ess.getCapacity().getNextValue().get(); //(in Wh)
		int soc = this.getSoc(ess.getSoc().getNextValue().get(), battery_capacity,
				allowed_charge, allowed_discharge, max_power);
		
		float predicted_charging_vol = 0; //(Wh)
		int virtual_boundary = max_single_surplus; //virtual boundary of feed-in power per timestamp
		
		while((predicted_charging_vol + soc) < battery_capacity && virtual_boundary > 0) {
			predicted_charging_vol = getChargingVol(virtual_boundary, surplus, allowed_charge, allowed_discharge);
			virtual_boundary--;
		}
		
		return new ChargingInstruction(getChargingPower(surplus[0], virtual_boundary, allowed_charge, allowed_discharge), virtual_boundary);
	}

	/**
	 * calculates SoC: If possible, relies on more precise values of allowed charge/discharge
	 * @return current SoC of connected ESS
	 */
	private int getSoc(int soc, int capacity, int allowed_charge, int allowed_discharge, int max_power) {
		int potential_soc = (int) (soc/100.0 * capacity);
		
		if(potential_soc < max_power) return allowed_discharge;
		
		if(potential_soc > capacity - max_power) return capacity + allowed_charge;
		
		return potential_soc;
	}

	/**
	 * calculates potential charging volume for current virtual limit and allowed charging/discharging
	 * @return potential charging volume
	 */
	private static int getChargingVol(int virtual_boundary, int[] surplus_f, int allowed_charge, int allowed_discharge) {
		int allowed_charge_absolute = allowed_charge * -1;
		int total_vol = 0;
		for(int v: surplus_f) {
			int charging_amount;
			//battery cannot be charged with more than MAX_CHARGING_POWER and less than MAX_DECHARGING_POWER at a time
			if(v >= 0) charging_amount = Math.min(Math.max(0, v - virtual_boundary), allowed_charge_absolute);
			
			else charging_amount = Math.max(v, allowed_discharge * -1);
			
			total_vol += charging_amount;
		}
		return total_vol;
	}
	
	/**
	 * calculates charging (or discharging) power for the final virtual limit
	 * @return final charging power
	 */
	private static int getChargingPower(int surplus, int virtual_boundary, int allowed_charge, int allowed_discharge) {
		int charging_power;
		int prelim_charge_discharge = surplus * -1;
		if(surplus >= 0) {
			charging_power = Math.max(Math.min(prelim_charge_discharge + virtual_boundary, 0), allowed_charge);
		}
		else {
			charging_power = Math.min(prelim_charge_discharge, allowed_discharge);
		}
		return charging_power;
	}
}
