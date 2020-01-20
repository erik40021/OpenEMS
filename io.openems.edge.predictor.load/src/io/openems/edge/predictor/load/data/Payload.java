package io.openems.edge.predictor.load.data;

public class Payload {

	
	String model_name;

	Integer[] load;
	
	public Payload(String model_name, Integer[] load) {
		this.model_name = model_name;
		this.load = load;
	}
	
	public String getModelName() {
		return model_name;
	}

	public Integer[] getLoad() {
		return load;
	}
}
