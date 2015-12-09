package core.analysis;

import java.util.List;
//Literal (1x1)-> Query (nx1) -> Filter (nxk) -> Transpose (kxn) -> Filter (kxk) -> Print
//                       OP (1x1) Query (mx1) -> Summary ->(1xk)      OP (1xn) Summary -> (1xk)
// CompNode()

//  Filter has setSubroutine(CompNode)
//  Filter f = new Filter(new Query().addInput(BASE, results).addInput(DB, ...).addInput(FROM).addInput(WHERE))
//  f.setSubroutine(new Summary(new Query().addInput(BASE).addInput(DB).addInput(FROM).addInput(WHERE, (String)f.filter()));
//  Filter f2 = new Filter(new Transpose(f));
//  f2.setSubroutine(new Summary(f2.filter());
//  new Print(f2).execute();

//Print's execute(): double[][] results = (double[][])src.execute();
//Something... 

public class TransposeFilter implements ResultFilter {

	@Override
	public List<ResultValue<?>> getOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultValue<?>> getInputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultFilter addInput(String inputName, ResultValue<?> input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResultValue<?>> apply() {
		// TODO Auto-generated method stub
		return null;
	}

}
