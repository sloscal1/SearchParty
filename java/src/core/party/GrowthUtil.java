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

import java.util.List;

/**
 * A utility class that maps the GrowthPattern types in a Setup message Parameter type
 * to the behavior it describes.
 * 
 * @author sloscal1
 *
 */
public abstract class GrowthUtil {
	/** Acceptable rounding error in double equality calculations */
	public static final double EPSILON = 1e-5;
	/** Whether or not this pattern has another value to report */
	protected boolean hasNext = true;
	
	/**
	 * True if this growth pattern has another value below the max value.
	 * @return true the first call by default
	 */
	public boolean hasNext(){
		return hasNext;
	}
	
	/**
	 * Generate the next value of this parameter.
	 * 
	 * @param current the current value of the parameter (starts at min)
	 * @param min the minimum value this parameter can be set to
	 * @param max the maximum value allowed by this parameter
	 * @param rate the rate of change of the parameter (current + rate for LINEAR, current * min for LOG)
	 * @return the next value, could exceed max
	 */
	public abstract Object next();
	
	public static class Linear extends GrowthUtil{
		private double max;
		private double rate;
		private double current;

		public Linear(double min, double max, double rate){
			this.current = min;
			this.max = max;
			this.rate = rate;
		}
		
		public boolean hasNext(){
			return current + rate <= max + EPSILON;
		}
		
		@Override
		public Object next() {
			current += rate;
			return current;
		}
	}
	
	public static class Log extends GrowthUtil{
		private double max;
		private double growth;
		private double current;
		
		public Log(double min, double max, double growth){
			this.current = min;
			this.growth = growth;
			this.max = max;
		}
		
		public boolean hasNext(){
			return current * growth <= max + EPSILON;
		}
		
		public Object next(){
			current *= growth;
			return current;
		}
	}
	
	public static class Specific extends GrowthUtil{
		private List<String> values;
		private int index = 1; //min is assigned values(0)
		
		public Specific(List<String> values){
			this.values = values;
		}
		
		@Override
		public boolean hasNext() {
			return index < values.size();
		}
		
		@Override
		public Object next() {
			return values.get(index++);
		}
	}
}
