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
		double[] retVal = null;
		Object obj = src.execute();
		if(obj instanceof Iterable){
			Iterable<Object> iter = (Iterable<Object>)obj;
			List<Double> values = new ArrayList<Double>();
			for(Object o : iter)
				values.add((Double)o);
			double[] arrVals = new double[values.size()];
			for(int i = 0; i < arrVals.length; ++i)
				arrVals[i] = values.get(i);
			retVal = new double[4];
			retVal[0] = StatUtils.mean(arrVals);
			retVal[1] = new StandardDeviation().evaluate(arrVals, retVal[0]);
			retVal[2] = StatUtils.max(arrVals);
			retVal[3] = StatUtils.max(arrVals);			
		}
		return retVal;
	}

}
