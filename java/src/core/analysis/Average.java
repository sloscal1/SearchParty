package core.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Average implements ResultFilter {
	private Map<String, ResultValue<?>> inputs;

	private final static List<ResultValue<?>> input;
	private final static List<ResultValue<?>> output;
	
	static {
		List<ResultValue<?>> o = new ArrayList<>(1);
		o.add(new PrimitiveResultValue<Number>("Average", null, false));
		output = Collections.unmodifiableList(o);
		
		List<ResultValue<?>> i = new ArrayList<>(1);
		i.add(new CollectionResultValue<Collection<? extends Number>>("Data", new ArrayList<Number>(), true));
		input = Collections.unmodifiableList(i);
	}
	
	public Average(){
		inputs = new HashMap<>();		
	}
	
	@Override
	public List<ResultValue<?>> getOutputs() {
		return output;
	}

	@Override
	public List<ResultValue<?>> getInputs() {
		return input;
	}

	@Override
	public void addInput(String inputName, ResultValue<?> input) {
		if("Data".equalsIgnoreCase(inputName)){
			if(input instanceof Iterable)
				inputs.put("Data", input);
			else throw new IllegalArgumentException("Average input: Data must be Iterable.");
		}
		else throw new IllegalArgumentException("Average has no input named: "+inputName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ResultValue<?>> apply() {
		if(!inputs.containsKey("Data")) throw new IllegalStateException("Input: Data has not been specified.");
		
		double value = 0.0;
		int count = 0;
		//Checked what could be done in addInput, 
		for(Number n : (Iterable<Number>)inputs.get("Data")){
			value += n.doubleValue();
			++count;
		}
		
		List<ResultValue<?>> out = new ArrayList<>(1);
		out.add(new PrimitiveResultValue<Number>("Average", value / count, false));
		return out;
	}
	
	public static void main(String[] args){
		//Create the data source to serve as some proxy result
		List<Double> data = new ArrayList<Double>(100);
		for(int i = 0; i <= 9; ++i)
			data.add((double)i);
		
		//Create the wrapped object that the Average node requires
		CollectionResultValue<List<Double>> input = new CollectionResultValue<>(data);
		Average avg = new Average();
		avg.addInput("Data", input);
		
		//Apply the operator and print out the results:
		List<ResultValue<?>> outputs = avg.apply();
		System.out.println("Expected: 50.0, Actual: "+outputs.get(0));
	}
}
