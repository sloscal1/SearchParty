package core.analysis;

/**
 * The attributes of edges (data) in the computational
 * graph defined by the user to produce results from data stored
 * in the database created by SearchParty.
 * 
 * @author sloscal1
 *
 * @param <T>
 */
public interface ResultValue<T> {
	/**
	 * Used by ResultFilter objects to name specific parameters.
	 * @return a non-null name of the object, will be unique for a particular ResultFilter class.
	 */
	String getName();
	
	/**
	 * Get the value of the result given by this object.
	 * @return
	 */
	T getValue();
	
	/**
	 * Get the type of value contained in this result value.
	 * @return must not be null
	 */
	Class<T> getType();
	
	/**
	 * Only useful when produced by ResultFilter's getInputs method.
	 * 
	 * @return true if this value is an optional input to a ResultFilter
	 */
	boolean isOptional();
}
