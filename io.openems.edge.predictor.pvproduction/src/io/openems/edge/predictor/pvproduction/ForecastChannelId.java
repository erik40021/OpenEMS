package io.openems.edge.predictor.pvproduction;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public enum ForecastChannelId implements io.openems.edge.common.channel.ChannelId {

	PVGEN_FUTURE_SIMULATED(Doc.of(OpenemsType.INTEGER));
	
	private final Doc doc;

	private ForecastChannelId(Doc doc) {
		this.doc = doc;
	}

	@Override
	public Doc doc() {
		return this.doc;
	}

}