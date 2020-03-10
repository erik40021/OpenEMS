package io.openems.edge.predictor.load.data;

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
import io.openems.edge.predictor.load.ForecastChannelId;
import io.openems.edge.predictor.load.LoadForecast;
import io.openems.edge.timedata.api.Timedata;


/**
 * requests input data for load forecasting from database and parses all data points into
 * one input vector
 */
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
	
	/**
	 * parses input vector for given model
	 * @throws OpenemsNamedException
	 */
	public void parse() throws OpenemsNamedException {
		Collection<SortedMap<ChannelAddress, JsonElement>> load_query = this.queryTimedata(true);
		Collection<SortedMap<ChannelAddress, JsonElement>> weather_query = this.queryTimedata(false);
		switch(this.model_name) {
			case("nn-f"):	
				this.parseForecastingInput(load_query, weather_query);
				break;
			case("p2nn-f"):
				this.parseForecastingInput(load_query, weather_query);
				Integer assigned_profile = TypeUtils.getAsType(OpenemsType.INTEGER, this.parent.channel(ForecastChannelId.ASSIGNED_PROFILE).value().get());
				this.addProfilingInput(assigned_profile, load_query);
				break;
			case("sw-f"):
				this.parseForecastingInput(load_query, weather_query);
				break;
		}
	}
	
	/**
	 * queries database to send past loads or future weather data
	 * @throws OpenemsNamedException
	 */
	private Collection<SortedMap<ChannelAddress, JsonElement>> queryTimedata(boolean is_in_past) throws OpenemsNamedException {
		int resolution = 3600;
		Set<ChannelAddress> channels = new TreeSet<>();
		ZonedDateTime fromDate;
		ZonedDateTime toDate;
		if(is_in_past) {
			//time dates are excluded (!)
			fromDate = ZonedDateTime.of(this.start_time.minusDays(28), ZoneId.of("UTC"));
			toDate = ZonedDateTime.of(this.start_time.plusHours(1), ZoneId.of("UTC"));
			channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_LOAD));
		}
		else {
			fromDate = ZonedDateTime.of(this.start_time, ZoneId.of("UTC"));
			toDate = ZonedDateTime.of(this.start_time.plusHours(16), ZoneId.of("UTC"));
			channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_TEMP));
			channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_PREC));
			channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_APPTEMP));
			channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_VISIB));
			channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_CLOUD));
		}
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> response_map = 
				database.queryHistoricData(null, fromDate, toDate, channels, resolution);
		response_map.remove(fromDate);	//first queried element is always null: remove it
		return response_map.values();
	}

	/**
	 * parses one coherent input vector
	 * @param load_query
	 * @param weather_query
	 * @throws OpenemsNamedException
	 */
	private void parseForecastingInput(Collection<SortedMap<ChannelAddress, JsonElement>> load_query, 
			Collection<SortedMap<ChannelAddress, JsonElement>> weather_query) throws OpenemsNamedException {
		float[] forecasting_input = new float[291];
		float[] load_input = this.parsePastLoads(load_query);
		float[] weather_input = this.parseFutureWeather(weather_query);
		int i = 0;
		for(i = 0; i<load_input.length; i++) {
			forecasting_input[i] = load_input[i];
		}
		for(int j = 0; j<weather_input.length; j++) {
			forecasting_input[j + i] = weather_input[j];
		}
		int l = forecasting_input.length;
		forecasting_input[l-3] = InputDataParser.getMeanOfPastDay(load_input);
		forecasting_input[l-2] = InputDataParser.getDayOfWeek(this.start_time);
		forecasting_input[l-1] = InputDataParser.getHourOfDay(this.start_time);
		
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
	
	/**
	 * parses past load points needed for input vector into float array
	 * @throws OpenemsNamedException
	 */
	private float[] parsePastLoads(Collection<SortedMap<ChannelAddress, JsonElement>> load_query) throws OpenemsNamedException {
		int month_length = 24*28;
		float[] past_month = this.extractAsFloatArray(load_query, LoadForecastConstants.INFLUXDB_LOAD, month_length);
		float[] past_loads = new float[213];
		
		//order: oldest -> newest
		int j = 0;
		//same weekday 28 days ago
        int start_index = 0;
        int i = start_index;
        while (i < start_index + 15) {
        	past_loads[j] = past_month[i];
        	i ++; j++;
        }
        //same weekday 21 days ago
        start_index = 7*24;
        i = start_index;
        while (i < start_index + 15) {
        	past_loads[j] = past_month[i];
        	i ++; j++;
        }
        //same weekday 14 days ago
        start_index = 14*24;
        i = start_index;
        while (i < start_index + 15) {
        	past_loads[j] = past_month[i];
        	i ++; j++;
        }
        // past entire week
		start_index = 21*24;
        i = start_index;
        while (i < start_index + 7*24) {
        	past_loads[j] = past_month[i];
        	i ++; j++;
        }
      return past_loads;
	}

	/**
	 * parses future weather data points needed for input vector into float array
	 * @throws OpenemsNamedException
	 */
	private float[] parseFutureWeather(Collection<SortedMap<ChannelAddress, JsonElement>> weather_query) throws OpenemsNamedException {
		float[] weather = new float[15*5];
		int j = 0;
		float[] output = this.extractAsFloatArray(weather_query, LoadForecastConstants.INFLUXDB_TEMP, 15);
		for(int i=0; i<15; i++, j++) {
			weather[j] = output[i];
		}
		output = this.extractAsFloatArray(weather_query, LoadForecastConstants.INFLUXDB_PREC, 15);
		for(int i=0; i<15; i++, j++) {
			weather[j] = output[i];
		}
		output = this.extractAsFloatArray(weather_query, LoadForecastConstants.INFLUXDB_APPTEMP, 15);
		for(int i=0; i<15; i++, j++) {
			weather[j] = output[i];
		}
		output = this.extractAsFloatArray(weather_query, LoadForecastConstants.INFLUXDB_VISIB, 15);
		for(int i=0; i<15; i++, j++) {
			weather[j] = output[i];
		}
		output = this.extractAsFloatArray(weather_query, LoadForecastConstants.INFLUXDB_CLOUD, 15);
		for(int i=0; i<15; i++, j++) {
			weather[j] = output[i];
		}
		return weather;
	}

	
	private static float getMeanOfPastDay(float[] load_input) {
		float sum = 0;
		for(int i=0; i<24; i++) {
			sum += load_input[i + 189];
		}
		return sum / 24;
	}
	
	
	private static int getHourOfDay(LocalDateTime date) {
		return date.plusHours(1).getHour();
	}
	
	
	private static int getDayOfWeek(LocalDateTime date) {
		return date.getDayOfWeek().getValue();
	}
	
	
	/**
	 * parses profiling input data (exclusively) for P2NN-F model
	 * @throws OpenemsNamedException
	 */
	private void addProfilingInput(Integer assigned_profile, Collection<SortedMap<ChannelAddress, JsonElement>> load_query) throws OpenemsNamedException {
		float[] profiling_input;
		// has this household already been assigned to a profile?
		if (assigned_profile == null) {	
			// no? -> get input data for P2NN to profile the new household
			profiling_input = this.extractAsFloatArray(load_query, LoadForecastConstants.INFLUXDB_LOAD, 28*24);
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
	
	
	
	
	
	/**
	 * parses perfect load forecasts for simulation comparison
	 * @throws OpenemsNamedException
	 */
	public Integer[] parsePerfect() throws OpenemsNamedException {
		int resolution = 3600;
		Set<ChannelAddress> channels = new TreeSet<>();
		ZonedDateTime fromDate;
		ZonedDateTime toDate;
		//time dates are excluded (!)
		fromDate = ZonedDateTime.of(this.start_time, ZoneId.of("UTC"));
		toDate = ZonedDateTime.of(this.start_time.plusHours(16), ZoneId.of("UTC"));
		channels.add(ChannelAddress.fromString(LoadForecastConstants.INFLUXDB_LOAD));
		
		SortedMap<ZonedDateTime, SortedMap<ChannelAddress, JsonElement>> response_map = 
				database.queryHistoricData(null, fromDate, toDate, channels, resolution);
		response_map.remove(fromDate);	//first queried element is always null: remove it
		
		float[] f =  this.extractAsFloatArray(response_map.values(), LoadForecastConstants.INFLUXDB_LOAD, 15);
		Integer[] f2 = new Integer[f.length];
		for(int i=0; i < f.length; i++) {
			f2[i] = (int) (f[i] * 1000);
		}
		return f2;
	}
}
