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
		if(obj instanceof Iterable){
			for(Object o : (Iterable<Object>)obj)
					printRecursive(o, depth+"*");
		}
		else
			System.out.println(depth+obj.toString());
	}
}
