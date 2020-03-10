package io.openems.edge.predictor.load.jsonrpc;

import java.util.UUID;

import com.google.gson.JsonArray;
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
	
	private Integer[] forecast;
	private String model_name;
	private int assigned_profile;

	public LoadForecastOutputRequest(JsonObject params) throws OpenemsNamedException {
		this(UUID.randomUUID(), params);
	}

	public LoadForecastOutputRequest(UUID id, JsonObject params) throws OpenemsNamedException {
		super(id, METHOD);
		this.extractParams(params);
	}
	
	public void extractParams(JsonObject params) throws OpenemsNamedException {
		this.params = params;
		JsonArray json_load = params.get("load").getAsJsonArray();
		Integer[] loads =  new Integer[json_load.size()];
		// Extract numbers from JSON array.
		for (int i = 0; i < json_load.size(); i++) {
		    loads[i] = json_load.get(i).getAsInt();
		}
		this.forecast = loads;
		this.model_name = JsonUtils.getAsString(params, "model_name");
		try {
			this.assigned_profile = JsonUtils.getAsInt(params, "assigned_profile");
		}
		catch(OpenemsNamedException e) {
			return;
		}
	}
	
	@Override
	public JsonObject getParams() {
		return this.params;
	}
	
	public Integer getAssignedProfile() {
		return this.assigned_profile;
	}
	
	public Integer[] getForecast() {
		return this.forecast;
	}
	
	public String getModelName() {
		return this.model_name;
	}

}