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

import core.messages.DispatcherCentricMessages.Parameter;
import core.messages.DispatcherCentricMessages.Parameter.GrowthPattern;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.SearcherCentricMessages.Argument;
import core.messages.SearcherCentricMessages.RunSettings;
import core.messages.SearcherCentricMessages.RunSettings.Builder;

public class ExhaustiveExpGenerator implements ExpGenerator{
	private List<RunSettings> allSettings = new ArrayList<>();
	private int index = 0;
	
	public ExhaustiveExpGenerator(Setup msg) {
		//Get the list of Parameters to construct experiments from
		List<Parameter> params = new LinkedList<>(msg.getParamsList());
		Collections.reverse(params);
		recursivelyBuildSettings(RunSettings.newBuilder(), params);
	}

	private void recursivelyBuildSettings(Builder b, List<Parameter> remainingParams){
		//Parse the user defined settings for this parameter
		Parameter current = remainingParams.get(0);
		double min = current.getMinValue();
		double max = current.getMaxValue();
		double rate = current.getGrowthValue();
		
		GrowthUtil f = null;
		if(GrowthPattern.LINEAR == current.getPattern())
			f = new GrowthUtil.Linear(min, max, rate);
		else if(GrowthPattern.LOG == current.getPattern())
			f = new GrowthUtil.Log(min, max);
		else if(current.getSpecificValuesCount() != 0 && GrowthPattern.SPECIFIC == current.getPattern()){
			f = new GrowthUtil.Specific(current.getSpecificValuesList());
			min = current.getSpecificValues(0);
		}
		else
			throw new IllegalStateException("Could not parse Parameter "+current.getParamName()+", incorrect growth pattern.");

		
		//Generate the values for this parameter
		int size = remainingParams.size();
		List<Parameter> rem = size > 1 ? remainingParams.subList(1, size) : null;
		int newPos = b.getArgumentCount();
		Argument.Builder argB = Argument.newBuilder();
		b.addArgument(argB.setFormalName("").setValue("").build());

		//This loop seems pretty contrived, it is due to the fact that I increment before testing, the last value
		//would get omitted. This seems to work for the time being though.
		for(double value = min, last = Double.NaN; 
				last != value; //index is 0 so yes, it has next...
				last = value, value = f.hasNext()? f.next() : value){ //next should return what is in index 1
			System.out.println(current.getParamName()+" "+value+" "+f.hasNext()+" "+min+" "+max+" "+rate);		
			argB.setFormalName(current.getParamName());
			argB.setValue(""+value);
			b.setArgument(newPos, argB.build());
			if(rem != null)
				recursivelyBuildSettings(b, rem);
			else
				allSettings.add(b.build());
		}
	}
	
	@Override
	public boolean hasNext() {
		return index < allSettings.size();
	}
	
	@Override
	public RunSettings next() {
		return allSettings.get(index++);
	}

}
