package io.openems.edge.controller.fid.pvprog;

import io.openems.edge.predictor.api.HourlyPrediction;

public class ChargingScheduleBuilder {
	
	
	private static final int dt=60; 			// Zeitschrittweite in s
	private static final float eta_batt=0.95f; 	// Wirkungsgrad des Lithium-Batteriespeichers (ohne AC/DC-Wandlung)
	private static final float eta_bwr=0.94f; 	// Wirkungsgrad des Batteriewechselrichters
	private static final int tf_past=3; 		// Rückblick-Zeitfenster der PV-Prognose in h 
	private static final int tf_prog=15; 		// Prognosehorizont der PV- und Lastprognose in h
	
	private HourlyPrediction consumption;
	private HourlyPrediction production;
	
	public ChargingScheduleBuilder(HourlyPrediction consumption, HourlyPrediction production) {
		this.consumption = consumption;
		this.production = production;
	}
	
//	public void build_schedule() {
//		// PV-Leistung in W
//		P_pv=p_pv*P_stc*1000; 
//
//		// PV-Leistungsprognose in W
//		P_pvf=p_pvf*P_stc*1000; 
//
//		// Differenzleistung
//		P_d=P_pv-P_ld;
//
//		// Prognose der Differenzleistung
//		P_df=P_pvf-P_ldf;
//
//		// Prognose der Batterieleistung (Startwert)
//		P_bf = 0;
//
//		// Prognose der Differenzleistung im aktuellen Zeitschritt (Startwert)
//		P_dfsel = 0;
//
//		// Batterieladeplanung, Ausregelung von Prognosefehlern sowie 
//		// Batteriesimulation nur durchführen, wenn die Speicherkapazität größer null ist
//		if C_bu>0
//	}
}
