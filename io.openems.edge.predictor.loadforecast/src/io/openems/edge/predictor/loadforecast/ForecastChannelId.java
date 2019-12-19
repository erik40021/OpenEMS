package io.openems.edge.predictor.loadforecast;

import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public enum ForecastChannelId implements io.openems.edge.common.channel.ChannelId {

	UNABLE_TO_FORECAST(Doc.of(Level.FAULT)),
	
	FORECAST_MODEL_NAME(Doc.of(OpenemsType.STRING)),
	
	ASSIGNED_PROFILE(Doc.of(OpenemsType.INTEGER));
	
	//TODO: wie ganzes array in channel laden oder wie array über REST sonst kommunizieren?
	/*FORECAST_INPUT(Doc.of(OpenemsType.DOUBLE)),
	
	FORECAST_LOAD(Doc.of(OpenemsType.DOUBLE)
			.unit(Unit.KILOWATT_HOURS));*/

	private final Doc doc;

	private ForecastChannelId(Doc doc) {
		this.doc = doc;
	}

	@Override
	public Doc doc() {
		return this.doc;
	}
}
