package io.openems.edge.predictor.load;

import io.openems.common.channel.Level;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;

public enum ForecastChannelId implements io.openems.edge.common.channel.ChannelId {

	UNABLE_TO_FORECAST(Doc.of(Level.FAULT)),
	
	FORECAST_MODEL_NAME(Doc.of(OpenemsType.STRING)),
	
	ASSIGNED_PROFILE(Doc.of(OpenemsType.STRING)),
	
	
	//simulated channels to enable queryHistoricData() requests on InfluxDB:
	
	LOAD_HISTORIC_SIMULATED(Doc.of(OpenemsType.INTEGER)),
	
	TEMP_FUTURE_SIMULATED(Doc.of(OpenemsType.INTEGER)),
	
	PREC_FUTURE_SIMULATED(Doc.of(OpenemsType.INTEGER)),
	
	APPTEMP_FUTURE_SIMULATED(Doc.of(OpenemsType.INTEGER)),
	
	VISIB_FUTURE_SIMULATED(Doc.of(OpenemsType.INTEGER)),
	
	CLOUD_FUTURE_SIMULATED(Doc.of(OpenemsType.INTEGER));
	
	
	private final Doc doc;

	private ForecastChannelId(Doc doc) {
		this.doc = doc;
	}

	@Override
	public Doc doc() {
		return this.doc;
	}

}