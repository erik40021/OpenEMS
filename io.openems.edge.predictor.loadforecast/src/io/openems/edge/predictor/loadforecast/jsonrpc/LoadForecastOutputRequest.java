package io.openems.edge.predictor.loadforecast.jsonrpc;

import java.util.UUID;

import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.jsonrpc.base.JsonrpcRequest;
import io.openems.common.utils.JsonUtils;

/**
 * Wraps a JSON-RPC Request to send the forecast load output.
 * 
 * <pre>
 * {
 *   "jsonrpc": "2.0",
 *   "id": "UUID",
 *   "method": "LoadForecastOutput",
 *   "params": {}
 * }
 * </pre>
 */
public class LoadForecastOutputRequest extends JsonrpcRequest {

	public static final String METHOD = "LoadForecastOutput";
	
	private JsonObject params;
	
	private float[] forecast;
	private String model_name;

	public LoadForecastOutputRequest(JsonObject params) throws OpenemsNamedException {
		this(UUID.randomUUID(), params);
	}

	public LoadForecastOutputRequest(UUID id, JsonObject params) throws OpenemsNamedException {
		super(id, METHOD);
		this.extractParams(params);
	}
	
	public void extractParams(JsonObject params) throws OpenemsNamedException {
		this.params = params;
		this.forecast = JsonUtils.getAsFloatArray(JsonUtils.getAsJsonArray(params, "load"));
		this.model_name = JsonUtils.getAsString(params, "model_name");
	}
	
	@Override
	public JsonObject getParams() {
		return this.params;
	}
	
	public float[] getForecast() {
		return this.forecast;
	}
	
	public String getModelName() {
		return this.model_name;
	}

}