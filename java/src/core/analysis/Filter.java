package core.analysis;

import java.util.ArrayList;
import java.util.List;

public class Filter extends CompNode {
	private CompNode task;
	
	public Filter(CompNode src, CompNode task) {
		super(src);
		this.task = task;
	}

	@Override
	public Object execute() {
		Iterable<Object> grouping = (Iterable<Object>)src.execute();
		List<Object> filtered = new ArrayList<Object>();
		for(Object obj : grouping){
			task.fill(this, obj);
			filtered.add(task.execute());
		}
		return filtered;
	}

}
