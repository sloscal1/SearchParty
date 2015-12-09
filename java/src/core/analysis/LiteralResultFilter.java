package core.analysis;

import java.util.ArrayList;
import java.util.List;

public class LiteralResultFilter<T> implements ResultFilter<T> {
	private T value;
	
	public LiteralResultFilter(T value){
		this.value = value;
	}
	@Override
	public ResultValue<?> transform(T values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultValue<?>> transformToList(T values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pull() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultValue<?>> getOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultValue<?>> getInputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultFilter<T> addInput(String inputName, ResultFilter<?> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultFilter<T> addInput(ResultFilter<?> inGen) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultValue<?>> apply() {
		List<ResultValue<?>> retVal = new ArrayList<ResultValue<?>>();
		retVal.add(new LiteralResultValue<T>(value));
		return retVal;
	}

}
