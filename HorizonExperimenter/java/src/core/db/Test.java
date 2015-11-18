package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Test{
  public static void main( String args[] ){
    try(Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")){
    	c.setAutoCommit(false);
    	
    	// CREATE TABLE
    	String sql = "CREATE TABLE COMPANY " +
                     "(ID INT PRIMARY KEY     NOT NULL," +
                     " NAME           TEXT    NOT NULL, " + 
                     " AGE            INT     NOT NULL, " + 
                     " ADDRESS        CHAR(50), " + 
                     " SALARY         REAL)";

        System.out.println("Opened database successfully");
        
    	Statement stmt = c.createStatement();
    	stmt.executeUpdate(sql);
       
    	// INSERT
    	sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
                     "VALUES (1, 'Paul', 32, 'California', 20000.00 );"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
              "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
              "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );"; 
        stmt.executeUpdate(sql);

        sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
              "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );"; 
        stmt.executeUpdate(sql);
        
        // SELECT
        System.out.println("Test SELECT:");
        ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
        printResults(rs);
        
        // UPDATE
        System.out.println("Test UPDATE:");
        stmt = c.createStatement();
        sql = "UPDATE COMPANY set SALARY = 25000.00 where ID=1;";
        stmt.executeUpdate(sql);
        c.commit();
        
        rs = stmt.executeQuery("SELECT * FROM COMPANY where ID=1;");
        printResults(rs);
        
        // DELETE
        System.out.println("Test DELETE:");
        sql = "DELETE from COMPANY where ID=2;";
        stmt.executeUpdate(sql);
        c.commit();
        rs = stmt.executeQuery("SELECT * FROM COMPANY;");
        printResults(rs);
        
        stmt.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
  }
  
  public static void printResults(ResultSet rs) throws SQLException{
	  while(rs.next()){
      	System.out.println("ID = "+rs.getInt("id") + 
      			"\nNAME = "+ rs.getString("name")+
      			"\nAGE = "+rs.getInt("age")+
      			"\nADDRESS = "+rs.getString("address")+
      			"\nSALARY = "+rs.getFloat("salary"));
      }
  }
}