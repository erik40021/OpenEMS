package io.openems.edge.predictor.load.jsonrpc;

import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;

/**
* Wraps a JSON-RPC Response to "getLoadForecastInput" Request.
* 
* <p>
* 
* <pre>
* {
*   "jsonrpc": "2.0",
*   "id": "UUID",
*   "result": {
*     "input": [
*     	  //24 load values (float)
*     ]
*   }
* }
* </pre>
*/
public class GetLoadForecastInputResponse extends JsonrpcResponseSuccess {

	private final float[] forecasting_input;

	public GetLoadForecastInputResponse(UUID id, float[] forecasting_input) {
		super(id);
		this.forecasting_input = forecasting_input;
	}

	public JsonArray getInput() {
		return this.toJsonArray(this.forecasting_input);
	}
	
	private JsonArray toJsonArray(float[] f_in) {
		JsonArray input_vector = new JsonArray();
		for(int i=0; i<f_in.length; i++) 
			input_vector.add(f_in[i]);
		return input_vector;
	}

	@Override
	public JsonObject getResult() {
		JsonArray input_vector = this.getInput();
		JsonObject j = new JsonObject();
		j.add("input", input_vector);
		return j;
	}

}
