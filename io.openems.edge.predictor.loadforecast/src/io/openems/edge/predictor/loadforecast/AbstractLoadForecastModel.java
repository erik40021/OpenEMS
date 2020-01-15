package io.openems.edge.predictor.loadforecast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.predictor.api.HourlyPredictor;
import io.openems.edge.predictor.loadforecast.data.Payload;

public abstract class AbstractLoadForecastModel extends AbstractOpenemsComponent {

	protected final Logger log = LoggerFactory.getLogger(AbstractLoadForecastModel.class);

	protected Payload payload = null;

	protected AbstractLoadForecastModel() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ForecastChannelId.values()
		);
	}
	protected abstract ComponentManager getComponentManager();
}
