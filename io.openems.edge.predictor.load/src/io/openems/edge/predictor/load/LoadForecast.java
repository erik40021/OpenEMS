package io.openems.edge.predictor.load;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.jsonrpc.base.JsonrpcRequest;
import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;
import io.openems.common.session.User;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.jsonapi.JsonApi;
import io.openems.edge.predictor.api.ConsumptionHourlyPredictor;
import io.openems.edge.predictor.api.HourlyPrediction;
import io.openems.edge.predictor.load.data.InputDataParser;
import io.openems.edge.predictor.load.jsonrpc.GetLoadForecastInputRequest;
import io.openems.edge.predictor.load.jsonrpc.GetLoadForecastInputResponse;
import io.openems.edge.predictor.load.jsonrpc.LoadForecastOutputRequest;
import io.openems.edge.predictor.load.jsonrpc.LoadForecastOutputResponse;

@Designate(ocd = Config.class, factory = true)
@Component(
	name = "Predictor.Load.LoadForecast", //
	immediate = true, //
	configurationPolicy = ConfigurationPolicy.REQUIRE)
public class LoadForecast extends AbstractOpenemsComponent //AbstractLoadForecastModel
		implements ConsumptionHourlyPredictor, JsonApi, OpenemsComponent {
	

	@Reference
	protected ComponentManager componentManager;
	
	private String model_requested;

	private String database_id;
	private String fidvl_id;
	private HourlyPrediction forecast;

	private LocalDateTime simulated_start_time;			//start time for forecast (in reality LocalDateTime.now())
	private float simulation_scaling_factor = 0.0f;

	private final Logger log = LoggerFactory.getLogger(LoadForecast.class);

	
	public LoadForecast() {
		super(OpenemsComponent.ChannelId.values(), //
				ForecastChannelId.values());
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		
		this.model_requested = config.model();
		this.database_id = config.database_id();
		this.fidvl_id = config.fidvl_id();
		this.forecast = null; //reset forecast in case model_requested changed in config
	}
	
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	protected ComponentManager getComponentManager() {
		return this.componentManager;
	}

	@Override
	public HourlyPrediction get24hPrediction() {
		try {
			this.updateSimulatedCurrentTime();
		} catch (IllegalArgumentException | OpenemsNamedException e) {
			e.printStackTrace();
			return null;
		}
		if(this.forecast == null) {
			this.log.info("Forecast not available yet. Requesting python forecaster to send forecast");
			this.writeForecastRequest(this.model_requested);
			return null;
		}
		else {
			HourlyPrediction current_forecast = this.forecast;
			this.forecast = null; //delete old (=used) forecast
			return current_forecast;
		}
	}

	private void updateSimulatedCurrentTime() throws IllegalArgumentException, OpenemsNamedException {
		String time = this.componentManager.getComponent(this.fidvl_id).channel("SimulatedCurrentTime").getNextValue().get().toString();
		String cut_time = time.substring(0,13);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH");
		this.simulated_start_time = LocalDateTime.parse(cut_time, formatter);
	}
	
	public void writeForecastRequest(String model_name) {
		if(model_name.contentEquals("perfect")) {
			this.getPerfect(); return;
		}
		this.channel(ForecastChannelId.FORECAST_MODEL_NAME).setNextValue(model_name);
	}
	
	private void getPerfect() {
		InputDataParser parser; Integer[] f;
		try {
			parser = new InputDataParser(this, "perfect", this.simulated_start_time, 
					 this.componentManager.getComponent(this.database_id));
			f = parser.parsePerfect();
		} catch (OpenemsNamedException e) {
			e.printStackTrace();
			System.err.println("Cannot get perfect forecast");
			return;
		}
		if(this.simulation_scaling_factor == 0.0f) {
			try {
				this.simulation_scaling_factor = (float) this.componentManager.getComponent(this.fidvl_id).channel("SimulationScalingFactorLoad").getNextValue().get();
			} catch (IllegalArgumentException | OpenemsNamedException e) {
				e.printStackTrace();
				System.err.println("Cannot apply scaling factor for simulation");
				return;
			}
		}
		this.forecast = new HourlyPrediction(scale(f, this.simulation_scaling_factor), this.simulated_start_time);
	}

	public void receiveLoadForecast(Integer[] load, String model_name, LocalDateTime time_of_request, Integer assigned_profile) {
		if(this.simulation_scaling_factor == 0.0f) {
			try {
				this.simulation_scaling_factor = (float) this.componentManager.getComponent(this.fidvl_id).channel("SimulationScalingFactorLoad").getNextValue().get();
			} catch (IllegalArgumentException | OpenemsNamedException e) {
				e.printStackTrace();
				System.err.println("Cannot apply scaling factor for simulation");
				return;
			}
		}
		this.forecast = new HourlyPrediction(scale(load, this.simulation_scaling_factor), time_of_request);
		//take back request to receive forecast
		this.channel(ForecastChannelId.FORECAST_MODEL_NAME).setNextValue(null);
		if(assigned_profile != null)
			this.channel(ForecastChannelId.ASSIGNED_PROFILE).setNextValue(assigned_profile);
	}
	
	private static Integer[] scale(Integer[] a, float factor) {
		Integer[] b = new Integer[a.length];
		for(int i=0; i<b.length; i++) {
			b[i] = (int) (a[i] * factor);
		}
		return b;
	}

	@Override
	public CompletableFuture<? extends JsonrpcResponseSuccess> handleJsonrpcRequest(User user, JsonrpcRequest request)
			throws OpenemsNamedException {
		System.out.println("\n[LoadForecast] Received Request "+request.getMethod()+"\n");
		switch (request.getMethod()) {
			case GetLoadForecastInputRequest.METHOD:
				InputDataParser parser = new InputDataParser(this, this.model_requested, this.simulated_start_time, 
						 this.componentManager.getComponent(this.database_id));
				parser.parse();
				return CompletableFuture.completedFuture(new GetLoadForecastInputResponse(request.getId(), parser.getInput()));
				
			case LoadForecastOutputRequest.METHOD:
				System.out.println("\n[LoadForecast] Output of Request: "+request.getParams().toString()+"\n");
				LoadForecastOutputRequest output_request = new LoadForecastOutputRequest(request.getParams());
				this.receiveLoadForecast(output_request.getForecast(), output_request.getModelName(), this.simulated_start_time,
						output_request.getAssignedProfile());
				return CompletableFuture.completedFuture(new LoadForecastOutputResponse(request.getId()));
		}
		return null; 
	}
	
	@Override
	public String debugLog() {
		return  "Forecast-model-name: "+this.channel(ForecastChannelId.FORECAST_MODEL_NAME).value().asString()+
				", assigned profile: "+this.channel(ForecastChannelId.ASSIGNED_PROFILE).getNextValue().asString();
	}

}
