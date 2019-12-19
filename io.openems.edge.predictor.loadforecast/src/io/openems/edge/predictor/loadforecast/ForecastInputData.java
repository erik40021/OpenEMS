//package io.openems.edge.predictor.loadforecast;
//
//import io.openems.common.types.OpenemsType;
//import io.openems.common.types.OptionsEnum;
//import io.openems.edge.common.channel.Doc;
//
//public enum ForecastInputData implements OptionsEnum {
//
//	MODEL_NAME(Doc.of(OpenemsType.STRING)),
//	
//	INPUT(Doc.of(OpenemsType.LONG)); //TODO: wie ganzes array in channel laden oder wie array über REST sonst kommunizieren?
//	
//	private final Doc doc;
//
//	private ForecastInputData(Doc doc) {
//		this.doc = doc;
//	}
//
//	public Doc doc() {
//		return this.doc;
//	}
//
//	@Override
//	public int getValue() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public String getName() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public OptionsEnum getUndefined() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
