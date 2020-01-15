package io.openems.edge.predictor.loadforecast;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Predictor Load Forecast", //
		description = "Predicts the eletrical load relying on different python models connected via the REST api")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "forecast0";
	
	@AttributeDefinition(name = "Database-ID", description = "Unique ID of the database that this predictor relies on")
	String database_id() default "influx0";
	
	@AttributeDefinition(name = "Model", description = "Name of requested forecasting model")
	String model() default "dnn";
	
	@AttributeDefinition(name = "Time", description = "Current time in time frame of 2014 - 2016 in format dd-MM-yyyy HH:mm:ss")
	String time() default "21-06-2015 12:30:30";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	String webconsole_configurationFactory_nameHint() default "Predictor Load Forecast [{id}]";

	
}