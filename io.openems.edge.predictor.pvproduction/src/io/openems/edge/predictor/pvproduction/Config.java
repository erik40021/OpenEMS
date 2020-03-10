package io.openems.edge.predictor.pvproduction;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Predictor PV-production", //
		description = "Predicts the PV-production (simulated)")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "pvProductionForecast0";
	
	@AttributeDefinition(name = "Database-ID", description = "Unique ID of the database that this predictor relies on")
	String database_id() default "influx0";
	
	@AttributeDefinition(name = "FID-VL-ID", description = "Unique ID of the FID-VL controller this component has to synchronize with")
	String fidvl_id() default "ctrlFidVl0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	String webconsole_configurationFactory_nameHint() default "Predictor PV-production [{id}]";

	
}
