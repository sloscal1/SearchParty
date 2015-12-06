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
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.ExperimentResults;
import core.messages.ExperimentResults.ResultMessage;
import core.messages.SearcherCentricMessages.Contract;
import core.messages.SearcherCentricMessages.RunSettings;

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
					Setup.Builder builder = Setup.newBuilder();
					TextFormat.merge(value, builder);
					Setup exp = builder.build();
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
					Contract.Builder builder = Contract.newBuilder();
					TextFormat.merge(value, builder);
					Contract contract = builder.build();
					values.put(this,contract);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return error;
			}
		});
	}
	/** Generating variable for (JVM/machine local) UIDs */
	private static long UIDgenerator = 0;
	/** The list of jobs that have been submitted to processes on this machine */
	private BlockingQueue<RunSettings> incomplete;
	/** The results that have been received by processes running on this machine */
	private BlockingQueue<ExperimentResults.ResultMessage> results = new LinkedBlockingQueue<ExperimentResults.ResultMessage>();
	/** The machine particular setup information from this machine */
	private MachineState machineState;

	/**
	 * Receives the address:port of the Dispatcher.
	 * @throws UnknownHostException 
	 */
	public Searcher(Setup exp, Contract msg) throws UnknownHostException{
		machineState = new MachineState(exp);
		System.out.println(InetAddress.getLocalHost().getHostName()+": "+machineState.getNumReplicates());
		incomplete = new ArrayBlockingQueue<RunSettings>(machineState.getNumReplicates());
		final ZMQ.Context cxt = ZMQ.context(1);
		
		Thread results = null;
		Thread experiments = null;
		try{
			//This thread receives results from the local experiment processes and passes them along to the Dispatcher
			results = new Thread(new ResultHandler(cxt, msg));
			results.start();
			
			//This thread starts experiments as they become available
			experiments = new Thread(new ExperimentHandler(cxt, msg));
			experiments.start();
			
			results.join();
			experiments.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			if(cxt != null) cxt.term();
		}
	}

	/**
	 * @return a UID.
	 */
	public synchronized static long nextUID(){
		return UIDgenerator++;
	}

	/**
	 * Starts up an experiment on this machine, called from ExperimentHnadler.
	 * 
	 * @author sloscal1
	 *
	 */
	private class ExperimentTask implements Callable<Integer>{
		private RunSettings exp;
		boolean keepGoing = true; //Out here to allow anonymous inner class to work with it.

		private ExperimentTask(RunSettings exp){
			this.exp = exp;
		}

		@Override
		public Integer call() throws UnknownHostException {
			//Set up the comms channel for this experiment, need the port to pass along to the experiment:
			ZMQ.Context context = ZMQ.context(1);
			final ZMQ.Socket local = context.socket(ZMQ.PULL); 
			local.setReceiveTimeOut(10000);
			int listeningPort = local.bindToRandomPort("tcp://"+InetAddress.getLocalHost().getHostAddress());
			System.out.println("Searcher bound to port: "+listeningPort);

			//Build up the command that we're going to run on this machine
			String[] parts = machineState.getExecutableCommand().split("\\s+");
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
			for(Entry<String, String> var : machineState.getEnvironmentVariables().entrySet())
				builder.environment().put(var.getKey(), var.getValue());
			//Set the working directory
			builder.directory(new File(machineState.getWorkingDir()));

			//Experiment UID
			final long uid = nextUID();
			//Start up a thread to unambiguously get the results from this experiment.
			new  Thread(){
				public void run() {
					try{
					for(;keepGoing;){
						byte[] msg = local.recv();
						try {
							if(msg != null){
								//Modify the message with it's local info (running machine name, local uid, time completed)
								ResultMessage rm = ResultMessage.parseFrom(msg);
								rm = ResultMessage.newBuilder(rm).setMachineName("'"+machineState.getLocalName()+"'")
										.setUid(uid)
										.setTimestamp(new GregorianCalendar().getTime().getTime()).build();
								results.put(rm);
							}
						} catch (InvalidProtocolBufferException e) {
							System.err.println("Got a bad message with arguments: "+builder.command());
							e.printStackTrace();
						} catch (InterruptedException e) {
							System.err.println("Got more results than capacity allows..."); //Should not be possible.
						}
					}
					}finally{
						if(local != null) local.close();
					}
				};
			}.start();

			//Actually run the task:
			Process proc = null;
			int retVal = -1;
			try {
				//				System.out.println(builder.command());
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
			ZMQ.Socket experiments = null;
			try{
				experiments = cxt.socket(ZMQ.REQ);

				experiments.setReceiveTimeOut(1000);
				System.out.println("Searcher connecting to Dispatcher experiment sender at: tcp://"+msg.getDispatchAddress()+":"+msg.getExperimentPort());
				experiments.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getExperimentPort());
				//Now loop on Experiment type messages
				//Need to setup the executor service to run the experiments locally:
				ExecutorService exec = Executors.newFixedThreadPool(machineState.getNumReplicates());
				List<Future<Integer>> results = new ArrayList<>();
				RunSettings sentinal = RunSettings.newBuilder().buildPartial();
				for(;!exec.isShutdown();){
					try {
						//					System.out.println("Connected and waiting on some experiments...");
						incomplete.put(sentinal); //Block until we have a thread that's ready for a task...
						incomplete.take(); //Take the dummy sentinel off
						experiments.send("gimme");
						byte[] msg = experiments.recv(); //Get the new task
						if(msg != null){
							RunSettings exp = RunSettings.parseFrom(msg);
							if(!exp.getTerminal() || exp.getArgumentCount() != 0){
								incomplete.put(exp);
								results.add(exec.submit(new ExperimentTask(exp)));
							}
							if(exp.getTerminal()){
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
						e.printStackTrace();
						if(!exec.isShutdown()) exec.shutdown();
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
						System.out.println("Does this not get propagated back???");
						e.printStackTrace();
					}	
				try {
					Thread.sleep(1000); //Give it a few seconds before ending the results thread...
					Searcher.this.machineState.setActive(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Exp listener done");
			}finally{
				if(experiments != null) experiments.close();
			}
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
			ZMQ.Socket results = null;
			try{
				results = cxt.socket(ZMQ.PUSH);
				System.out.println("Searcher connecting to Dispatcher Result Listener at: tcp://"+msg.getDispatchAddress()+":"+msg.getReplyPort());
				results.connect("tcp://"+msg.getDispatchAddress()+":"+msg.getReplyPort());
				for(;Searcher.this.machineState.isActive();){
					try {
						ResultMessage rm = Searcher.this.results.poll(50, TimeUnit.SECONDS);
						if(rm != null)
							results.send(rm.toByteArray(), ZMQ.NOBLOCK);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}finally{
				if(results != null) results.close();
			}
		}
	}

	public static void main(String[] args) throws Exception{
		Map<ProgramParameter, Object> vals = ProgramParameter.getValues(args, allParams, allParams);
		if(vals != null)
			new Searcher((Setup)vals.get(allParams.get(0)), (Contract)vals.get(allParams.get(1)));
	}
}
