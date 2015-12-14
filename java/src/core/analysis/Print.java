package core.analysis;

public class Print extends CompNode {
	
	public Print(CompNode src) {
		super(src);
	}

	@Override
	public Object execute() {
		Object obj = src.execute();
		printRecursive(obj, "");
		return null;
	}

	private void printRecursive(Object obj, String depth){
		String nextDepth = ": ";
		if(obj instanceof Iterable){
			nextDepth = (!"".equals(depth))? depth+"x" : depth;
			int pos = 0;
			for(Object o : (Iterable<?>)obj)
					printRecursive(o,nextDepth+(pos++));
		}
		else
			System.out.println(depth+nextDepth+obj.toString());
	}
}
