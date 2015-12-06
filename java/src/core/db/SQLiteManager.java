package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import core.messages.DispatcherCentricMessages;
import core.messages.DispatcherCentricMessages.Parameter.DataType;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.ExperimentResults.Result;
import core.messages.ExperimentResults.ResultMessage;
import core.messages.SearcherCentricMessages.Argument;
import core.messages.SearcherCentricMessages.RunSettings;
import core.party.ExpGenerator;

public class SQLiteManager {
	private Connection dbConn;
	private Statement stmt;
	private String runTableName;
	private String runParams;
	private Setup exp;
	private int currentExpID;
	private Set<String> createdResultTables = new HashSet<>();
	/** Map to an argument name to the appropriate table in the database */
	private Map<String, Integer> runTableMap;

	public SQLiteManager(Setup inputExp) throws SQLException{
		dbConn = DriverManager.getConnection("jdbc:sqlite:"+inputExp.getDatabasePath());
		stmt = dbConn.createStatement();
		runTableName = getTableNameFrom(inputExp.getExperimentName()).trim();
		this.exp = inputExp;
		
		runTableMap = new HashMap<>();
		StringBuilder rp = new StringBuilder();
		
		int pos = 0;
		for(DispatcherCentricMessages.Parameter p : exp.getParamsList()){
			String pName = p.getParamName();
			rp.append(" "+escape(pName)+",");
			if(pName.length() > 1)
				runTableMap.put("--"+pName, pos++);
			else
				runTableMap.put("-"+pName, pos++);
		}
		
		rp.append(" "+ExpGenerator.RAND_SEED_ARG_NAME);
		runTableMap.put("--"+ExpGenerator.RAND_SEED_ARG_NAME, pos++);
		runParams = rp.toString();
	}

	private String escape(String name){
		String ret = name.replace('.', '_');
		return ret.replace('-', '_');
	}
	
	private String getTableNameFrom(String experimentName) {
		return "TID_"+experimentName;
	}

	public boolean runsTableExists(){
		boolean retVal = false;
		ResultSet runsTable;
		try {
			runsTable = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"+runTableName+"';");
			retVal = runsTable.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	public boolean experimentTableExists(){
		boolean retVal = false;
		ResultSet exist;
		try {
			exist = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='ALL_EXP';");
			retVal = exist.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	public void createExperimentTable(){
		//One table per test method with all parameter settings and a unique key
		try {
			stmt.executeUpdate("CREATE TABLE ALL_EXP ( "
					+ "ExpID INTEGER PRIMARY KEY, "
					+ "Name TEXT NOT NULL, "
					+ "Proto BLOB NOT NULL, "
					+ "RunTime INTEGER NOT NULL)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createRunsTable() {
		//For all experiments of the same name, use another table to list out the parameters (reduce clutter in the main table)
		//The column names are the parameter names:
		StringBuilder sb = new StringBuilder();

		
		for(DispatcherCentricMessages.Parameter p : exp.getParamsList())
			sb.append(" "+escape(p.getParamName())+" "+getDBType(p.getType())+",");
		sb.append(" "+ExpGenerator.RAND_SEED_ARG_NAME+" INT NOT NULL");
		
		//Create the table
		try {
			stmt.executeUpdate("CREATE TABLE "+runTableName+" ( "
					+ "ExpID INTEGER NOT NULL, "
					+ "RunID INTEGER PRIMARY KEY, "
					+ sb.toString() +")");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getDBType(DataType type){
		return type == DataType.STRING ? "TEXT" : type.toString();

	}

	public void insertExperiment() {
		//Populate the experiment table for this Experiment:
		try {
			stmt.executeUpdate("INSERT INTO ALL_EXP (Name, Proto, RunTime) VALUES("
					+"'"+exp.getExperimentName()+"', "
					+"'"+exp.toByteString()+"', "
					+new GregorianCalendar().getTimeInMillis()+");");
			ResultSet idVal = stmt.executeQuery("SELECT ExpID FROM ALL_EXP ORDER BY ExpID DESC LIMIT 1;");
			currentExpID = idVal.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	public String insertRun(RunSettings run){
		
		String runPrefix = null;

		String[] argPos = new String[run.getArgumentCount()];
		for(Argument arg : run.getArgumentList()){
			Scanner scan = new Scanner(arg.getValue());
			if(scan.hasNextDouble())
				argPos[runTableMap.get(arg.getFormalName())] = arg.getValue();
			else
				argPos[runTableMap.get(arg.getFormalName())] = "'"+arg.getValue()+"'";
			scan.close();
		}
		StringBuilder values = new StringBuilder();
		for(int i = 0; i < argPos.length-1; ++i)
			values.append(argPos[i]+",");
		values.append(argPos[argPos.length-1]);
		
		try {
//			System.out.println("INSERT INTO "+runTableName+"( ExpID, "+runParams+") VALUES ("
//					+currentExpID+", "+values.toString()+");");
			stmt.executeUpdate("INSERT INTO "+runTableName+"( ExpID, "+runParams+") VALUES ("
					+currentExpID+", "+values.toString()+");");
			ResultSet ret = stmt.executeQuery("SELECT ExpID, RunID FROM "+runTableName+" ORDER BY ExpID DESC LIMIT 1;");
			runPrefix = "TID_"+ret.getString(1)+"_"+ret.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return runPrefix;
	}
	//One table per parameter settings key with all result values
	//create table per (if executable and all start parameters are the same, it should go to the same table.)

	public void insertResults(ResultMessage msg) {
		//See if this table exists:
		//TODO: The DB table name needs to be more specific, maybe include exp_id and run_id to completely identify it.

		if(!createdResultTables.contains(msg.getTableName())){
			StringBuilder columns = new StringBuilder();
			for(Result r : msg.getReportedValueList())
				columns.append(" "+r.getName()+" DOUBLE,");
			columns.append(" Timestamp INTEGER,");
			columns.append(" Machine TEXT");

			try {
				System.out.println("CREATE TABLE "+msg.getTableName()+" ("
						+columns.toString()+")");
				stmt.executeUpdate("CREATE TABLE "+msg.getTableName()+" ("
						+columns.toString()+")");
				createdResultTables.add(msg.getTableName());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//Now put these values into the appropriate table
		StringBuilder columnNames = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for(Result r : msg.getReportedValueList()){
			columnNames.append(" "+r.getName()+",");
			values.append(" "+r.getValue()+",");
		}
		try {
			stmt.executeUpdate("INSERT INTO "+msg.getTableName()+"("+columnNames.toString()+" Timestamp, Machine ) "
					+ " VALUES("+values.toString()+" "+msg.getTimestamp()+", "+msg.getMachineName()+");");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			stmt.close();
			dbConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
