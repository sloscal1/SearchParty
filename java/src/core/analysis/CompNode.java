package core.analysis;

import java.util.HashMap;
import java.util.Map;

public abstract class CompNode implements Computable {
	protected static Map<CompNode, CompNode> forwards = new HashMap<>();

	protected CompNode src;
	protected Map<CompNode, String> placeHolders = new HashMap<>();
	private Map<String, Object> inputs = new HashMap<>();

	public CompNode(CompNode src){
		this.src = src;
	}

	public CompNode markForFilling(CompNode filler, String desiredKey){
		placeHolders.put(filler, desiredKey);
		return this;
	}

	public CompNode input(String key, Object value){
		inputs.put(key, value);
		return this;
	}
	
	public Object getInput(String key){
		Object obj = inputs.get(key);
		if(obj instanceof CompNode)
			obj = ((CompNode)obj).execute();
		return obj;
	}

	protected void fill(CompNode filler, Object value){
		if(placeHolders.containsKey(filler))
			input(placeHolders.get(filler), value);
		for(Object input : inputs.values())
			if(input instanceof CompNode)
				((CompNode)input).fill(filler, value);
		if(src != null)
			src.fill(filler, value);
	}

	public static void main(String[] args){
		//Literal (1x1)-> Query (nx1) -> Filter (nxk) -> Transpose (kxn) -> Filter (kxk) -> Print
		//OP (1x1) Query (mx1) -> Summary ->(1xk)      OP (1xn) Summary -> (1xk)

		//"results.db", "SELECT table.name FROM all.tables WHERE name=TID_20_*_RUN"
		//"results.db", "SELECT DISTINCT Generation "
		//"results.db", "SELECT Fitness FROM TID_20_41_RUN"

		// Filter[RunTable] -> QueryRunOp 
		//   --(Run Specific Tables)-> 
		//     Filter[TableName] -> QueryGenOp 
		//       --(Valid Gen #'s from Single Run Table)->
		//         QueryFitness -Collection of fitness vals-> Summary
		

		//These are the filters that will be run...
		CompNode forwardGenOp = new DummyFill("GenOp");
		CompNode forwardRunOp = new DummyFill("RunOp");
		CompNode forwardOverallOp = new DummyFill("OverallOp");

		//Get all fitness values from a particular generation of a particular run
		CompNode s1 = new Summary(new Query().input("DB", "results.db")
				.input("BASE", "SELECT Fitness")
				.markForFilling(forwardRunOp, "FROM")
				.input("WHERE", new Concatenation().input("1", "Generation=").markForFilling(forwardGenOp, "2")));
		
		//Get the set of generation values that are valid for this table
		Filter genOp = new Filter(new Query().input("DB", "results.db")
				.input("BASE", "SELECT DISTINCT Generation")
				.markForFilling(forwardRunOp, "FROM"),
				s1,
				forwardGenOp);

		//Get the table names that contain information of interest...
		Filter runOp = new Filter(new Query().input("DB", "results.db")
				.input("BASE", "SELECT name")
				.input("FROM", "sqlite_master")
				.input("WHERE", "name LIKE 'TID_20_%_RUN'"),
				new Transpose(genOp),
				forwardRunOp);

		//There is a problem here! The runOp results are 3 dimensional so Summary doesn't know what to do.
		//Need to reshape the matrix, currently it is something like:
		//NumRunsx4xNumGenerations
		//Think I'm going to need some slice type operation or permute
		//NumRunsxNumGenerations
		//
		Filter overall = new Filter(runOp,
				new Summary(new Phony()).markForFilling(forwardOverallOp, "DATA"),
				forwardOverallOp);

		new Print(overall).execute();
	}
}
