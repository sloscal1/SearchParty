package core.party;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
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
import core.messages.EmploySearcher.Experiment;
import core.messages.ExperimentResults;
import core.messages.ExperimentResults.Result;
import core.messages.ExperimentResults.ResultMessage;

public class Dispatcher {
	private DispatchMessages.Experiment inputExp;
	private Results.Result databaseSetup;
	private int numListeningThreads = 1;
	private int expPort;
	private int resultPort;
	private int readyPorts = 0;
	private Lock portLock = new ReentrantLock();
	private Condition portReady = portLock.newCondition();
	private String localIPv4;

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

	/**
	 * Create a Dispatcher from the given configuration objects.
	 * @param exp
	 * @param res
	 */
	public Dispatcher(DispatchMessages.Experiment exp, Results.Result res){
		this.inputExp = exp;
		this.databaseSetup = res;

		try {
			localIPv4 = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			System.err.println("Cannot get Dispatcher IPv4, attempting to use localhost.");
			localIPv4 = "127.0.0.1";
		} 

		final ZMQ.Context cxt = ZMQ.context(1);
		//Setup experiment request socket
		new Thread(new Runnable(){
			@Override
			public void run() {
				ZMQ.Socket experiments = cxt.socket(ZMQ.REP);
				portLock.lock();
				expPort = experiments.bindToRandomPort("tcp://"+localIPv4);
				++readyPorts;
				portReady.signal();
				portLock.unlock();
				//TODO need an experiment generating function...
				System.out.println("Waiting for an EXP message:");
				String msg = new String(experiments.recv());
				System.out.println("Got an Exp message: "+msg);
				experiments.send(Experiment.newBuilder()
						.addArgument(core.messages.EmploySearcher.Argument.newBuilder()
								.setFormalName("--episodes")
								.setValue("25").build())
								.build().toByteArray(), ZMQ.NOBLOCK);
				experiments.recv();
				experiments.send(Experiment.newBuilder()
						.addArgument(core.messages.EmploySearcher.Argument.newBuilder()
								.setFormalName("--episodes")
								.setValue("5").build())
								.addArgument(core.messages.EmploySearcher.Argument.newBuilder()
										.setFormalName("--lambda")
										.setValue("0.005").build())
										.build().toByteArray(), ZMQ.NOBLOCK);
				//TODO 
				experiments.recv();
				experiments.send(Experiment.newBuilder()
						.setTerminal(true).build().toByteArray(), ZMQ.NOBLOCK);
				//Should close experiemnts...
			}
		}).start();

		//Setup results listener
		new Thread(new Runnable(){
			public void run() {
				ZMQ.Socket results = cxt.socket(ZMQ.PULL);
				portLock.lock();
				resultPort = results.bindToRandomPort("tcp://"+localIPv4);
				++readyPorts;
				portReady.signal();
				portLock.unlock();
				for(boolean term = false;!term;){
					try {
						ResultMessage res = ExperimentResults.ResultMessage.parseFrom(results.recv());
						for(Result r : res.getReportedValueList())
							term |= "TERM_".equals(r.getName()) && "TRUE".equals(r.getValue());
						//TODO push this information to the database
					} catch (InvalidProtocolBufferException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Terminated receiver...");
			}
		}).start();

		Set<Machine> machines = new HashSet<>(exp.getMachinesList());
		String defaultUser = System.getProperty("user.name"); //get current user
		//Wait for the binding to finish
		portLock.lock();
		while(readyPorts != 2)
			try {
				portReady.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			}
		portLock.unlock();

		for(Machine m : machines){
			new Thread(new Runnable(){
				public void run() {
					System.out.println("Machine: "+m);
					//Signal the remote machine to start its Searcher
					String user = m.hasUsername() ? m.getUsername() : defaultUser;
					ProcessBuilder builder = new ProcessBuilder("ssh", user+"@"+m.getName(),
							"java -jar "+exp.getSearchPartyPath()+File.separator+"searchparty.jar --searcher -i \""+inputExp.toString().replaceAll("\n", " ").replaceAll("\"", "\\\\\"")
							+ "\" -c \""+EmploySearcher.Contract.newBuilder().setDispatchAddress(localIPv4)
							.setExperimentPort(expPort)
							.setReplyPort(resultPort)
							.build().toString().replaceAll("\n", " ").replaceAll("\"", "\\\\\"")+"\"");
					builder.redirectErrorStream(true);
					try {
						Process p = builder.start();
						Scanner output = new Scanner(p.getInputStream());
						while(output.hasNextLine())
							System.out.println("SENDER: "+Thread.currentThread().getId()+" "+output.nextLine());
						p.waitFor();
						output.close();
						System.out.println("The Searcher: "+m.getName()+" terminated.");
					} catch (IOException e) {
						System.err.println("Cannot start Searcher process on "+m.getName()+": ");
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
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
