package core.party;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import core.messages.DispatcherCentricMessages.EnvVariable;
import core.messages.DispatcherCentricMessages.Machine;
import core.messages.DispatcherCentricMessages.Profile;
import core.messages.DispatcherCentricMessages.Setup;

/**
 * Encapsulates the various setting for experiments run on this machine that
 * will be shared across all experiments.
 * @author sloscal1
 *
 */
public class MachineState{
	private int numReplicates = 1;
	private Map<String, String> envVariables = new HashMap<>();
	private volatile boolean active = true;
	private String execCommand;
	private String localName;
	
	public MachineState(Setup exp) throws UnknownHostException{
		this(exp, InetAddress.getLocalHost().getHostName());
	}
	
	public MachineState(Setup exp, String name){
		this.execCommand = exp.getExecutableCommand();
		//What name is being looked for in the setup experiment object
		if(name.contains("/"))
			name = name.substring(0, name.indexOf('/'));
		System.out.println("localname is: "+name);
		this.localName = name;
		
		//See if there is a profile that corresponds to this machine:
		boolean profileFound = false;
		for(int i = 0; !profileFound && i < exp.getProfileCount(); ++i){
			Profile p = exp.getProfile(i);
			if(p.getApplicableMachinesList().contains(name)){
				for(EnvVariable env : p.getEnvVariablesList())
					envVariables.put(env.getKey(), env.getValue());
				numReplicates = p.getReplicates();				
				profileFound = true;
			}
		}
		
		//See if there are any overrides in a machine message:
		boolean machineFound = false;
		for(int i = 0; !machineFound && i < exp.getExpMachineCount(); ++i){
			Machine m = exp.getExpMachine(i);
			if(name.equals(m.getName())){
				for(EnvVariable env : m.getEnvVariableList())
					envVariables.put(env.getKey(), env.getValue());
				if(m.hasReplicates())
					numReplicates = m.getReplicates();
				machineFound = true;
			}
		}			
	}
	
	public String getLocalName(){
		return localName;
	}
	
	public int getNumReplicates(){
		return numReplicates;
	}
	
	public Map<String, String> getEnvironmentVariables(){
		return Collections.unmodifiableMap(envVariables);
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void	setActive(boolean active){
		this.active = active;
	}

	public String getExecutableCommand() {
		return execCommand;
	}
}