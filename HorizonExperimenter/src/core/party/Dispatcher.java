package core.party;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zeromq.ZMQ;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import core.io.Arg;
import core.io.ProgramParameter;
import core.io.Results;
import core.messages.DispatchMessages;
import core.messages.DispatchMessages.Machine;
import core.messages.EmploySearcher;
import core.messages.EmploySearcher.Contract;
import core.messages.EmploySearcher.Experiment;
import core.messages.EmploySearcher.Response;
import core.messages.ExperimentResults;
import core.messages.ExperimentResults.ResultMessage;

public class Dispatcher {
	private DispatchMessages.Experiment inputExp;
	private Results.Result databaseSetup;
	private int numListeningThreads = 1;
	private Map<Machine, SearcherInfo> remoteInfo = new HashMap<>();
	private Random rand = new Random();
	private Lock portLock = new ReentrantLock();
	private Condition portCond = portLock.newCondition();

	public static List<ProgramParameter> params = new ArrayList<ProgramParameter>();
	static{
		params.add(new ProgramParameter("experiment-file", Arg.REQUIRED, 'i'){
			@Override
			public boolean process(String value,
					Map<ProgramParameter, Object> values) {
				boolean error = true;
				try {
					StringBuilder text = new StringBuilder();
					try(Scanner in = new Scanner(new File(value))){
						while(in.hasNextLine())
							text.append(in.nextLine());
					}
					DispatchMessages.Experiment.Builder builder = DispatchMessages.Experiment.newBuilder();
					TextFormat.merge(text.toString(), builder);
					DispatchMessages.Experiment e = builder.build();
					values.put(this, e);
					error = false;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return error;
			}
		});
		params.add(new ProgramParameter("output-file", Arg.REQUIRED, 'o'){
			@Override
			public boolean process(String value,
					Map<ProgramParameter, Object> values) {
				boolean error = true;
				//TODO get this working...
//				try {
//					DispatchMessages.Experiment e = DispatchMessages.Experiment.parseFrom(new FileInputStream(new File(value)));
//					values.put(this, e);
//					error = false;
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				return false;
			}
		});
	}

	private class DispatchServerTask implements Runnable{
		private Machine machine;
		public DispatchServerTask(Machine machine){
			this.machine = machine;
		}

		public void run(){
			ZMQ.Context context = ZMQ.context(numListeningThreads);
			ZMQ.Socket main = context.socket(ZMQ.REP);
			int port;
			try {
				port = main.bindToRandomPort("tcp://"+InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				port = main.bindToRandomPort("tcp://127.0.0.1");
			} //TODO, factor out
			//TODO check error
			portLock.lock();
			remoteInfo.get(machine).listeningPort = port;
			portCond.signal();
			portLock.unlock();
			
			new Timer("Killer", false).schedule(
					new TimerTask(){
						@Override
						public void run() {
							System.out.println("Kill...");
							System.exit(0);
						}
					}, 
					35000
			);
			
			//Dispatcher is ready and waiting for messages from Searcher
			//First message should be the Contract Response:
			boolean ready = false;
			try {
				Response r = EmploySearcher.Response.parseFrom(main.recv());
				
				if(r.hasSecret() && r.getSecret() != remoteInfo.get(machine).secret)
					throw new InvalidProtocolBufferException("Incorrect secret");
				main.connect("tcp://"+machine.getName()+":"+r.getSearcherPort());
				ready = true;
			} catch (InvalidProtocolBufferException e) {
				System.err.println("Cannot use: "+machine.getName()+", incorrect response.");
				e.printStackTrace();
			}
			
			//The rest of the messages should be about sending messages to the 
			System.out.println("Now we're going to enter the comms loop");
//			for(;ready;){
				//TODO this experiment comes from the generated experiment list somewhere
				Experiment exp = Experiment.newBuilder()
									.setProgramName("java -cp bin drivers.SampleExperiment")
									.addArgument("--episodes=25")
									.build();
				//Need to keep sending experiments for each replicate on that machine:
				main.send(exp.toByteArray(), ZMQ.NOBLOCK);
				byte[] msg = main.recv();
				try {
					ResultMessage rm = ExperimentResults.ResultMessage.parseFrom(msg);
					System.out.println(rm);
				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
		}
	}

	/**
	 * Create a Dispatcher from the given configuration objects.
	 * @param exp
	 * @param res
	 */
	public Dispatcher(DispatchMessages.Experiment exp, Results.Result res){
		this.inputExp = exp;
		this.databaseSetup = res;
		Set<Machine> machines = new HashSet<>(exp.getMachinesList());
		String defaultUser = System.getProperty("user.name"); //get current user
		String localIPv4;
		try {
			localIPv4 = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			System.err.println("Cannot get Dispatcher IPv4, attempting to use localhost.");
			localIPv4 = "127.0.0.1";
		} 
		for(Machine m : machines){
			//Start up the server for this machine
			remoteInfo.put(m, new SearcherInfo(rand.nextLong()));
			new Thread(new DispatchServerTask(m)).start();
			//Need to wait for the server thread to start listening on an available port...
			boolean success = false;
			portLock.lock();
			try {
				success = portCond.await(2, TimeUnit.SECONDS);
				if(!success)
					throw new InterruptedException();
			} catch (InterruptedException e1) {
				System.err.println("Could not start Dispatcher server for machine "+m.getName()+" in a reasonable time. Skipping.");
				e1.printStackTrace();
			}
			portLock.unlock();

			if(success){
				//Make up the search contract for this machine:
				Contract c = Contract.newBuilder()
						.setDispatchAddress(localIPv4)
						.setDispatchPort(remoteInfo.get(m).listeningPort)
						.setNumReplicates(m.getReplicates())
						.setSecret(remoteInfo.get(m).secret)
						.build(); //TASK Do we want explicit local option?

				//Signal the remote machine to start its Searcher
				String user = m.hasUsername() ? m.getUsername() : defaultUser;
				ProcessBuilder builder = new ProcessBuilder("ssh", user+"@"+m.getName(),
								"java -jar "+exp.getSearchPartyPath()+File.separator+"searchparty.jar -c \""+c.toString().replaceAll("\n", " ").replaceAll("\"", "\\\\\"")+"\"");
				builder.redirectError();
				try {
					Process p = builder.start();
					Scanner output = new Scanner(p.getInputStream());
					while(output.hasNextLine())
						System.out.println("SENDER: "+output.nextLine());
					p.waitFor();
					output.close();
				} catch (IOException e) {
					System.err.println("Cannot start Searcher process on "+m.getName()+": ");
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private class SearcherInfo {
		private int listeningPort;
		private long secret;

		private SearcherInfo(long secret){
			this.secret = secret;
		}
	}

	public static void main(String[] args){
		//Takes the setup message and results message.
		Map<ProgramParameter, Object> cli = ProgramParameter.getValues(args, params, params);
		new Dispatcher((DispatchMessages.Experiment)cli.get(params.get(0)), 
				(Results.Result)cli.get(params.get(1)));
	}



	//TODO Check to see if this a continuation

	//Figure out all the experiments

	//Set up the data base
	//.Check to see if it already exists.
	//.Check to see if there are runs that match this codebase and parameter settings
	//TODO Check in this code and see if there are any changes between it other experiments.
	//TODO Maybe it would be easier to simply request the user to specify an experiment to continue (from DB)
}
