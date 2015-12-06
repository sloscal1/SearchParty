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
		rs = stmt.executeQuery("SELECT * FROM TID_NEAT_6_0");
		System.out.println(rs.getMetaData().getColumnName(1)+", "+rs.getMetaData().getColumnName(2)+", "+rs.getMetaData().getColumnName(3)+", "+rs.getMetaData().getColumnName(4));
		while(rs.next()){
			for(int i = 1; i <= rs.getMetaData().getColumnCount(); ++i){
				System.out.print(rs.getString(i)+",\t");
			}
			System.out.println();
		}
		rs = stmt.executeQuery("SELECT * FROM TID_9_11_RUN WHERE Generation < 5");
		System.out.println(rs.getMetaData().getColumnName(1)+", "+rs.getMetaData().getColumnName(2)+", "+rs.getMetaData().getColumnName(3)+", "+rs.getMetaData().getColumnName(4));
		int count = 0;
		while(rs.next()){
			for(int i = 1; i <= rs.getMetaData().getColumnCount(); ++i){
				System.out.print(rs.getString(i)+",\t");
			}
			System.out.println();
			++count;
		}
		System.out.println("Num results: "+count);
		rs.close();	
		dbConn.close();
	}
}
