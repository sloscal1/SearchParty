package core.analysis;

import java.util.ArrayList;
import java.util.List;

public class Filter extends CompNode {
	private CompNode task;
	private CompNode forwardDecl;
	
	public Filter(CompNode src, CompNode task, CompNode forwardDecl) {
		super(src);
		this.task = task;
		CompNode.forwards.put(forwardDecl, this);
		this.forwardDecl = forwardDecl;
	}

	@Override
	public Object execute() {
		Iterable<Object> grouping = (Iterable<Object>)src.execute();
		List<Object> filtered = new ArrayList<Object>();
		for(Object obj : grouping){
			//Propagate this change up to any object who needs it...
			task.fill(forwardDecl, obj);
			filtered.add(task.execute());
		}
		return filtered;
	}
	
	@Override
	protected void fill(CompNode filler, Object value) {
		super.fill(filler, value);
		task.fill(filler, value);
	}
}
