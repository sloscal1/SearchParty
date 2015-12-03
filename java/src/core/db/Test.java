package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Test {

	public static void main(String[] args) throws Exception{
		Connection dbConn = DriverManager.getConnection("jdbc:sqlite:results.db");
		Statement stmt = dbConn.createStatement();
		
		ResultSet rs = stmt.executeQuery("SELECT * FROM ALL_EXP");
		System.out.println(rs.getMetaData().getColumnName(1)+", "+rs.getMetaData().getColumnName(2)+", "+rs.getMetaData().getColumnName(3)+", "+rs.getMetaData().getColumnName(4));
		while(rs.next()){
			System.out.println(rs.getString(1)+", "+rs.getString(2)+", "+rs.getString(3)+", "+rs.getString(4));
		}
		rs.close();
		dbConn.close();
	}
}
