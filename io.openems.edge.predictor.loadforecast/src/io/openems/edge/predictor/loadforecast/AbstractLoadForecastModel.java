package io.openems.edge.predictor.loadforecast;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.predictor.api.HourlyPredictor;
import io.openems.edge.predictor.loadforecast.data.Payload;

public abstract class AbstractLoadForecastModel extends AbstractOpenemsComponent implements HourlyPredictor {

	protected final Logger log = LoggerFactory.getLogger(AbstractLoadForecastModel.class);
	
	//private final ChannelAddress channelAddress;
	//private final Clock clock;
	
	protected Payload payload = null;


//	private final TreeMap<LocalDateTime, EnergyData> hourlyEnergyData = new TreeMap<LocalDateTime, EnergyData>();
	protected AbstractLoadForecastModel(Clock clock, String componentId,
			io.openems.edge.common.channel.ChannelId channelId) {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ForecastChannelId.values() //
		);
//		this.channelAddress = new ChannelAddress(componentId, channelId.id());
//		this.clock = clock; TODO: is this superclass necessary?
	}
	
	protected AbstractLoadForecastModel(String componentId,
			io.openems.edge.common.channel.ChannelId channelId) {
		this(Clock.systemDefaultZone(), componentId, channelId);
	}

	protected abstract ComponentManager getComponentManager();

	
	
	/**
	 * Collects the forecast data on every cycle.
	 * 
	 * @param event the Event provided by {@link EventHandler}.
	 * @throws IOException 
	 */
	/*public void handleEvent(Event event)   {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			try {
				//TODO load current forecast into channels on cycle event
				
				this.channel(ForecastChannelId.UNABLE_TO_FORECAST).setNextValue(false);
			} catch (OpenemsException e) {
				this.logError(this.log, e.getMessage());
				this.channel(ForecastChannelId.UNABLE_TO_FORECAST).setNextValue(true);
			} 
			break;
		}
	}*/
}
