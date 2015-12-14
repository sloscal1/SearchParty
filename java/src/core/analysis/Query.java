package core.analysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class Query extends CompNode {

	public Query(){
		super(null);
	}

	@Override
	public Object execute() {
		List<Object> returned = new ArrayList<>();
		try {
			Connection dbConn = DriverManager.getConnection("jdbc:sqlite:"+getInput("DB"));
			Statement stmt = dbConn.createStatement();
			String sql = getInput("BASE")
					+" FROM " + getInput("FROM")
					+(getInput("WHERE") != null ?" WHERE " + getInput("WHERE"):"")
					+";";
			ResultSet res = stmt.executeQuery(sql);
			ResultSetMetaData meta = res.getMetaData();
			
			int nCols = meta.getColumnCount();
			List<Integer> colTypes = new ArrayList<>(nCols);
			for(int i = 1; i <= nCols; ++i)
				colTypes.add(meta.getColumnType(i));
			while(res.next()){
				if(nCols > 1){
					List<Object> row = new ArrayList<>(nCols);
					for(int i = 1; i <= nCols; ++i){
						int type = colTypes.get(i-1);
						if(type == Types.INTEGER)
							row.add(res.getInt(i));
						else if(type == Types.DOUBLE)
							row.add(res.getDouble(i));
						else if(type == 12)
							row.add(res.getString(i));
					}
					returned.add(row);
				}
				else{
					int type = colTypes.get(0);
					if(type== Types.INTEGER)
						returned.add(res.getInt(1));
					else if(type == Types.DOUBLE)
						returned.add(res.getDouble(1));
					else if(type == 12)
						returned.add(res.getString(1));
				}
			}
			res.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returned;
	}

}
