package core.party;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import com.google.protobuf.InvalidProtocolBufferException;

import core.io.Arg;
import core.messages.EmploySearcher;
import core.messages.EmploySearcher.Contract;

/**
 * This an instance of this class runs on a machine that is to
 * execute jobs. It handles starting up each of the particular
 * experiments it receives from the Dispatcher, and directing
 * results to make sure things go smoothly during execution of
 * experiments. 
 * 
 * @author sloscal1
 *
 */
public class Searcher {
	private ZMQ.Context context;
	private ZMQ.Socket basecomms;
	private List<Experiment> incomplete;
	private List<Experiment> complete;
	private int numIOThreads = 4;
	private int localPort;

	/**
	 * Receives the address:port of the Dispatcher.
	 */
	public Searcher(Contract msg){
		
		new Thread(new DispatcherTask(msg)).start();
		
	}

	private class DispatcherTask implements Runnable{
		private Contract msg;
		
		public DispatcherTask(Contract msg) {
			this.msg = msg;
		}
		
		@Override
		public void run() {
			//Need to set up the communications with the Dispatcher.
			try(ZContext ctx = new ZContext()){
				basecomms = ctx.createSocket(ZMQ.REP);
				localPort = basecomms.bindToRandomPort("tcp://127.0.0.1");
				basecomms.connect(msg.getDispatchAddress()+":"+msg.getDispatchPort());
				
				//Let the Searcher know that all is well...
				basecomms.send(msg.getSecret()+":"+localPort);
				
				//Now loop on Experiment type messages
				for(;;){
					byte[] bytes = basecomms.recv();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception{
		Map<CLArgs, Object> vals = getValues(args);
		if(vals != null)
			new Searcher((Contract)vals.get(CLArgs.CONTRACT_PROTO));
	}

	private enum CLArgs{
		CONTRACT_PROTO("contract-proto", Arg.REQUIRED, 'c'){
			@Override
			public boolean process(String value, Map<CLArgs, Object> values) {
				boolean error = false;
				try {
					values.put(this, EmploySearcher.Contract.parseFrom(value.getBytes()));
				} catch (InvalidProtocolBufferException e) {
					error = true;
					e.printStackTrace();
				}
				return error;
			}
		};
		//Everything from here down in the enum is boilerplate and should not change from
		//experiment to experiment.
		private String longName;
		private Arg required;
		private char shortChar;

		private CLArgs(String longName, Arg required, char shortChar){
			this.longName = longName;
			this.required = required;
			this.shortChar = shortChar;
		}
		public String getLongName(){return longName;}
		public char getShortChar(){return shortChar;}
		public Arg getRequiredVal(){return required;}
		/**
		 * Perform the required initialization actions on the given field of obj with
		 * value.
		 * @param obj to initialize, must not be null
		 * @param value the value to parse to fill in a member of obj
		 * @return true if an error occurred during initiatlization
		 */
		public boolean process(String value, Map<CLArgs, Object> values){return false;}
	}

	/**
	 * This method creates an object of type SampleExperiment, and then initializes its
	 * values according to the command line arguments, and the arguments that the program
	 * is looking for according to the {@link CLIKey} enum.
	 * @param commandLineArgs
	 * @return initialized SampleExperiment object
	 * @throws Exception 
	 */
	public static Map<CLArgs, Object> getValues(String[] commandLineArgs) throws Exception{
		//Make a list of all required flags
		Set<CLArgs> requiredFlags = new HashSet<>();
		//We need a value of Number of episodes or else this is a no go!
		requiredFlags.add(CLArgs.CONTRACT_PROTO);

		//*********** NOTHING CHANGES BELOW THIS LINE*********************
		//Return structure:
		Map<CLArgs, Object> retVals = new HashMap<>();
		//Build all of the long options to process from the command line...
		LongOpt[] options = new LongOpt[CLArgs.values().length];
		CLArgs[] keys = CLArgs.values();
		Map<Character, Integer> reverseLookup = new HashMap<>();
		String getOptString = "";
		for(int i = 0; i < options.length; ++i){
			Arg req = keys[i].getRequiredVal();
			char single = keys[i].getShortChar();
			options[i] = new LongOpt(keys[i].getLongName(), req.getValue(), null, single);
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

			error = keys[c].process(opts.getOptarg(), retVals);
			requiredFlags.remove(keys[c]);
		}

		//Check to see if a required flag was not present:
		if(!requiredFlags.isEmpty()){
			error = true;
			for(CLArgs key : requiredFlags)
				System.err.println("Error: Required flag: --"+key.getLongName()+" (-"+key.getShortChar()+") is not specified.");
		}
		return error? null : retVals;
	}
}
