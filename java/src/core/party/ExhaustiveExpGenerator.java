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
import java.util.Random;

import core.messages.DispatcherCentricMessages.Parameter;
import core.messages.DispatcherCentricMessages.Parameter.GrowthPattern;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.SearcherCentricMessages.Argument;
import core.messages.SearcherCentricMessages.RunSettings;
import core.messages.SearcherCentricMessages.RunSettings.Builder;

/**
 * Create all the experiments to be run in this session up front before
 * any are executed.
 * 
 * @author sloscal1
 *
 */
public class ExhaustiveExpGenerator implements ExpGenerator{
	/** The list of all the settings that have been generated */
	private List<RunSettings> allSettings = new ArrayList<>();
	/** The current index to select the run setting from */
	private int index = 0;
	/** The source random seed to compose with each run setting */
	private long randSeed;

	public ExhaustiveExpGenerator(Setup msg) {
		//Get the list of Parameters to construct experiments from
		List<Parameter> params = new LinkedList<>(msg.getParamsList());
		randSeed = msg.getRandSeed();
		Collections.reverse(params);
		recursivelyBuildSettings(RunSettings.newBuilder(), 1, params);
	}

	private void recursivelyBuildSettings(Builder b, int replicates, List<Parameter> remainingParams){
		//Parse the user defined settings for this parameter
		Parameter current = remainingParams.get(0);
		double min = current.getMinValue();
		double max = current.getMaxValue();
		double rate = current.getGrowthValue();
		//See what the max replicates are so far:
		int maxReps = Math.max(replicates, current.getReplicates());

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
		Argument.Builder argB = Argument.newBuilder().setFormalName(current.getParamName());

		//The randseed to set each RunSetting's repeatability source with
		Argument.Builder argRand = Argument.newBuilder().setFormalName(ExpGenerator.RAND_SEED_ARG_NAME); //TODO undoc requirement.
		Random rand = null;
		if(rem == null)
			rand = new Random(randSeed);
		
		//This loop seems pretty contrived, it is due to the fact that I increment before testing, the last value
		//would get omitted. This seems to work for the time being though.
		for(double value = min, last = Double.NaN;
				last != value;
				last = value, value = f.hasNext()? f.next() : value){
			//Check to see if it's an int value - experiment might rely on parsing as int.
			long intVal = Math.round(value);
			if(value == intVal)
				argB.setValue(""+intVal);
			else
				argB.setValue(""+value);
			b.addArgument(argB.build());
			if(rem != null)
				recursivelyBuildSettings(b, maxReps, rem);
			else{
				//Each set of parameter values must be run maxReps numbers of times, set the random source for each.
				for(int i = 0; i < maxReps; ++i){
					//Add and remove random seeds according to the number of replications
					b.addArgument(argRand.setValue(""+rand.nextLong()).build());
					allSettings.add(b.build());
					b.removeArgument(b.getArgumentCount()-1);
				}
			}
			//Keep adding and removing until all iterations are complete.
			b.removeArgument(b.getArgumentCount()-1);
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
