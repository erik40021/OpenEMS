package io.openems.edge.predictor.snn;

import java.io.*;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import org.osgi.service.component.annotations.Component;

@Component(name="io.openems.edge.predictor.snn")
public class SNNForecaster {

	
	private Object ret;
	
	public SNNForecaster() {
		
	}
	
	public void import_snn_model() {
		//https://towardsdatascience.com/deploying-keras-deep-learning-models-with-java-62d80464f34a
		// load the model
		String simpleMlp = new ClassPathResource("model_name.h5").getFile().getPath();
		MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
		int[] random_sample = {1,2,5,5,2,21,4};
		int[] sample_prediction = model.output(random_sample);
	}
	
	/*//do they make this platform dependent???! -yes -> this is shit
	public void exec_python_runtime_approach() {
		try {
			Process p = Runtime.getRuntime().exec("python python-program-to-execute.py");
			 
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 
			this.ret = new Integer(in.readLine()).intValue(); //return value/object/whatever. cast accordingly and we good.
			
		} catch(IOException e) {
			System.out.println(e);
		}
	}
	public void exec_python_processbuilder_approach() {
		try {
			ProcessBuilder pb = new ProcessBuilder("python","python-program-to-execute.py");
			Process p = pb.start();
			 
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 
			this.ret = new Integer(in.readLine()).intValue(); //return value/object/whatever. cast accordingly and we good.
			
		} catch(IOException e) {
			System.out.println(e);
		}
	}*/
	

}
