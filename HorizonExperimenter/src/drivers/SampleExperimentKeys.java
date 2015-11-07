package drivers;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.io.Arg;

public class SampleExperimentKeys {
	/**
	 * Each enum element is a command line argument that this program accepts.
	 * The first parameter is the long name, the second is whether the parameter
	 * takes a value (one of optional, required, or none), and the third parameter
	 * is the short name of the argument. The name of each enum element should be
	 * close to the long name of the argument. It is assumed that each of these
	 * values has a variable somewhere that will be initialized with these values.
	 * 
	 * @author sloscal1
	 *
	 */
	static enum CLIKey{
		ALPHA("alpha", Arg.REQUIRED, 'a'),
		EPS("epsilon", Arg.REQUIRED, 'e'),
		LAMBDA("lambda", Arg.REQUIRED, 'l'),
		NUMEPS("episodes", Arg.REQUIRED, 'n'),
		DECAY("decay-alpha", Arg.NONE, 'd'),
		INCLASS("learner-class", Arg.REQUIRED, 'i'),
		OUTPUT_BASE_DIR("output-base-dir", Arg.REQUIRED, 'b'),
		OUTPUT_PREFIX("output-prefix", Arg.REQUIRED, 'p'),
		OUTPUT_SUFFIX("output-sufffix", Arg.REQUIRED, 's');

		//Everything from here down in the enum is boilerplate and should not change from
		//experiment to experiment.
		private String longName;
		private Arg required;
		private char shortChar;
		private StringBuffer sb = null;

		private CLIKey(String longName, Arg required, char shortChar){
			this.longName = longName;
			this.required = required;
			this.shortChar = shortChar;
		}
		public String getLongName(){return longName;}
		public char getShortChar(){return shortChar;}
		public Arg getRequiredVal(){return required;}
		public StringBuffer getBuffer(){return sb;}
	}

	/**
	 * This method creates an object of type SampleExperiment, and then initializes its
	 * values according to the command line arguments, and the arguments that the program
	 * is looking for according to the {@link CLIKey} enum.
	 * @param commandLineArgs
	 * @return initialized SampleExperiment object, null if there is some parsing error (see stderr)
	 * @throws Exception 
	 */
	public static SampleExperiment initializeObject(String[] commandLineArgs) throws Exception{
		//Make a list of all required flags
		Set<CLIKey> requiredFlags = new HashSet<>();
		//We need a value of Number of episodes or else this is a no go!
		requiredFlags.add(CLIKey.NUMEPS);

		SampleExperiment obj = new SampleExperiment();

		//***** Shouldn't have to change anything until the next ******** section to handle options
		//Build all of the long options to process from the command line...
		LongOpt[] options = new LongOpt[CLIKey.values().length];
		CLIKey[] keys = CLIKey.values();
		Map<Character, Integer> reverseLookup = new HashMap<>();
		String getOptString = "";
		for(int i = 0; i < options.length; ++i){
			Arg req = keys[i].getRequiredVal();
			char single = keys[i].getShortChar();
			options[i] = new LongOpt(keys[i].getLongName(), req.getValue(), keys[i].getBuffer(), single);
			reverseLookup.put(single, i);
			//Need to make the opt string, this one should be a:e:l:n:di:b:p:s:
			getOptString += single;
			if(req == Arg.REQUIRED)
				getOptString += ":";
			else if(req == Arg.OPTIONAL)
				getOptString += "::";
		}
		//Now parse the options:
		Getopt opts = new Getopt("SampleExperiment", commandLineArgs, getOptString, options);
		opts.setOpterr(true);
		
		boolean error = false;
		for(int c = opts.getopt(); c != -1 && !error; c = opts.getopt()){
			//Got an unexpected error... note it.
			if(c == '?'){
				System.err.println("Unexpected command line parameter: "+(char)opts.getOptopt());
				error = true;
				continue;
			}
			//Otherwise, getopts returns the single char flag - map back to the long key
			if(c != 0)
				c = reverseLookup.get((char)c);
			//Otherwise, getopts returns 0 if a long flag was used - pull out the long index
			else
				c = opts.getLongind();
			
			//******** Okay, now write all the handlers below: *****************
			//Figure out what the flag is, and do something in response:
			//Note, this is not very object oriented.
			CLIKey key = keys[c];
			if(key == CLIKey.ALPHA){
				obj.setAlpha(Double.parseDouble(opts.getOptarg()));
			}else if(key == CLIKey.DECAY)
				obj.setUseDecay(true);
			else if(key == CLIKey.EPS)
				obj.setEpsilon(Double.parseDouble(opts.getOptarg()));
			else if(key == CLIKey.NUMEPS)
				obj.setNumEpisodes(Integer.parseInt(opts.getOptarg()));
			else if(key == CLIKey.INCLASS)
				//If we do anything non-trivial, this can get ugly.
				try {
					obj.setInputClass(Class.forName(opts.getOptarg()));
				} catch (ClassNotFoundException e) {
					System.err.println(opts.getOptarg()+" is not in the classpath. "+
							"Also remember to use fully qualified names.");
					error = true;
				}
			else if(key == CLIKey.OUTPUT_BASE_DIR)
				obj.setOutputDirName(opts.getOptarg());
			else if(key == CLIKey.OUTPUT_PREFIX)
				obj.setOutputPrefix(opts.getOptarg());
			else if(key == CLIKey.OUTPUT_SUFFIX)
				obj.setOutputSuffix(opts.getOptarg());
			//Remove the key from the required keys:
			requiredFlags.remove(key);
		}
		
		//**** Back to boilerplate: Check to see if a required flag was not present:
		if(!requiredFlags.isEmpty()){
			error = true;
			for(CLIKey key : requiredFlags)
				System.err.println("Error: Required flag: --"+key.getLongName()+" (-"+key.getShortChar()+") is not specified.");
		}
		return error? null : obj;
	}
}