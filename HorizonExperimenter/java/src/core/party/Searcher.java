package core.party;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

import core.io.Arg;
import core.io.ProgramParameter;
import core.messages.DispatchMessages;
import core.messages.DispatchMessages.Env_Variable;
import core.messages.DispatchMessages.Experiment;
import core.messages.DispatchMessages.Machine;
import core.messages.DispatchMessages.Profile;
import core.messages.EmploySearcher;
import core.messages.EmploySearcher.Contract;
import core.messages.ExperimentResults;
import core.messages.ExperimentResults.ResultMessage;

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
	public static List<ProgramParameter> allParams = new ArrayList<>();
	static{
		allParams.add(new ProgramParameter("input-setup", Arg.REQUIRED, 'i'){
			@Override
			public boolean process(String value,
					Map<ProgramParameter, Object> values) {
				boolean error = false;
				try {
					DispatchMessages.Experiment.Builder builder = DispatchMessages.Experiment.newBuilder();
					TextFormat.merge(value, builder);
					DispatchMessages.Experiment exp = builder.build();
					values.put(this, exp);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return error;
			}
		});
		allParams.add(new ProgramParameter("contract-proto", Arg.REQUIRED, 'c'){
			@Override
			public boolean process(String value, Map<ProgramParameter, Object> values) {
				boolean error = false;
				try {
					EmploySearcher.Contract.Builder builder = EmploySearcher.Contract.newBuilder();
					TextFormat.merge(value, builder);
					EmploySearcher.Contract contract = builder.build();
					values.put(this,contract);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return error;
			}
		});
	}

	/** The list of jobs that have been submitted to processes on this machine */
	private BlockingQueue<EmploySearcher.Experiment> incomplete;
	/** The results that have been received by processes running on this machine */
	private BlockingQueue<ExperimentResults.ResultMessage> results = new LinkedBlockingQueue<ExperimentResults.ResultMessage>();
	/** The setup information to get experiments running on this machine */
	private Experiment allValues;
	/** The machine particular setup information from this machine */
	private MachineState machine;
	
	/**
	 * Receives the address:port of the Dispatcher.
	 * @throws UnknownHostException 
	 */
	public Searcher(Experiment exp, Contract msg) throws UnknownHostException{
		machine = new MachineState(exp);
		incomplete = new ArrayBlockingQueue<EmploySearcher.Experiment>(machine.numReplicates);
		final ZMQ.Context cxt = ZMQ.context(1);
		
		//This thread receives results from the local experiment processes and passes them along to the Dispatcher
		new Thread(new ResultHandler(cxt, msg)).start();

		//This thread starts experiments as they become available
		new Thread(new ExperimentHandler(cxt, msg)).start();
	}

	/**
	 * Starts up an experiment on this machine, called from ExperimentHnadler.
	 * 
	 * @author sloscal1
	 *
	 */
	private class ExperimentTask implements Callable<Integer>{
		private EmploySearcher.Experiment exp;
		boolean keepGoing = true; //Out here to allow anonymous inner class to work with it.

		private ExperimentTask(EmploySearcher.Experiment exp){
			this.exp = exp;
		}

		@Override
		public Integer call() throws UnknownHostException {
			//Set up the comms channel for this experiment, need the port to pass along to the experiment:
			ZMQ.Context context = ZMQ.context(1);
			ZMQ.Socket local = context.socket(ZMQ.PULL); 
			local.setReceiveTimeOut(10000);
			int listeningPort = local.bindToRandomPort("tcp://"+InetAddress.getLocalHost().getHostAddress());
			
			//Build up the command that we're going to run on this machine
			String[] parts = allValues.getExecutableCommand().split("\\s+");
			String[] command = new String[exp.getArgumentCount()*2+parts.length+1];
			//Break the executable name in case there is a helper application needed to run the experiment
			for(int i = 0; i < parts.length; ++i)
				command[i] = parts[i];
			//Now get all the arguments
			for(int i = 0; i < exp.getArgumentCount(); ++i){
				command[parts.length+2*i] = exp.getArgument(i).getFormalName();
				command[parts.length+2*i+1] = exp.getArgument(i).getValue();
			}
			//Finally, add the undocumented required argument TODO make it clear that the program has to get this!
			command[command.length-1] = "--searcher-port="+listeningPort;
			
			//Create the process builder
			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			for(Entry<String, String> var : machine.envVariables.entrySet())
				builder.environment().put(var.getKey(), var.getValue());
			
			//Start up a thread to unambiguously get the results from this experiment.
			new  Thread(){
				public void run() {
					for(;keepGoing;){
						byte[] msg = local.recv();
						try {
							if(msg != null)
								results.put(ResultMessage.parseFrom(msg)); //TODO may need to tag this so I know which results get put back where.
						} catch (InvalidProtocolBufferException e) {
							System.err.println("Got a bad message with arguments: "+builder.command());
							e.printStackTrace();
						} catch (InterruptedException e) {
							System.err.println("Got more results than capacity allows..."); //Should not be possible.
						}
					}
				};
			}.start();
			
			//Actually run the task:
			Process proc = null;
			int retVal = -1;
			try {
				proc = builder.start();
				Scanner scan = new Scanner(proc.getInputStream());
				while(scan.hasNextLine())
					System.out.println(Thread.currentThread().getId()+": "+scan.nextLine());
				retVal = proc.waitFor();
				scan.close();
			} catch (IOException e) {
				System.err.println("Could not start process: "+builder.command());
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Searcher was killed, so too will its things...");
				proc.destroyForcibly();
				e.printStackTrace();
			} finally{
				//Prevent the other threads from hanging on an error from this one
				incomplete.remove(exp);
				keepGoing = false;
			}
			return retVal;
		}
	}

	/**
	 * Handle pull requests for more experiments. We do not want to cache experiments on
	 * any machine, so we need to specifically request them when a process becomes available.
	 * 
	 * @author sloscal1
	 *
	 */
	private class ExperimentHandler implements Runnable{
		private ZMQ.Context cxt;
		private Contract msg;
		
		public ExperimentHandler(ZMQ.Context cxt, Contract msg) {
			this.cxt = cxt;
			this.msg = msg;
		}
		
		@Override
		public void run() {
			ZMQ.Socket experiments = cxt.socket(ZMQ.REQ);
			experiments.setReceiveTimeOut(1000);
			experiments.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getExperimentPort());
			//Now loop on Experiment type messages
			//Need to setup the executor service to run the experiments locally:
			ExecutorService exec = Executors.newFixedThreadPool(machine.numReplicates);
			List<Future<Integer>> results = new ArrayList<>();
			EmploySearcher.Experiment sentinal = EmploySearcher.Experiment.newBuilder().buildPartial();
			for(;!exec.isShutdown();){
				try {
//					System.out.println("Connected and waiting on some experiments...");
					incomplete.put(sentinal); //Block until we have a thread that's ready for a task...
					incomplete.take(); //Take the dummy sentinel off
					experiments.send("gimme");
					byte[] msg = experiments.recv(); //Get the new task
					if(msg != null){
						EmploySearcher.Experiment exp = EmploySearcher.Experiment.parseFrom(msg);
						if(!exp.getTerminal() || exp.getArgumentCount() != 0){
							incomplete.put(exp);
							results.add(exec.submit(new ExperimentTask(exp)));
						}
						if(exp.getTerminal()){
							System.out.println("Shutting down...");
							exec.shutdown();
						}
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ZMQException e){
					System.out.println("Caught...");
					if(!exec.isShutdown()) exec.shutdown();
					e.printStackTrace();
				}
			}
			//Wait for all the tasks to complete...
			for(Future<Integer> f : results)
				try {
					f.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			try {
				Thread.sleep(1000); //Give it a few seconds before ending the results thread...
				Searcher.this.machine.active = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Exp listener done");
			experiments.close();
		}
	}

	/**
	 * This class receives result messages from the experiments that are the
	 * focus of this run, and passes them along to the Dispatcher.
	 * 
	 * @author sloscal1
	 *
	 */
	private class ResultHandler implements Runnable{
		private ZMQ.Context cxt;
		private Contract msg;
		
		public ResultHandler(ZMQ.Context cxt, Contract msg) {
			this.cxt = cxt;
			this.msg = msg;
		}
		
		@Override
		public void run() {
			ZMQ.Socket results = cxt.socket(ZMQ.PUSH);
			results.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getReplyPort());
			for(;Searcher.this.machine.active;){
				try {
					ResultMessage rm = Searcher.this.results.poll(5, TimeUnit.SECONDS);
					if(rm != null)
						results.send(rm.toByteArray(), ZMQ.NOBLOCK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			results.close();
			cxt.term();
		}
	}
	
	/**
	 * Encapsulates the various setting for experiments run on this machine that
	 * will be shared across all experiments.
	 * @author sloscal1
	 *
	 */
	private class MachineState{
		private int numReplicates = 1;
		private Map<String, String> envVariables = new HashMap<>();
		private volatile boolean active = true;
		
		public MachineState(Experiment exp) throws UnknownHostException{
			allValues = exp;
			//What name is being looked for in the setup experiment object
			String localName = InetAddress.getLocalHost().getHostName();
			if(localName.contains("/"))
				localName = localName.substring(0, localName.indexOf('/'));
			System.out.println("localname is: "+localName);
			
			//See if there is a profile that corresponds to this machine:
			boolean profileFound = false;
			for(int i = 0; !profileFound && i < allValues.getProfilesCount(); ++i){
				Profile p = allValues.getProfiles(i);
				if(p.getApplicableMachinesList().contains(localName)){
					for(Env_Variable env : p.getEnvVariablesList())
						envVariables.put(env.getKey(), env.getValue());
					numReplicates = p.getReplicates();				
					profileFound = true;
				}
			}
			
			//See if there are any overrides in a machine message:
			boolean machineFound = false;
			for(int i = 0; !machineFound && i < allValues.getMachinesCount(); ++i){
				Machine m = allValues.getMachines(i);
				if(localName.equals(m.getName())){
					for(Env_Variable env : m.getEnvVariablesList())
						envVariables.put(env.getKey(), env.getValue());
					if(m.hasReplicates())
						numReplicates = m.getReplicates();
					machineFound = true;
				}
			}			
		}
	}
	
	public static void main(String[] args) throws Exception{
		Map<ProgramParameter, Object> vals = ProgramParameter.getValues(args, allParams, allParams);
		if(vals != null)
			new Searcher((Experiment)vals.get(allParams.get(0)), (Contract)vals.get(allParams.get(1)));
	}
}
