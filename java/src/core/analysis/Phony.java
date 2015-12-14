package core.analysis;

public class Phony extends CompNode {

	public Phony(CompNode forwardTarget) {
		super(null);
		markForFilling(forwardTarget, "DATA");
	}

	@Override
	public Object execute() {
		return getInput("DATA");
	}
}
