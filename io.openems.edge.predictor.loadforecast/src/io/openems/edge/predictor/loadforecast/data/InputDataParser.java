package io.openems.edge.predictor.loadforecast.data;

import java.time.LocalDateTime;

import io.openems.common.types.OpenemsType;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.predictor.loadforecast.ForecastChannelId;
import io.openems.edge.predictor.loadforecast.LoadForecast;

public class InputDataParser {

	private LoadForecast parent;
	private String model_name;
	private LocalDateTime start_time;
	private float[] input = null;
	
	public InputDataParser(LoadForecast parent, String model_name, LocalDateTime start_time) {
		this.parent = parent;
		this.model_name = model_name;
		this.start_time = start_time;
	}
	
	public void parse() {
		switch(model_name) {
		
		case("dnn"):	
			this.parseForecastingInput();
			
		case("p2nn"):
			this.parseForecastingInput();
			Integer assigned_profile = TypeUtils.getAsType(OpenemsType.INTEGER, this.parent.channel(ForecastChannelId.ASSIGNED_PROFILE).value().get());
			this.addProfilingInput(assigned_profile);
			
		case("sw"):
			this.parseForecastingInput();
		}
	}

	public void parseForecastingInput() {
		int start_index = 42; //TODO
		float[] forecasting_input = new float[363];
		float[] load_input = this.parsePastLoads(start_index);
		float[] weather_input = this.parseFutureWeather(start_index);
		for(int i=0; i<load_input.length; i++) {
			forecasting_input[i] = load_input[i];
		}
		for(int i=0; i<weather_input.length; i++) {
			forecasting_input[i+load_input.length] = weather_input[i];
		}
		// TODO rest-eintraege bestimmen und dranhaengen (meanofpastday, currenthour, currentdayinweek, etc.)
		this.input = null; //TODO
	}
	
	private float[] parsePastLoads(int start_index) {
		float[] past_loads = new float[240];
		// TODO aus influxDB vergangene lasten (past7days, past3sameweekdays) anfordern
		return past_loads;
	}
	
	private float[] parseFutureWeather(int start_index) {
		// TODO aus influxDB naechsten 24 h (ab start_index) wetter anfordern
		return null;
	}
	
	
	public float[] parseProfilingInput() {
		int start_index = 42;
		return this.parseProfilingLoads(start_index);
	}

	private float[] parseProfilingLoads(int start_index) {
		// TODO aus influxDB vergangene 28 Tage holen
		return null;
	}

//	public int findStartIndex() {
//		for(int i=0; i<1000; i++) {		//TODO: iterate over InfluxDB entries
//			if()
//		}
//	}
	private void addProfilingInput(Integer assigned_profile) {
		float[] profiling_input;
		if (assigned_profile == null) {
			profiling_input = this.parseProfilingInput();
		}
		else profiling_input = new float[] {assigned_profile};
		float[] new_input = new float[this.input.length + profiling_input.length];
		for(int i=0; i<this.input.length; i++) {
			new_input[i] = this.input[i];
		}
		for(int i=0; i<profiling_input.length; i++) {
			new_input[this.input.length + i] = profiling_input[i];
		}
		this.input = new_input;
	}
	
	public float[] getInput() {
		return this.input;
	}
	
	public String getModelName() {
		return this.model_name;
	}
}
