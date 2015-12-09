package core.analysis;

public class StringResultValue implements ResultValue<String> {
	private String name;
	private String value;
	private boolean optional;
	
	public StringResultValue(String name, String value, boolean optional){
		this.name = name;
		this.value = value;
		this.optional = optional;
	}
	
	public StringResultValue(String value){
		this("anonymous", value, false);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}
	
	@Override
	public String toString() {
		return value;
	}

}
