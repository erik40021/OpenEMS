package io.openems.edge.bridge.sml;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition( //
		name = "Bridge SML", //
		description = "Provides a service for connecting to, reading and writing an SML device.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "sml0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Serial-Device", description = "Serial Device Name")
	String device() default "/dev/ttyUSB0";

	@AttributeDefinition(name = "Baudrate", description = "Baudrate of serial device")
	int baud() default 2400;

	String webconsole_configurationFactory_nameHint() default "Bridge SML [{id}]";
}