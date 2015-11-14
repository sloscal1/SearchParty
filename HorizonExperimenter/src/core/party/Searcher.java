package core.party;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.zeromq.ZMQ;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

import core.io.Arg;
import core.io.ProgramParameter;
import core.messages.EmploySearcher;
import core.messages.EmploySearcher.Contract;
import core.messages.EmploySearcher.Experiment.Env_Variable;
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

	private ZMQ.Context context;
	private ZMQ.Socket basecomms;
	//	private List<Experiment> incomplete;
	//	private List<Experiment> complete;
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
			System.out.println("Pushing out a message...");
			try(ZMQ.Context ctx = ZMQ.context(1)){
				basecomms = ctx.socket(ZMQ.REQ);
				try {
					localPort = basecomms.bindToRandomPort("tcp://"+InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
					localPort = basecomms.bindToRandomPort("tcp://127.0.0.1");
				}
				basecomms.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getDispatchPort());
				//TODO Need to get the git information as well...
				//Make a temp file too.

				//Let the Searcher know that all is well...
				basecomms.send(EmploySearcher.Response.newBuilder()
						.setSearcherPort(localPort)
						.setSecret(msg.getSecret())
						.build().toByteArray(), ZMQ.NOBLOCK);

				//Now loop on Experiment type messages
				//Need to setup the executor service to run the experiments locally:	
				ExecutorService exec = Executors.newFixedThreadPool(msg.getNumReplicates());
				System.out.println("Running the service loop now...");
				new Timer("Killer", false).schedule(
						new TimerTask(){
							@Override
							public void run() {
								System.out.println("Kill...");
								System.exit(0);
							}
						}, 
						30000
						);
				for(;;){
					byte[] msg = basecomms.recv();
					System.out.println("SEARCHER RECVD!");
					try {
						EmploySearcher.Experiment exp = EmploySearcher.Experiment.parseFrom(msg);
						//TODO Need to track experiments that failed.
						//TODO Need to set up some sockets or something to receive results.
						exec.submit(new ExperimentTask(exp));
					} catch (InvalidProtocolBufferException e) {
						System.err.println("Unknown message was received. Ignoring...");
					}
				}
			}
		}
	}

	private class ExperimentTask implements Callable<Integer>{
		private EmploySearcher.Experiment exp;
		boolean keepGoing = true; //Out here to allow anonymous inner class to work with it.
		
		private ExperimentTask(EmploySearcher.Experiment exp){
			this.exp = exp;
		}

		@Override
		public Integer call() throws UnknownHostException {
			System.out.println("Running the task...");
			//Set up the comms channel for this experiment:
			//TODO pull this out to have fixed sockets declared all at once?
			ZMQ.Context context = ZMQ.context(1);
			System.out.println("got a context...");
			ZMQ.Socket local = context.socket(ZMQ.REQ); //TODO Maybe poll?
			System.out.println("got a socket...");
			local.setReceiveTimeOut(10000); //TODO make this an variable somewhere
			System.out.println("set the timeout");
			int listeningPort = local.bindToRandomPort("tcp://"+InetAddress.getLocalHost().getHostAddress());
			System.out.println("bount to port: "+listeningPort);
			new  Thread(){
				public void run() {
					System.out.println("Hanging out for the results of the test class...");
					for(;keepGoing;){
						byte[] msg = local.recv();
						try {
							ResultMessage rm = ResultMessage.parseFrom(msg);
							System.out.println(rm);
						} catch (InvalidProtocolBufferException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.out.println("Out of the execution loop.");
				};
			}.start();
			
			System.out.println("Working through parameters...");
			//Get the command set
			String[] command = new String[exp.getArgumentCount()+2];
			command[0] = exp.getProgramName();
			int pos = 1;
			for(String arg : exp.getArgumentList())
				command[pos++] = arg;
			command[command.length-1] = "--searcher-port="+listeningPort; //TODO undocumented requirement!
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectError();
			//Set up the environment for this process
			Map<String, String> env = builder.environment();
			for(Env_Variable var : exp.getEnvironmentList())
				env.put(var.getKey(), var.getValue());

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
				keepGoing = false;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Searcher was killed, so too will its things...");
				proc.destroyForcibly();
				e.printStackTrace();
			}
			return retVal;
		}
	}

	public static void main(String[] args) throws Exception{
		Map<ProgramParameter, Object> vals = ProgramParameter.getValues(args, allParams, allParams);
		if(vals != null)
			new Searcher((Contract)vals.get(allParams.get(0)));
	}
}
