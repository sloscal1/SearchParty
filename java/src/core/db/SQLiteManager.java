package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.Scanner;

import core.messages.DispatcherCentricMessages;
import core.messages.DispatcherCentricMessages.Parameter.DataType;
import core.messages.DispatcherCentricMessages.Setup;
import core.messages.SearcherCentricMessages.Argument;
import core.messages.SearcherCentricMessages.RunSettings;

public class SQLiteManager {
	private Connection dbConn;
	private Statement stmt;
	private String runTableName;
	private String runParams;
	private Setup exp;
	private int currentExpID;
	
	public SQLiteManager(Setup inputExp) throws SQLException{
		dbConn = DriverManager.getConnection("jdbc:sqlite:"+inputExp.getDatabasePath());
		dbConn.setAutoCommit(false);
		stmt = dbConn.createStatement();
		runTableName = getTableNameFrom(inputExp.getExperimentName());
		this.exp = inputExp;
	}
	
	private String getTableNameFrom(String experimentName) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean runsTableExists(){
		boolean retVal = false;
		ResultSet runsTable;
		try {
			runsTable = stmt.executeQuery("SELECT * FROM ALL_EXP WHERE Name='"+runTableName+"';");
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
					+ "ExpID INT PRIMARY KEY NOT NULL, "
					+ "Name TEXT NOT NULL, "
					+ "Proto BLOB NOT NULL, "
					+ "RunTime INT NOT NULL)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createRunsTable() {
		//For all experiments of the same name, use another table to list out the parameters (reduce clutter in the main table)
		//The column names are the parameter names:
		StringBuilder sb = new StringBuilder();
		runParams = "";
		for(DispatcherCentricMessages.Parameter p : exp.getParamsList()){
			sb.append(p.getParamName()+" "+getDBType(p.getType())+", ");
			runParams += p.getParamName()+",";
		}
		sb.deleteCharAt(sb.length()-1);
		
		//Create the table
		try {
			stmt.executeUpdate("CREATE TABLE "+runTableName+" ( "
				+ "ExpID INT NOT NULL, "
				+ "RunID INT PRIMARY KEY NOT NULL, "
				+ "Seed INT NOT NULL, "
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
			currentExpID = idVal.getInt(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
	public void insertRun(RunSettings run){
		StringBuilder values = new StringBuilder();
		for(Argument arg : run.getArgumentList()){
			Scanner scan = new Scanner(arg.getValue());
			if(scan.hasNextDouble())
				values.append(arg.getValue());
			else
				values.append("'"+arg.getValue()+"'");
			values.append(",");
			scan.close();
		}
		values.deleteCharAt(values.length()-1);
		
		try {
			stmt.executeUpdate("INSERT INTO "+runTableName+"( ExpID, "+runParams+") VALUES ("
					+currentExpID+", "+values.toString()+");");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//One table per parameter settings key with all result values
	//create table per (if executable and all start parameters are the same, it should go to the same table.)
}
