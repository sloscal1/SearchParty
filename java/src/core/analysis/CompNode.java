package core.analysis;

//Literal (1x1)-> Query (nx1) -> Filter (nxk) -> Transpose (kxn) -> Filter (kxk) -> Print
//OP (1x1) Query (mx1) -> Summary ->(1xk)      OP (1xn) Summary -> (1xk)
//CompNode()

//Filter has setSubroutine(CompNode)
//Filter f = new Filter(new Query().addInput(BASE, results).addInput(DB, ...).addInput(FROM).addInput(WHERE))
//f.setSubroutine(new Summary(new Query().addInput(BASE).addInput(DB).addInput(FROM).addInput(WHERE, (String)f.filter()));
//Filter f2 = new Filter(new Transpose(f));
//f2.setSubroutine(new Summary(f2.filter());
//new Print(f2).execute();

//Print's execute(): double[][] results = (double[][])src.execute();

public abstract class CompNode implements Computable {
	protected CompNode src;
	
	public CompNode(CompNode src){
		this.src = src;
	}
}
