package core.analysis;

import java.util.List;

/**
 * Key node in the computational graph described by a user
 * to execute over results collected by the SearchParty system.
 * 
 * @author sloscal1
 *
 */
public interface ResultFilter<T> extends ResultProvider<T>{

	/**
	 * Gets list of the outputs produced by this filter.
	 * @return a list containing the name and type of each produced result, all values will be null.
	 */
	List<ResultValue<?>> getOutputs();
	
	/**
	 * Gets a list of the inputs used by this filter. They include
	 * both required and optional inputs.
	 * @return a non-null list of inputs, all values will be null
	 */
	List<ResultValue<?>> getInputs();
	
	/**
	 * Associate the given input with the given input name from this object
	 * listed by this objects getInputs method.
	 * @param input must not be null
	 */
	ResultFilter<T> addInput(String inputName, ResultFilter<?> input);
	
	ResultFilter<T> addInput(ResultFilter<?> inGen);
	/**
	 * Apply this filter to the set inputs.
	 * 
	 * @return a list of outputs
	 */
	List<ResultValue<?>> apply();
}
