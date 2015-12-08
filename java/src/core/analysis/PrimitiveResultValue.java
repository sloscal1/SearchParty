package core.analysis;

public class PrimitiveResultValue<T extends Number> implements ResultValue<T> {
	private T value;
	private String name;
	private boolean optional;
	
	public PrimitiveResultValue(String name, T value, boolean optional){
		this.name = name;
		this.value = value;
		this.optional = optional;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getType() {
		return (Class<T>) value.getClass();
	}

	@Override
	public boolean isOptional() {
		return optional;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
}
