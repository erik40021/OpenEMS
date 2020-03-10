package io.openems.edge.simulator.meter.nrc.fidsimulation;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Simulator NRCMeter FID-Simulation", //
		description = "This simulates a non-regulated-consumption meter for the simulation of FID-VL.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "meter0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "FID-VL-ID", description = "Unique ID of the FID-VL controller this component has to synchronize with")
	String fidvl_id() default "ctrlFidVl0";
	
	@AttributeDefinition(name = "Database-ID", description = "ID of database.")
	String database_id() default "influx0";

	String webconsole_configurationFactory_nameHint() default "Simulator NRCMeter FIDsimulation [{id}]";
}