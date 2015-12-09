package core.analysis;

import java.util.List;

public interface ResultProvider<T> {
	//Is each separate query a different type of result?
	//What is the interface between the DB and the result graph?
	ResultValue<?> transform(T values);
	List<ResultValue<?>> transformToList(T values);
	//Can do something concrete like transform(ResultSet values) into a CollectionResultValue or Primitive
	
	T pull();
}
