/*
Copyright 2015 Steven Loscalzo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package core.party;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zeromq.ZMQ;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import core.io.Arg;
import core.io.ProgramParameter;
import core.messages.DispatcherCentricMessages.Machine;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.ExperimentResults;
import core.messages.ExperimentResults.Result;
import core.messages.SearcherCentricMessages.Argument;
import core.messages.SearcherCentricMessages.Contract;
import core.messages.SearcherCentricMessages.RunSettings;

public class Dispatcher {
	private Setup inputExp;
	//	private Result databaseSetup;
	private int expPort;
	private int resultPort;
	private int readyPorts = 0;
	private Lock portLock = new ReentrantLock();
	private Condition portReady = portLock.newCondition();
	private String localIPv4; 
	private Semaphore activeMachines;

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
					Setup.Builder builder = Setup.newBuilder();
					TextFormat.merge(text.toString(), builder);
					Setup e = builder.build();
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
				//				boolean error = true;
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
	public Dispatcher(Setup exp, Result res){
		this.inputExp = exp;
		this.activeMachines = new Semaphore(exp.getExpMachineCount());

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
				Queue<RunSettings> runs = new LinkedList<>();
				runs.add(RunSettings.newBuilder()
						.addArgument(Argument.newBuilder()
								.setFormalName("--episodes")
								.setValue("25").build())
								.build());
				runs.add(RunSettings.newBuilder()
						.addArgument(Argument.newBuilder()
								.setFormalName("--episodes")
								.setValue("5").build())
								.addArgument(Argument.newBuilder()
										.setFormalName("--lambda")
										.setValue("0.005").build())
										.build());
				for(int i = 0; i < 10; ++i)
					runs.add(runs.peek());

				experiments.setReceiveTimeOut(2000);
				for(;activeMachines.availablePermits() != 0;){
					byte[] msg = experiments.recv();
					if(msg != null){
						//Got a request, if there are more left and a running experimenter machine, send one
						if(!runs.isEmpty())
							experiments.send(runs.remove().toByteArray(), ZMQ.NOBLOCK);
						else{
							System.out.println("Sending a term exp");
							experiments.send(RunSettings.newBuilder()
									.setTerminal(true).build().toByteArray(), ZMQ.NOBLOCK);
						}
					}
				}
				System.out.println("All machines know we're out of experiments...");
				experiments.close();
			}
		}).start();

		//Setup results listener
		new Thread(new Runnable(){
			public void run() {
				ZMQ.Socket results = cxt.socket(ZMQ.PULL);
				portLock.lock();
				resultPort = results.bindToRandomPort("tcp://"+localIPv4);
				results.setReceiveTimeOut(2000);
				++readyPorts;
				portReady.signal();
				portLock.unlock();
				for(;activeMachines.availablePermits() != 0;){
					try {
						byte[] msg = results.recv();
						if(res != null){
							ExperimentResults.ResultMessage.parseFrom(msg);
							//							System.out.println("");
						}
						//TODO push this information to the database
					} catch (InvalidProtocolBufferException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Terminated receiver...");
			}
		}).start();

		Set<core.messages.DispatcherCentricMessages.Machine> machines = new HashSet<>(exp.getExpMachineList());
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
					MachineState state = new MachineState(exp, m.getName());
					//Signal the remote machine to start its Searcher
					String user = m.hasUsername() ? m.getUsername() : defaultUser;
					//Need to find where searchparty.jar is on the remote machine:
					String[] cpEntries = state.getEnvironmentVariables().get("CLASSPATH").split("[;:]");
					String spLoc = null;
					for(int i = 0; spLoc == null && i < cpEntries.length; ++i)
						if(cpEntries[i].contains("searchparty.jar"))
							spLoc = cpEntries[i];
					ProcessBuilder builder = new ProcessBuilder("ssh", user+"@"+m.getName(),
							"java -jar "+spLoc+" --searcher -i \""+inputExp.toString().replaceAll("\n", " ").replaceAll("\"", "\\\\\"")
							+ "\" -c \""+Contract.newBuilder().setDispatchAddress(localIPv4)
							.setExperimentPort(expPort)
							.setReplyPort(resultPort)
							.build().toString().replaceAll("\n", " ").replaceAll("\"", "\\\\\"")+"\"");
					builder.redirectErrorStream(true);
					for(Entry<String, String> var : state.getEnvironmentVariables().entrySet())
						builder.environment().put(var.getKey(), var.getValue());

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
					} finally{
						try {
							activeMachines.acquire();
							System.out.println("Acquired.. "+activeMachines.availablePermits());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}


	public static void main(String[] args){
		//Takes the setup message and results message.
		Map<ProgramParameter, Object> cli = ProgramParameter.getValues(args, params, params);
		new Dispatcher((Setup)cli.get(params.get(0)), 
				(Result)cli.get(params.get(1)));
	}



	//TODO Check to see if this a continuation

	//Figure out all the experiments

	//Set up the data base
	//.Check to see if it already exists.
	//.Check to see if there are runs that match this codebase and parameter settings
	//TODO Check in this code and see if there are any changes between it other experiments.
	//TODO Maybe it would be easier to simply request the user to specify an experiment to continue (from DB)
}