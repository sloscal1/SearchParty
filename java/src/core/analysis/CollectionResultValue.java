package core.analysis;

import java.util.Iterator;

public class CollectionResultValue<T extends Iterable<? extends Number>> implements ResultValue<T>, Iterable<Number> {
	private String name;
	private T values;
	private boolean optional;
	
	public CollectionResultValue(String name, T values, boolean optional){
		this.name = name;
		this.values = values;
		this.optional = optional;
	}
	
	public CollectionResultValue(T values){
		this("anonymous", values, false);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getValue() {
		return (T) values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getType() {
		return (Class<T>) values.getClass();
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Number> iterator() {
		return ((Iterable<Number>)values).iterator();
	}
	
	@Override
	public String toString() {
		return values.toString();
	}
}
