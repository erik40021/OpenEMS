package io.openems.edge.meter.janitza.umg96rme;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.meter.api.MeterType;

@ObjectClassDefinition( //
		name = "Meter Janitza UMG 96RM-E", //
		description = "Implements the Janitza UMG 96RM-E power analyser.")
@interface Config {
	String service_pid();

	String id() default "meter0";

	boolean enabled() default true;

	@AttributeDefinition(name = "Meter-Type", description = "What is measured by this Meter?")
	MeterType type() default MeterType.PRODUCTION;

	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus brige.")
	String modbus_id();

	@AttributeDefinition(name = "Modbus Unit-ID", description = "The Unit-ID of the Modbus device. Defaults to '1' for Modbus/TCP.")
	int modbusUnitId() default 1;

	@AttributeDefinition(name = "Minimum Ever Active Power", description = "This is automatically updated.")
	int minActivePower();

	@AttributeDefinition(name = "Maximum Ever Active Power", description = "This is automatically updated.")
	int maxActivePower();

	@AttributeDefinition(name = "Modbus target filter", description = "This is auto-generated by 'Modbus-ID'.")
	String Modbus_target() default "";

	String webconsole_configurationFactory_nameHint() default "Meter Janitza UMG 96RM-E [{id}]";
}