package io.openems.edge.predictor.load.jsonrpc;

import java.util.UUID;

import com.google.gson.JsonObject;

import io.openems.common.jsonrpc.base.JsonrpcRequest;

/**
* Wraps a JSON-RPC Request to query the Load Forecast Input Data from InfluxDB.
* 
* <pre>
* {
*   "jsonrpc": "2.0",
*   "id": "UUID",
*   "method": "getLoadForecastInput",
*   "params": {}
* }
* </pre>
*/
public class GetLoadForecastInputRequest extends JsonrpcRequest {

	public static final String METHOD = "getLoadForecastInput";

	public GetLoadForecastInputRequest() {
		this(UUID.randomUUID());
	}

	public GetLoadForecastInputRequest(UUID id) {
		super(id, METHOD);
	}

	@Override
	public JsonObject getParams() {
		return new JsonObject();
	}

}

