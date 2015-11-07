package drivers;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.io.Arg;

/**
 * This is an Object-Oriented parsing version of the SampleExperimentKeys example. The
 * only change is that we no longer have a nasty if/else statement in the parsing section,
 * but overloaded an "initialize" method in the enum for each parameter to do what it needs to
 * with the argument.
 * <ul>
 * <li>Pros: Much more object-oriented design that keeps behaviors near their declarations, and we
 * no longer need to check the enum element, we can just dispatch what we need.</li>
 * <li>Cons: The enum itself is messy: it is no longer obvious at a glance if all the command line
 * args are accounted for or not.</li>
 * </ul>
 * <p>
 * Note that the con can be mitigated while writing the class by filling in the behaviors of each
 * element after all of them have been listed. Thus, I prefer this method to the SampleExperimentKeys
 * approach.
 * 
 * @author sloscal1
 *
 */
public class SampleExperimentKeysOO {
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
		ALPHA("alpha", Arg.REQUIRED, 'a'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setAlpha(Double.parseDouble(value));
				return false;
			}
		},
		EPS("epsilon", Arg.REQUIRED, 'e'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setEpsilon(Double.parseDouble(value));
				return false;
			}
		},
		LAMBDA("lambda", Arg.REQUIRED, 'l'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setLambda(Double.parseDouble(value));
				return false;
			}
		},
		NUMEPS("episodes", Arg.REQUIRED, 'n'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setNumEpisodes(Integer.parseInt(value));
				return false;
			}
		},
		DECAY("decay-alpha", Arg.NONE, 'd'),
		INCLASS("learner-class", Arg.REQUIRED, 'i'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				boolean error = false;
				try {
					Class<?> cls = Class.forName(value);
					obj.setInputClass(cls);
				} catch (ClassNotFoundException e) {
					System.err.println(value+" is not in the classpath. "+
							"Also remember to use fully qualified names.");
					error = true;
				}
				return error;
			}
		},
		OUTPUT_BASE_DIR("output-base-dir", Arg.REQUIRED, 'b'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setOutputDirName(value);
				return false;
			}
		},
		OUTPUT_PREFIX("output-prefix", Arg.REQUIRED, 'p'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setOutputPrefix(value);
				return false;
			}
		},
		OUTPUT_SUFFIX("output-sufffix", Arg.REQUIRED, 's'){
			@Override
			public boolean initialize(SampleExperiment obj, String value) {
				obj.setOutputSuffix(value);
				return false;
			}
		};

		//Everything from here down in the enum is boilerplate and should not change from
		//experiment to experiment.
		private String longName;
		private Arg required;
		private char shortChar;
		private StringBuffer sb = new StringBuffer();

		private CLIKey(String longName, Arg required, char shortChar){
			this.longName = longName;
			this.required = required;
			this.shortChar = shortChar;
		}
		public String getLongName(){return longName;}
		public char getShortChar(){return shortChar;}
		public Arg getRequiredVal(){return required;}
		public StringBuffer getBuffer(){return sb;}
		/**
		 * Perform the required initialization actions on the given field of obj with
		 * value.
		 * @param obj to initialize, must not be null
		 * @param value the value to parse to fill in a member of obj
		 * @return true if an error occurred during initiatlization
		 */
		public boolean initialize(SampleExperiment obj, String value){
			//Default, ignore the value... useful there is a Arg.NONE value.
			return false;
		}
	}

	/**
	 * This method creates an object of type SampleExperiment, and then initializes its
	 * values according to the command line arguments, and the arguments that the program
	 * is looking for according to the {@link CLIKey} enum.
	 * @param commandLineArgs
	 * @return initialized SampleExperiment object
	 * @throws Exception 
	 */
	public static SampleExperiment initializeObject(String[] commandLineArgs) throws Exception{
		//Make a list of all required flags
		Set<CLIKey> requiredFlags = new HashSet<>();
		//We need a value of Number of episodes or else this is a no go!
		requiredFlags.add(CLIKey.NUMEPS);

		SampleExperiment obj = new SampleExperiment();
		
		//*********** NOTHING CHANGES BELOW THIS LINE*********************
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
			//Got an unexpected error
			if(c == '?'){
				System.err.println("Unexpected command line parameter: "+opts.getopt());
				error = true;
				continue;
			}
			//Otherwise, getopts returns the single char flag - map back to the long key
			if(c != 0)
				c = reverseLookup.get((char)c);
			//Otherwise, getopts returns 0 if a long flag was used - pull out the long index
			else
				c = opts.getLongind();
			
			error = keys[c].initialize(obj, opts.getOptarg());
			requiredFlags.remove(keys[c]);
		}

		//Check to see if a required flag was not present:
		if(!requiredFlags.isEmpty()){
			error = true;
			for(CLIKey key : requiredFlags)
				System.err.println("Error: Required flag: --"+key.getLongName()+" (-"+key.getShortChar()+") is not specified.");
		}
		return error? null : obj;
	}
}