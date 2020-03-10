package io.openems.edge.controller.fid.fidvl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "Controller FID-VL", //
		description = "Handles a given feed-in limitation based on PV-production and load forecasts.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlFidVl0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "Feed-In Limit", description = "Limit of Feed-In that is to be satisfied (proportional to kWp).")
	float feedin_limit() default 0.5f;
	
	@AttributeDefinition(name = "Length of forecast horizon", description = "Length of the considered forecast horizon.")
	int forecast_horizon_length() default 15;
	
	@AttributeDefinition(name = "Ess-ID", description = "ID of Ess that is to be managed.")
	String ess_Id() default "ess0";
	
	@AttributeDefinition(name = "Meter-ID", description = "ID of meter that is to be managed.")
	String meter_Id() default "meter0";
	
	@AttributeDefinition(name = "PV-Inverter-ID", description = "ID of pv-inverter that is to be managed.")
	String pv_inverter_Id() default "pvInverter0";
	
	@AttributeDefinition(name = "Load-Forecast-ID", description = "ID of Load Foreacast.")
	String load_forecast_Id() default "loadForecast0";
	
	@AttributeDefinition(name = "PV-Production-Forecast-ID", description = "ID of PV-production Forecast.")
	String pvproduction_forecast_Id() default "pvProductionForecast0";
	
	//for simulation:
	@AttributeDefinition(name = "Database-ID", description = "Unique ID of the database this controller needs to read out to get simulated load values")
	String database_id() default "influx0";
	
	@AttributeDefinition(name = "Simulation start time", description = "Start time of simulation in time frame of 2014 - 2016 and format uuuu-MM-dd'T'HH:mm")
	String simulation_start_time() default "2015-08-19T12:30";
	
	@AttributeDefinition(name = "Simple Strategy", description = "True uses simple feed-in delimiting without forecasts")
	boolean simpleStrategy() default false;
	
	@AttributeDefinition(name = "Timeout", description = "Timeout in number of timesteps after which simulation finishes (e.g 30*24 = 720)")
	int timeout() default 720;
	

	String webconsole_configurationFactory_nameHint() default "Controller Feed-In Delimiter PvProg [{id}]";
}
