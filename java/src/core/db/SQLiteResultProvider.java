package core.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.analysis.CollectionResultValue;
import core.analysis.PrimitiveResultValue;
import core.analysis.ResultFilter;
import core.analysis.ResultValue;

//Need to specify where the ResultSet gets closed. Assuming outside this method for now.
public abstract class SQLiteResultProvider implements ResultFilter<ResultSet> {

	@Override
	public ResultValue<?> transform(ResultSet values) {
		return transformColumns(values, 1).get(0);
	}

	@Override
	public List<ResultValue<?>> transformToList(ResultSet values) {
		List<ResultValue<?>> retVal = null;
		try {
			int[] colIndices = new int[values.getMetaData().getColumnCount()];
			for(int i = 0; i < colIndices.length; ++i)
				colIndices[i] = i+1;
			retVal = transformColumns(values, colIndices);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;		
	}

	private List<ResultValue<?>> transformColumns(ResultSet values, int... colIndices){
		List<ResultValue<?>> retVal = new ArrayList<>();
		try {
			ResultSetMetaData meta = values.getMetaData();
			int maxColumnIndex = Integer.MIN_VALUE;
			for(int i : colIndices)
				if(i > maxColumnIndex)
					maxColumnIndex = i;
			if(meta.getColumnCount() < maxColumnIndex)
				throw new IllegalArgumentException("ResultSet should have at least "+maxColumnIndex+" columns: "+meta.getColumnCount());
			Map<Integer, List<Number>> resultsMap = new HashMap<>(colIndices.length);
			Map<Integer, String> nameMap = new HashMap<>(colIndices.length);
			for(int i : colIndices){
				resultsMap.put(i, new ArrayList<Number>());
				nameMap.put(i, meta.getColumnLabel(i));
			}
			
			while(values.next()){
				for(int i : colIndices){
					int types = meta.getColumnType(i);
					if(types == Types.INTEGER){
						resultsMap.get(i).add(values.getInt(i));				
					}else if(types == Types.FLOAT || types == Types.DOUBLE){
						resultsMap.get(i).add(values.getDouble(i));
						System.out.println(resultsMap.get(i).size());
					}
				}
			}
			for(int i : colIndices){
				if(resultsMap.get(i).size() == 1)
					retVal.add(new PrimitiveResultValue<Number>(nameMap.get(i), resultsMap.get(i).get(0), false));
				else
					retVal.add(new CollectionResultValue<Iterable<? extends Number>>(nameMap.get(i), resultsMap.get(i), false));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}
}
