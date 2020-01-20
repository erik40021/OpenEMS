package io.openems.edge.predictor.load;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;
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
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.jsonapi.JsonApi;
import io.openems.edge.predictor.api.ConsumptionHourlyPredictor;
import io.openems.edge.predictor.api.HourlyPrediction;
import io.openems.edge.predictor.load.data.InputDataParser;
import io.openems.edge.predictor.load.data.Payload;
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
	
	private String model_requested;									// set in config
	private LocalDateTime simulated_current_date_time_on_start;		// set in config

	private String database_id;
	private Payload payload;

	private LocalDateTime real_localdatetime_on_start;	
	private LocalDateTime simulated_start_time;			//start time for forecast (in reality LocalDateTime.now())

	private final Logger log = LoggerFactory.getLogger(LoadForecast.class);
	
	//private int selected_profile = -1;
	
	public LoadForecast() {
		super(OpenemsComponent.ChannelId.values(), //
				ForecastChannelId.values());
		
		this.real_localdatetime_on_start = LocalDateTime.now();
		System.out.println(("constructor passed successfully [LoadForecast]"));
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		System.out.println(("activating component starting [LoadForecast]"));
		super.activate(context, config.id(), config.alias(), config.enabled());
		
		this.model_requested = config.model();
		this.database_id = config.database_id();
		this.simulated_current_date_time_on_start = this.parseDateTime(config.time());
		
		
//		if (!OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "database", config.database_id())) {
//			System.out.println("Updating reference filters failed or wasn't needed");
//			//return; //TODO: throw OpenemsNamedException("");
//		}
		System.out.println(("Activation passed (successfully?) [LoadForecast]"));
	}
	
	@Deactivate
	protected void deactivate() {
		System.out.println("WHYYYYYY (2)");
		super.deactivate();
	}

	
	protected ComponentManager getComponentManager() {
		return this.componentManager;
	}

	@Override
	public HourlyPrediction get24hPrediction() {
		long hours_since_start = this.calculateHoursSinceStart();
		this.simulated_start_time = this.simulated_current_date_time_on_start.plusHours(hours_since_start);
		Integer[] forecast_load = null;
		try {
			forecast_load = this.getForecast();
		} catch (InterruptedException e) {
			this.logError(this.log, e.toString());
			return null;
		}
		if(forecast_load == null) return null;
		return new HourlyPrediction(forecast_load, this.simulated_start_time);
	}
	
	public long calculateHoursSinceStart() {
		return ChronoUnit.HOURS.between(this.real_localdatetime_on_start, LocalDateTime.now());
	}

	private LocalDateTime parseDateTime(String time) {
		String cut_time = time.substring(0, 13);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH");
		return LocalDateTime.parse(cut_time, formatter);
	}


	public Integer[] getForecast() throws InterruptedException {
		this.writeForecastRequest(this.model_requested);
		Thread.sleep(5);	//TODO correct?
		if (this.payload != null) {
			Payload payload = this.payload;
			if (this.model_requested == payload.getModelName()) {
				return payload.getLoad();
			}
		}
		return null;
	}
	
	public void writeForecastRequest(String model_name) {
		this.channel(ForecastChannelId.FORECAST_MODEL_NAME).setNextValue(model_name);
	}
	
	public void receiveLoadForecast(float[] load, String model_name) {
		Integer[] l = new Integer[load.length];
		for(int i=0; i<load.length; i++) {
			l[i] = (int)load[i]*1000;		//assuming HourlyPrediction wants Wh and not kWh ..
		}
		this.payload = new Payload(model_name, l);
		notify();
	}

	@Override
	public CompletableFuture<? extends JsonrpcResponseSuccess> handleJsonrpcRequest(User user, JsonrpcRequest request)
			throws OpenemsNamedException {
		switch (request.getMethod()) {
			case GetLoadForecastInputRequest.METHOD:
				InputDataParser parser = new InputDataParser(this, this.model_requested, this.simulated_start_time, 
						 this.componentManager.getComponent(this.database_id));
				parser.parse();
				return CompletableFuture.completedFuture(new GetLoadForecastInputResponse(request.getId(), parser.getInput()));
				
			case LoadForecastOutputRequest.METHOD:
				LoadForecastOutputRequest output_request = new LoadForecastOutputRequest(request.getParams());
				this.receiveLoadForecast(output_request.getForecast(), output_request.getModelName());
				return CompletableFuture.completedFuture(new LoadForecastOutputResponse(request.getId()));
		}
		return null; 
	}
	
	@Override
	public String debugLog() {
		return this.channel(ForecastChannelId.FORECAST_MODEL_NAME).value().asString();
	}

}
