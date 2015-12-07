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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

import core.io.Arg;
import core.io.ProgramParameter;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.SearcherCentricMessages.Contract;

public class SearcherShell {
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

	public static void main(String[] args) throws IOException {
		Map<ProgramParameter, Object> vals = ProgramParameter.getValues(args, allParams, allParams);
		if(vals != null){
			MachineState state = new MachineState((Setup)vals.get(allParams.get(0)));
			
			ProcessBuilder pb = new ProcessBuilder("java", "core.party.Searcher", args[0], args[1], args[2], args[3]);
			pb.redirectErrorStream(true);

			pb.directory(new File(state.getWorkingDir()));
			
			Map<String, String> envVars = state.getEnvironmentVariables();
			for(String key : envVars.keySet())
				pb.environment().put(key, envVars.get(key));
			
			Process proc = pb.start();
			Scanner in = new Scanner(proc.getInputStream());
			while(in.hasNextLine())
				System.out.println(in.nextLine());
			in.close();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
				proc.destroy();
			}
		}
	}

}
