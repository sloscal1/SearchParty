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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

import core.messages.DispatcherCentricMessages.Parameter;
import core.messages.DispatcherCentricMessages.Parameter.GrowthPattern;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.SearcherCentricMessages.RunSettings;

public class ExhaustiveExpGenerator implements ExpGenerator{
	private List<String> allSettings = new ArrayList<>();
	private int index = 0;
	
	public ExhaustiveExpGenerator(Setup msg) {
		//Get the list of Parameters to construct experiments from
		List<Parameter> params = new LinkedList<>(msg.getParamsList());
		Collections.reverse(params);
		recursivelyBuildSettings("", params);
	}

	private void recursivelyBuildSettings(String prev, List<Parameter> remainingParams){
		//Parse the user defined settings for this parameter
		Parameter current = remainingParams.get(0);
		GrowthUtil f = null;
		if(GrowthPattern.LINEAR == current.getPattern())
			f = new GrowthUtil.Linear();
		else if(GrowthPattern.LOG == current.getPattern())
			f = new GrowthUtil.Log();
		else if(current.getSpecificValuesCount() != 0 && GrowthPattern.SPECIFIC == current.getPattern())
			f = new GrowthUtil.Specific(current.getSpecificValuesList());
		else
			throw new IllegalStateException("Could not parse Parameter "+current.getParamName()+", incorrect growth pattern.");

		double min = current.getMinValue();
		double max = current.getMaxValue();
		double rate = current.getGrowthValue();

		//Generate the values for this parameter
		int size = remainingParams.size();
		List<Parameter> rem = size > 1 ? remainingParams.subList(1, size) : null;
		for(double value = rate, last = Double.NaN; 
				last != rate && f.hasNext(); 
				last = value, f.next(value, min, max, rate)){
			System.out.println(current.getParamName()+" "+value+" "+f.hasNext());
			if(rem != null)
				recursivelyBuildSettings(prev+" formal_name : \""+current.getParamName()+"\" value: \""+value+"\"", rem);
			else{
				allSettings.add(prev.trim());
				System.out.println(allSettings.get(allSettings.size()-1));
			}
		}
		System.out.println(f.hasNext());
	}
	
	@Override
	public boolean hasNext() {
		return index < allSettings.size();
	}
	
	@Override
	public RunSettings next() {
		RunSettings.Builder b = RunSettings.newBuilder();
		try {
			TextFormat.merge(allSettings.get(index++), b);
			if(index == allSettings.size())
				b.setTerminal(true); //This generator is out of experiments!
		} catch (ParseException e) {
			System.err.println("Serious issue, internal code could not generate RunSettings!!!");
			e.printStackTrace();
			System.exit(1);
		}
		return b.build();
	}

}
