package io.openems.edge.controller.fid.pvprog;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "Controller Feed-In-Delimiter PvProg", //
		description = "Handles a given feed-in-limitation based on PV-production and load forecasts.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlFidPvProg0";
	
	@AttributeDefinition(name = "Load-Forecast-ID", description = "ID of Load Foreacast.")
	String load_forecast_Id() default "predictorLoadForecast0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Battery-ID", description = "ID of Battery that is to be managed.")
	String battery_Id() default "bms0";

	String webconsole_configurationFactory_nameHint() default "Controller Feed-In-Delimiter PvProg [{id}]";
}