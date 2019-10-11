package io.openems.edge.bridge.sml;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.EventConstants;
import org.osgi.service.metatype.annotations.Designate;
import io.openems.edge.common.event.EdgeEventConstants;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Bridge.Sml", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
		})
public class BridgeSmlImpl {

	

	@Activate
	void activate(Config config) {
	}

	@Deactivate
	void deactivate() {
	}

}
