package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.analysis.CollectionResultValue;
import core.analysis.ResultFilter;
import core.analysis.ResultValue;
import core.analysis.StringResultValue;

public class SQLQueryResultProvider extends SQLiteResultProvider {
	private final static List<ResultValue<?>> input;
	private final static List<ResultValue<?>> output;
	public final static String WHERE_PARAM = "WHERE";
	public final static String FROM_PARAM = "FROM";
	public final static String DB_PARAM = "DB";
	public final static String QUERY_PARAM = "QUERY";
	
	static {
		List<ResultValue<?>> o = new ArrayList<>(1);
		o.add(new CollectionResultValue<Iterable<? extends Number>>("Data", null, false));
		output = Collections.unmodifiableList(o);

		List<ResultValue<?>> i = new ArrayList<>(1);
		i.add(new StringResultValue(WHERE_PARAM, "", false));
		i.add(new StringResultValue(FROM_PARAM, "", true));
		i.add(new StringResultValue(DB_PARAM, "", true));
		i.add(new StringResultValue(QUERY_PARAM, "", true));
		input = Collections.unmodifiableList(i);
	}

	private Map<String, List<ResultFilter<?>>> inputs = new HashMap<>();
	private Connection dbConn;
	private Statement stmt;
	private String sql;

	public SQLQueryResultProvider() throws SQLException {
		if(!sql.trim().startsWith("SELECT"))
			throw new IllegalArgumentException("SQL statement must begin with \"SELECT\": "+sql);
		dbConn = DriverManager.getConnection("jdbc:sqlite:"+db);
		stmt = dbConn.createStatement();
		inputs.put(WHERE_PARAM, new ArrayList<>());
		inputs.put(FROM_PARAM, new ArrayList<>());
		this.sql = sql;
	}

	@Override
	public ResultSet pull() {
		ResultSet ret = null;
		try {
			ret = stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public List<ResultValue<?>> getOutputs() {
		return output;
	}

	@Override
	public List<ResultValue<?>> getInputs() {
		return input;
	}

	@Override
	public ResultFilter<ResultSet> addInput(String inputName, ResultFilter<?> input) {
		inputs.get(inputName).add(input);
		return this;
	}

	@Override
	public List<ResultValue<?>> apply() {
		inputs.get(DB_PARAM).get(0).apply().get(0);
		//Build the SQL query:
		String completeSQL = sql;
		List<ResultFilter<?>> wheres = inputs.get(WHERE_PARAM);
		if(!wheres.isEmpty()){
			completeSQL += " WHERE, " +inputs.get("WHERE").toString();
		}
		completeSQL += ";";
		String oldSQL = sql;
		sql = completeSQL;
		List<ResultValue<?>> retVal = transformToList(pull());
		sql = oldSQL;
		return retVal;
	}

	@Override
	public ResultFilter<ResultSet> addInput(ResultFilter<?> inGen) {
		throw new UnsupportedOperationException("No anonymous inputs in SQLQueryResultProvider");
	}
}
