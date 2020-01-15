package io.openems.edge.predictor.loadforecast.data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import com.google.gson.JsonElement;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.predictor.loadforecast.ForecastChannelId;
import io.openems.edge.predictor.loadforecast.LoadForecast;
import io.openems.edge.timedata.api.Timedata;

public class InputDataParser {

	private LoadForecast parent;
	private String model_name;
	private LocalDateTime start_time;
	private float[] input = null;
	private Timedata database;
	
	public InputDataParser(LoadForecast parent, String model_name, LocalDateTime start_time, Timedata database) {
		this.parent = parent;
		this.model_name = model_name;
		this.start_time = start_time;
		this.database = database;
	}
	
	public void parse() throws OpenemsNamedException {
		Collection<SortedMap<ChannelAddress, JsonElement>> query_response = this.queryTimedata();
		switch(model_name) {
			case("dnn"):	
				this.parseForecastingInput(query_response);
				
			case("p2nn"):
				this.parseForecastingInput(query_response);
				Integer assigned_profile = TypeUtils.getAsType(OpenemsType.INTEGER, this.parent.channel(ForecastChannelId.ASSIGNED_PROFILE).value().get());
				this.addProfilingInput(assigned_profile, query_response);
				
			case("sw"):
				this.parseForecastingInput(query_response);
		}
	}

	private Collection<SortedMap<ChannelAddress, JsonElement>> queryTimedata() throws OpenemsNamedException {
		ZonedDateTime fromDate = ZonedDateTime.of(this.start_time, ZoneId.of("UTC"));
		ZonedDateTime toDate = ZonedDateTime.of(2014, 11, 13, 0, 0, 0, 0, ZoneId.of("UTC"));
		int resolution = 1; //TODO: 1 seconds only fits if datasource-reader reads slower than/equally fast like 1 second (currently 3600 sec...)
		Set<ChannelAddress> channels = new TreeSet<>();
		channels.add(ChannelAddress.fromString("Load")); //TODO correct channel names??
		channels.add(ChannelAddress.fromString("Temp"));
		channels.add(ChannelAddress.fromString("Prec"));
		channels.add(ChannelAddress.fromString("AppTemp"));
		channels.add(ChannelAddress.fromString("Visib"));
		channels.add(ChannelAddress.fromString("Cloud"));
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> response_map = database.queryHistoricData(null, fromDate, toDate, channels, resolution);
//		SortedMap<ChannelAddress, JsonElement> response_full_timeframe = new TreeMap();
//		for(int i=0; i<response_map.size(); i++) {
//			SortedMap<ChannelAddress, JsonElement> m = response_map.values()
//			response_full_timeframe.put
//		}
		return response_map.values();
		
	}

	private void parseForecastingInput(Collection<SortedMap<ChannelAddress, JsonElement>> query_response) throws OpenemsNamedException {
		int start_index = 42; //TODO
		float[] forecasting_input = new float[363];
		float[] load_input = this.parsePastLoads(query_response);
		float[] weather_input = this.parseFutureWeather(query_response);
		int i = 0;
		for(i = 0; i<load_input.length; i++) {
			forecasting_input[i] = load_input[i];
		}
		for(i = i; i<weather_input.length; i++) {
			forecasting_input[i] = weather_input[i];
		}
		forecasting_input[i++] = InputDataParser.getMeanOfPastDay(load_input);
		forecasting_input[i++] = InputDataParser.getDayOfWeek(this.start_time);
		forecasting_input[i++] = InputDataParser.getHourOfDay(this.start_time);
		
		this.input = forecasting_input;
	}

	private float[] extractAsFloatArray(Collection<SortedMap<ChannelAddress, JsonElement>> collection, String channel, int length) throws OpenemsNamedException {
		float[] a = new float[length];
		int k = 0;
		ChannelAddress channel_address = ChannelAddress.fromString(channel);
		for(SortedMap<ChannelAddress, JsonElement> map: collection) {
			a[k] = map.get(channel_address).getAsFloat();
			k++;
		}
		return a;
	}
	
	private float[] parsePastLoads(Collection<SortedMap<ChannelAddress, JsonElement>> query_response) throws OpenemsNamedException {
		int month_length = 24*28;
		float[] past_month = this.extractAsFloatArray(query_response, "Load", month_length);
		
//		JsonArray loads = output.getAsJsonArray();
//		if(loads.size() < month_length) return null; //TODO better with exception
//		float[] past_month = new float[month_length]; //TODO in what order is the array? latest -> oldest or oldest -> latest?
//		for(int i=0; i < month_length; i++) {
//			past_month[i] = loads.get(i).getAsFloat();
//		}
		float[] past_loads = new float[240];
		int j = 0;
		//same weekday 28 days ago
        for(int i=month_length-1; i>=27*24; i--) {
        	past_loads[j] = past_month[i];
        	j++;
        }
        //same weekday 21 days ago
        for(int i=21*24-1; i>=20*24; i--) {
        	past_loads[j] = past_month[i];
        	j++;
        }
        //same weekday 14 days ago
        for(int i=14*24-1; i>=13*24; i--) {
        	past_loads[j] = past_month[i];
        	j++;
        }
        //whole last week
        for(int i=7*24-1; i<=0; i++) {
        	past_loads[j] = past_month[i];
        	j++;
        }
        return past_loads;
	}

	private float[] parseFutureWeather(Collection<SortedMap<ChannelAddress, JsonElement>> query_response) throws OpenemsNamedException {
		float[] weather = new float[24*5];
		int j = 0;
		float[] output = this.extractAsFloatArray(query_response, "Temp", 24);
		for(int i=23; i>=0; i--) { //TODO: check array order
			weather[j] = output[i];
			j++;
		}
		output = this.extractAsFloatArray(query_response, "Prec", 24);
		for(int i=23; i>=0; i--) {
			weather[j] = output[i];
			j++;
		}
		output = this.extractAsFloatArray(query_response, "AppTemp", 24);
		for(int i=23; i>=0; i--) {
			weather[j] = output[i];
			j++;
		}
		output = this.extractAsFloatArray(query_response, "Visib", 24);
		for(int i=23; i>=0; i--) {
			weather[j] = output[i];
			j++;
		}
		output = this.extractAsFloatArray(query_response, "Cloud", 24);
		for(int i=23; i>=0; i--) {
			weather[j] = output[i];
			j++;
		}
		return weather;
	}

	private float[] parseProfilingInput(Collection<SortedMap<ChannelAddress, JsonElement>> query_response) throws OpenemsNamedException {
		return this.extractAsFloatArray(query_response, "Load", 28*24);
	}
	
	private static float getMeanOfPastDay(float[] load_input) {
		float sum = 0;
		for(int i=0; i<24; i++) {
			sum += load_input[i + 216];
		}
		return sum / 24;
	}
	private static int getHourOfDay(LocalDateTime date) {
		return date.getHour();
	}
	private static int getDayOfWeek(LocalDateTime date) {
		return date.getDayOfWeek().getValue();
	}
	
	
	// profiling data:
	private void addProfilingInput(Integer assigned_profile, Collection<SortedMap<ChannelAddress, JsonElement>> query_response) throws OpenemsNamedException {
		float[] profiling_input;
		// has this household already been assigned to a profile?
		if (assigned_profile == null) {	
			// no? -> get input data for P2NN to profile the new household
			profiling_input = this.parseProfilingInput(query_response);
		}
		// yes? -> fill in already known profile instead
		else profiling_input = new float[] {assigned_profile};
		
		// append profiling_input to forecasting_input
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
