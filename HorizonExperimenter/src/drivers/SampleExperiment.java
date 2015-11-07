package drivers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.zeromq.ZMQ;

import core.io.Results;
import core.io.Results.Result;

/**
 * This class is a minimum working example of an experiment program that takes
 * arguments from the command line to set up an experiment. It is usually enough
 * to do something like this, but there are some serious restrictions:
 * 1. A default constructor exists (0 arguments) for the class you want to test with.
 * 2. Setter methods exist for all of the command line arguments.
 * 3. You need to parse the command line arguments see: {@link drivers.SampleExperimentKeys}
 * 
 * @author sloscal1
 *
 */
public class SampleExperiment {
	//I'm doing some kind of RL thing... here area a bunch of parameters that I need to set.
	//A good indication that a value is required for one if it is not assigned here. These
	//Correspond to the parameters listed in SampleExperimentKeys.java
	private Class<?> inputClass = Class.forName("drivers.SampleExperiment");
	private String outputDirName = "./";
	private String outputPrefix = "";
	private String outputSuffix = ".dat";
	private double alpha = 0.1;
	private double epsilon = 0.99;
	private double lambda = 0.1;
	private int numEpisodes;
	private boolean useDecay = false;
	//Internal BS
	ZMQ.Socket socket;
	
	public SampleExperiment() throws Exception{
		ZMQ.Context context = ZMQ.context(1);
		socket = context.socket(ZMQ.REP);
		socket.bind("tcp://*:5555");
	}
	/**
	 * Here is a method that does the work for this experiment.
	 */
	public void doStuff(){
		//Here's some arbitrary stuff happening...
		Random r = new Random(15131);
		for(int i = 0; i < numEpisodes; ++i){
			//Go through some batch
			//Do some updates
			//Output some parameters
			List<Double> model_params = new ArrayList<>(Arrays.asList(new Double[]{1.2*(i+1), 321.2*(i+1), 293.2*(i+1)}));
			Results.Result.Builder b = Results.Result.newBuilder();
			System.out.println(b.isInitialized());
			b.setEpisode(i);
			b.setRandseed(r.nextLong());
			b.setVError(r.nextDouble()/(i+1));
			b.addAllModelTheta(model_params);
			System.out.println(b.isInitialized());
			Result res = b.build();
			socket.send(res.toByteArray(), 0);
		}
	}
	
	/**
	 * This is our sample experiment's entry point. We assume that there
	 * are a bunch of command line args that will need to be processed.
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		SampleExperiment obj = SampleExperimentKeysOO.initializeObject(args);
		if(obj != null){
			System.out.println(obj);
			System.out.println("Doing stuff!");
			//Now, obj is ready to roll!
			obj.doStuff();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("SampleExperiment=["
				+"alpha="+alpha
				+",epsilon="+epsilon
				+",lambda="+lambda
				+",numEpisodes="+numEpisodes
				+",useDecay="+useDecay
				+",inputClass="+inputClass.getName()
				+",outputDirName="+outputDirName
				+",outputPrefix="+outputPrefix
				+",outputSuffix="+outputSuffix+"]");
		return sb.toString();
	}
	
	public void setInputClass(Class<?> inputClass) {
		this.inputClass = inputClass;
	}

	public void setOutputDirName(String outputDirName) {
		this.outputDirName = outputDirName;
	}

	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}

	public void setOutputSuffix(String outputSuffix) {
		this.outputSuffix = outputSuffix;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public void setNumEpisodes(int numEpisodes) {
		this.numEpisodes = numEpisodes;
	}

	public void setUseDecay(boolean useDecay) {
		this.useDecay = useDecay;
	}
}
