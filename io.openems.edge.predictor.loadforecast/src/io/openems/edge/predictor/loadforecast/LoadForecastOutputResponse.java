package io.openems.edge.predictor.loadforecast;

import java.util.UUID;

import com.google.gson.JsonObject;

import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;

public class LoadForecastOutputResponse extends JsonrpcResponseSuccess {

	public LoadForecastOutputResponse(UUID id) {
		super(id);
	}

	@Override
	public JsonObject getResult() {
		return new JsonObject();
	}

}
