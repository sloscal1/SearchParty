package core.analysis;

import java.util.ArrayList;
import java.util.List;

public class Transpose extends CompNode {

	public Transpose(CompNode src) {
		super(src);
	}

	@Override
	public Object execute() {
		Iterable<Iterable<Object>> res = (Iterable<Iterable<Object>>)src.execute();
		//Copy the data into an indexable structure
		List<List<Object>> objs = new ArrayList<List<Object>>();
		for(Iterable<Object> r : res){
			List<Object> list = new ArrayList<Object>();
			for(Object obj : r)
				list.add(obj);
			objs.add(list);
		}
		
		//Transpose that structure (need to do some padding too...)
		List<List<Object>> transpose = new ArrayList<List<Object>>();
		for(int c = 0; c < objs.get(0).size(); ++c){
			List<Object> newRow = new ArrayList<>(objs.size());
			for(int r = 0; r < objs.size(); ++r){
				if(c >= objs.get(r).size())
					newRow.add(objs.get(r).get(objs.get(r).size()-1));
				else
					newRow.add(objs.get(r).get(c));
			}
			transpose.add(newRow);
		}
		System.out.println(transpose.size()+" "+transpose.get(0).size());
		return transpose;
	}

}
