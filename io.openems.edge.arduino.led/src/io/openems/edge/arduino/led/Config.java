package io.openems.edge.arduino.led;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "Red Arduino Led", //
		description = "Implements the Red Arduino Led System.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "arduino0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";	

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Read-Only mode", description = "Enables Read-Only mode")
	boolean readOnlyMode() default false;
	
	@AttributeDefinition(name = "Voltage Threshhold", description = "Threshhold of voltage that defines if LED is on or off")
	int voltageThreshhold() default 3;

	@AttributeDefinition(name = "Port-Name", description = "The name of the serial port - e.g. '/dev/ttyUSB0' or 'COM3'")
	String portName() default "/dev/ttyACM0";

	String webconsole_configurationFactory_nameHint() default "Red Arduino Led [{id}]";
}
