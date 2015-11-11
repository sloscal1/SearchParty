package core.io;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ProgramParameter {
	//Everything from here down in the enum is boilerplate and should not change from
	//experiment to experiment.
	private String longName;
	private Arg required;
	private char shortChar;

	protected ProgramParameter(String longName, Arg required, char shortChar){
		this.longName = longName;
		this.required = required;
		this.shortChar = shortChar;
	}
	
	public final String getLongName(){return longName;}
	public final char getShortChar(){return shortChar;}
	public final Arg getRequiredVal(){return required;}
	
	/**
	 * Perform the required initialization actions on the given field of obj with
	 * value.
	 * @param obj to initialize, must not be null
	 * @param value the value to parse to fill in a member of obj
	 * @return true if an error occurred during initiatlization
	 */
	public boolean process(String value, Map<ProgramParameter, Object> values){
		return false;
	}
	
	/**
	 * This method creates an object of type SampleExperiment, and then initializes its
	 * values according to the command line arguments, and the arguments that the program
	 * is looking for according to the {@link CLIKey} enum.
	 * @param commandLineArgs
	 * @return initialized SampleExperiment object
	 * @throws Exception 
	 */
	public static Map<ProgramParameter, Object> getValues(String[] commandLineArgs, List<ProgramParameter> allParams, Collection<ProgramParameter> required){
		//Make a list of all required flags
		Set<ProgramParameter> requiredFlags = new HashSet<>();
		//We need a value of Number of episodes or else this is a no go!
		requiredFlags.addAll(required);

		//*********** NOTHING CHANGES BELOW THIS LINE*********************
		//Return structure:
		Map<ProgramParameter, Object> retVals = new HashMap<>();
		//Build all of the long options to process from the command line...
		LongOpt[] options = new LongOpt[allParams.size()];
		Map<Character, Integer> reverseLookup = new HashMap<>();
		String getOptString = "";
		for(int i = 0; i < options.length; ++i){
			Arg req = allParams.get(i).getRequiredVal();
			char single = allParams.get(i).getShortChar();
			options[i] = new LongOpt(allParams.get(i).getLongName(), req.getValue(), null, single);
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

			error = allParams.get(c).process(opts.getOptarg(), retVals);
			requiredFlags.remove(allParams.get(c));
		}

		//Check to see if a required flag was not present:
		if(!requiredFlags.isEmpty()){
			error = true;
			for(ProgramParameter key : requiredFlags)
				System.err.println("Error: Required flag: --"+key.getLongName()+" (-"+key.getShortChar()+") is not specified.");
		}
		return error? null : retVals;
	}
}
