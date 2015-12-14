package core.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class Summary extends CompNode{

	public Summary(CompNode src) {
		super(src);
	}

	@Override
	public Object execute() {
		List<Double> retVal = new ArrayList<>(4);
		Object obj = src.execute();
			
		if(obj instanceof Iterable){
			Iterable<Object> iter = (Iterable<Object>)obj;
			List<Double> values = new ArrayList<Double>();
			for(Object o : iter)
				values.add((Double)o);
			double[] arrVals = new double[values.size()];
			for(int i = 0; i < arrVals.length; ++i)
				arrVals[i] = values.get(i);
			retVal.add(StatUtils.mean(arrVals));
			retVal.add(new StandardDeviation().evaluate(arrVals, retVal.get(0)));
			retVal.add(StatUtils.min(arrVals));
			retVal.add(StatUtils.max(arrVals));			
		}
		return retVal;
	}

}
