package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {

	public static void main(String[] args) throws Exception{
		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:results.db");
		Statement stmt = dbConn.createStatement();

		printTable(stmt.executeQuery("SELECT name FROM sqlite_master WHERE name LIKE 'TID_20_%_RUN';"));
		//		printTable(stmt.executeQuery("SELECT * FROM TID_NEAT_6_0"));
		//		printTable(stmt.executeQuery("SELECT COUNT(DISTINCT Generation) FROM TID_20_41_RUN"));
		//		printTable(stmt.executeQuery("SELECT * FROM TID_20_41_RUN WHERE Generation = 20"));
		//		dbConn.close();

		//Have some broadcast selector?
		//Wildcard from
		//Wildcard where?
		//Select table names with TID_20_ prefix



		//
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
