package io.openems.edge.controller.arduino;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "Arduino Voltage Controller", //
		description = "Controls the Arduino.")
@interface Config {
	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlArduinoVoltage";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "ArduinoId", description = "ID of Arduino device.")
	String arduino_id() default "arduino0";

	String webconsole_configurationFactory_nameHint() default "Controller Arduino Voltage [{id}]";
}

