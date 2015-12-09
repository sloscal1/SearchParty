package core.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import core.analysis.Accumulator;
import core.analysis.Average;
import core.analysis.LiteralResultFilter;
import core.analysis.Map;
import core.analysis.ResultFilter;
import core.analysis.ResultValue;
import core.analysis.StringResultValue;
import core.analysis.TransposeFilter;

public class Test {

	public static void main(String[] args) throws Exception{
		//		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:results.db");
		//		Statement stmt = dbConn.createStatement();
		//
		//		printTable(stmt.executeQuery("SELECT * FROM ALL_EXP"));
		//		printTable(stmt.executeQuery("SELECT * FROM TID_NEAT_6_0"));
		//		printTable(stmt.executeQuery("SELECT COUNT(DISTINCT Generation) FROM TID_20_41_RUN"));
		//		printTable(stmt.executeQuery("SELECT * FROM TID_20_41_RUN WHERE Generation = 20"));
		//		dbConn.close();

		//Have some broadcast selector?
		//Wildcard from
		//Wildcard where?
		//Select table names with TID_20_ prefix



		//
		//Filter(count, Average)
		//Map(Average, generationQuery) returns output that is shape rows: |num results from gQ| X cols: |num columns from gQ|
		//Transpose returns output that is shape rows: |num columns from gQ| X cols: |num results from gQ|
		//Map(Avg, Accumulate( T(Map(Avg, Map(gQ, Map(gR, START))))))
		ResultFilter core = new SQLQueryResultProvider("results.db", "SELECT table.name FROM all.tables WHERE name=TID_20_*_RUN;");
		new Map().addInput("OP", new Average()
				          )
				 .addInput("Data", core);
		//Average of each generation:
		//Map(Avg, each_gen_of_table_i)
		//ResultFilters need to take as input
		new SQLQueryResultProvider()
			.addInput("DB", new LiteralResultFilter<String>("result.db"))
			.addInput("QUERY", new LiteralResultFilter<String>("base query"))
			.addInput("TABLE", new Map().addInput("OP", new IdentityResultFilter())
										.addInput("Data", new SQLResultProvider("counting queries")));
		//This seems like a mess!
		SQLQueryResultProvider runTablesQuery = new SQLQueryResultProvider("results.db", "SELECT table.name FROM all.tables WHERE name=TID_20_*_RUN");
		List<ResultValue<?>> allRunTables = runTablesQuery.apply();
		Accumulator runAcc = new Accumulator();
		for(ResultValue<?> run : allRunTables){
			SQLQueryResultProvider generationQuery = new SQLQueryResultProvider("results.db", "SELECT DISTINCT Generation ");
			generationQuery.addInput("FROM", run);
			List<ResultValue<?>> generationResult = generationQuery.apply();	
			Accumulator genAcc = new Accumulator();

			//Map takes as input a ResultFilter operator (need to chain them together)
			//It also takes a bunch of 
			for(ResultValue<?> val : generationResult){
				SQLQueryResultProvider rp = new SQLQueryResultProvider("results.db", "SELECT Fitness FROM TID_20_41_RUN");
				rp.addInput("WHERE", new StringResultValue("Generation = "+val.getValue()));
				//How to broadcast the Generation parameter?
				List<ResultValue<?>> results = rp.apply();
				Average avg = new Average();
				avg.addInput("Data", results.get(0));
				List<ResultValue<?>> average = avg.apply();
				for(ResultValue<?> res : average)
					genAcc.addInput(res.getName(), res);
			}
			List<ResultValue<?>> allRun = genAcc.apply();
			for(ResultValue<?> res : allRun)
				runAcc.addInput(res.getName(), new TransposeFilter().addInput(res.getName(), res).apply().get(0));
		}
		Average runAvgs = new Average();
		List<ResultValue<?>> allOutputs = runAcc.getOutputs();
		for(ResultValue<?> output : allOutputs)
			runAvgs.addInput(output.getName(), output);

		List<ResultValue<?>> allAvgs = runAvgs.apply();
		for(ResultValue<?> finalResult : allAvgs)
			System.out.println(finalResult.getName()+": "+finalResult);
	}

	private static void printTable(ResultSet table) throws SQLException{
		if(table != null){
			//Print out the table headers
			ResultSetMetaData meta = table.getMetaData();
			System.out.println(meta.getTableName(1));
			for(int i = 1; i <= meta.getColumnCount(); ++i)
				System.out.print(meta.getColumnName(i)+",\t");
			System.out.println();

			//Print out the table contents:
			int count = 0;
			while(table.next()){
				for(int i = 1; i <= meta.getColumnCount(); ++i){
					System.out.print(table.getString(i)+",\t");
				}
				System.out.println();
				++count;
			}
			System.out.println("Rows: "+count);
			table.close();
		}
	}
}
