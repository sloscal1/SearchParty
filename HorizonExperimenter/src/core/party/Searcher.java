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

import org.zeromq.ZMQ;

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
import core.messages.EmploySearcher.Argument;
import core.messages.EmploySearcher.Contract;
import core.messages.ExperimentResults;
import core.messages.ExperimentResults.Result;
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

	private BlockingQueue<EmploySearcher.Experiment> incomplete;
	private BlockingQueue<ExperimentResults.ResultMessage> results = new LinkedBlockingQueue<ExperimentResults.ResultMessage>();
	//	private List<Experiment> complete;
	private Experiment allValues;
	private Map<String, String> envVars = new HashMap<>();

	/**
	 * Receives the address:port of the Dispatcher.
	 * @throws UnknownHostException 
	 */
	public Searcher(Experiment exp, Contract msg) throws UnknownHostException{
		allValues = exp;
		incomplete = new ArrayBlockingQueue<EmploySearcher.Experiment>(msg.getNumReplicates());
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
					envVars.put(env.getKey(), env.getValue());
				profileFound = true;
			}
		}
		//See if there are any overrides:
		boolean machineFound = false;
		for(int i = 0; !machineFound && i < allValues.getMachinesCount(); ++i){
			Machine m = allValues.getMachines(i);
			if(localName.equals(m.getName())){
				for(Env_Variable env : m.getEnvVariablesList())
					envVars.put(env.getKey(), env.getValue());
				machineFound = true;
			}
		}
		final ZMQ.Context cxt = ZMQ.context(1);
		//This thread receives results from the local experiment processes and passes them along to the Dispatcher
		new Thread(new Runnable(){
			@Override
			public void run() {
				ZMQ.Socket results = cxt.socket(ZMQ.PUSH);
				results.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getReplyPort());
				for(boolean term = false; !term;){
					try {
						System.out.println("Connected and waiting on results...");
						ResultMessage rm = Searcher.this.results.take();
						results.send(rm.toByteArray(), ZMQ.NOBLOCK);
						for(Result r : rm.getReportedValueList())
							term |= "TERM_".equals(r.getName()) && "TRUE".equals(r.getValue());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Finished sending results...");
				results.close();
				cxt.term();
			}
		}).start();

		//This thread gets experiments as they become available
		new Thread(new Runnable() {
			@Override
			public void run() {
				ZMQ.Socket experiments = cxt.socket(ZMQ.REQ);
				experiments.setReceiveTimeOut(1000);
				experiments.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getExperimentPort());
				//Now loop on Experiment type messages
				//Need to setup the executor service to run the experiments locally:	
				ExecutorService exec = Executors.newFixedThreadPool(msg.getNumReplicates());
				List<Future<Integer>> results = new ArrayList<>();
				EmploySearcher.Experiment sentinal = EmploySearcher.Experiment.newBuilder().buildPartial();
				for(;!exec.isShutdown();){
					try {
						System.out.println("Connected and waiting on some experiments...");
						incomplete.put(sentinal); //Block until we have a thread that's ready for a task...
						incomplete.take(); //Take the dummy sentinal off
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
					Searcher.this.results.put(ExperimentResults.ResultMessage.newBuilder().addReportedValue(
							Result.newBuilder()
							.setName("TERM_")
							.setValue("TRUE").build()).build());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Exp listener done");
				experiments.close();
			}
		}).start();
	}

	private class ExperimentTask implements Callable<Integer>{
		private EmploySearcher.Experiment exp;
		boolean keepGoing = true; //Out here to allow anonymous inner class to work with it.

		private ExperimentTask(EmploySearcher.Experiment exp){
			this.exp = exp;
		}

		@Override
		public Integer call() throws UnknownHostException {
			//Set up the comms channel for this experiment:
			//TODO pull this out to have fixed sockets declared all at once?
			ZMQ.Context context = ZMQ.context(1);
			ZMQ.Socket local = context.socket(ZMQ.PULL); //TODO Maybe poll?
			local.setReceiveTimeOut(10000); //TODO make this an variable somewhere
			int listeningPort = local.bindToRandomPort("tcp://"+InetAddress.getLocalHost().getHostAddress());
			new  Thread(){
				public void run() {
					for(;keepGoing;){
						byte[] msg = local.recv();
						try {
							if(msg != null)
								results.put(ResultMessage.parseFrom(msg));
						} catch (InvalidProtocolBufferException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.out.println("Out of the execution loop.");
				};
			}.start();

			//TODO This should be changed in the prototxt, shouldn't have hard-code split on Java
			String[] parts = allValues.getExecutableCommand().split("\\s+");
			String[] command = new String[exp.getArgumentCount()*2+parts.length+1];
			for(int i = 0; i < parts.length; ++i)
				command[i] = parts[i];
			int pos = parts.length;
			for(Argument arg : exp.getArgumentList()){
				command[pos++] = arg.getFormalName();
				command[pos++] = arg.getValue();
			}
			command[command.length-1] = "--searcher-port="+listeningPort; //TODO undocumented requirement!
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			//Set up the environment for this process
			for(Entry<String, String> var : envVars.entrySet())
				builder.environment().put(var.getKey(), var.getValue());

			System.out.println("Running command: "+builder.command());

			Process proc = null;
			int retVal = -1;
			try {
				proc = builder.start();
				Scanner scan = new Scanner(proc.getInputStream());
				while(scan.hasNextLine()){
					System.out.println(Thread.currentThread().getId()+": "+scan.nextLine());
				}
				retVal = proc.waitFor();
				scan.close();
				incomplete.remove(exp);
				keepGoing = false;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Searcher was killed, so too will its things...");
				proc.destroyForcibly();
				e.printStackTrace();
			}
			System.out.println("Finished call...");
			return retVal;
		}
	}

	public static void main(String[] args) throws Exception{
		Map<ProgramParameter, Object> vals = ProgramParameter.getValues(args, allParams, allParams);
		if(vals != null)
			new Searcher((Experiment)vals.get(allParams.get(0)), (Contract)vals.get(allParams.get(1)));
	}
}
