package core.analysis;

public class Concatenation extends CompNode {
	public Concatenation() {
		super(null);
	}
	
	@Override
	public Object execute() {
		return getInput("1").toString()+getInput("2").toString();
	}
}
