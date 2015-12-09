package core.analysis;

public class LiteralResultValue<T> implements ResultValue<T> {
	private String name;
	private T value;
	private boolean optional;
	
	public LiteralResultValue(String name, T value, boolean optional){
		this.name = name;
		this.value = value;
		this.optional = optional;
	}
	
	public LiteralResultValue(T value){
		this("anonymous", value, false);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getValue() {
		return value;
	}


	@Override
	public boolean isOptional() {
		return optional;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public Class<T> getType() {
		return (Class<T>) value.getClass();
	}

}
