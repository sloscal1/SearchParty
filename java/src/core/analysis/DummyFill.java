package core.analysis;

public class DummyFill extends CompNode {
	private String name;
	
	public DummyFill(String name) {
		super(null);
		this.name = name;
	}

	@Override
	public Object execute() {
		throw new IllegalStateException("A dummy node wasn't filled in: "+name);
	}

}
