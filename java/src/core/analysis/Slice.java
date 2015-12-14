package core.analysis;
import java.util.ArrayList;
import java.util.List;

public class Slice extends CompNode {

	public Slice(CompNode src) {
		super(src);
	}

	@Override
	public Object execute() {
		Object data = src.execute();		
		return iterate(data, 0, (Integer)getInput("SLICE_LAYER"), (Integer)getInput("COMPONENT"));
	}

	private Object iterate(Object src, int layerOn, int sliceTarget, int targetComponent) {
		Object retVal = null;
		if(layerOn == sliceTarget){
			//As I iterate through this layer, need to replace its target with the select component:
			int on = 0;
			for(Object obj : (Iterable<?>)src){
				if(on == targetComponent){
					retVal = obj;
					break;
				}
				++on;
			}
		}
		else{
			List<Object> newLayer = new ArrayList<Object>();
			for(Object obj : (Iterable<?>)src)
				newLayer.add(iterate(obj, layerOn+1, sliceTarget, targetComponent));
			retVal = newLayer;
		}
		return retVal;
	}
	
	//Image:
	//[[[2,5,7,8], (2x3x4)
	//  [3,6,1,4],
	//  [2,2,2,2]],
	// [[1,1,1,1],
	//  [2,2,2,2],
	//  [3,3,3,3]]]
	//3D -> 2D
	//Slice(0,1) ->  
	// [[1,1,1,1], (3x4)
	//  [2,2,2,2],
	//  [3,3,3,3]]
	//Slice(1,0) ->
	// [[2,5,7,8],  (2x4)
	//  [1,1,1,1]]
	//Slice(2,2) ->
	// [[7,1,2],  (2x3)
	//  [1,2,3]]
	//0 2 List<List<List<Double>>>
	//1 2 List<List<Double>>
	//2 2 List<Double>
	
}
