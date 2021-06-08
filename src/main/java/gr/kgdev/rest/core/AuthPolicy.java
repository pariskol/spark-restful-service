package gr.kgdev.rest.core;

import gr.kgdev.dbconn.MysqlConnector;
import gr.kgdev.dbconn.SqlConnector;
import gr.kgdev.dbconn.SqliteConnector;

public class AuthPolicy {
	
	private AuthPolicy() {
	}
	
	public static final String NONE = "None";
	public static final String BASIC_AUTH = "Basic Authentication";
	
	public static final String getUsersCreateQuery(SqlConnector sqlConnector) {
		
		String query = "";
		
		if (sqlConnector instanceof SqliteConnector)
			query = "CREATE TABLE IF NOT EXISTS users (" + 
				"ID INTEGER PRIMARY KEY AUTOINCREMENT," + 
				"NAME TEXT," + 
				"PASSWORD TEXT" + 
				")";
		else if (sqlConnector instanceof MysqlConnector)
			query = "CREATE TABLE IF NOT EXISTS users (" + 
					"ID INT PRIMARY KEY AUTO_INCREMENT," + 
					"NAME VARCHAR(64)," + 
					"PASSWORD VARCHAR(64)" + 
					")";
		
		return query;
	}

	public static final String getEditableTablesCreateQuery(SqlConnector sqlConnector) {
		
		String query = "";
		
		if (sqlConnector instanceof SqliteConnector)
			query = "CREATE TABLE IF NOT EXISTS editable_tables (" + 
				"ID INTEGER PRIMARY KEY AUTOINCREMENT," + 
				"NAME TEXT," + 
				"AUTH_POLICY TEXT," + 
				"METHOD TEXT" + 
				")";
		else if (sqlConnector instanceof MysqlConnector)
			query = "CREATE TABLE IF NOT EXISTS editable_tables (" + 
					"ID INT PRIMARY KEY AUTO_INCREMENT," + 
					"NAME VARCHAR(64)," + 
					"AUTH_POLICY VARCHAR(64)," + 
					"METHOD VARCHAR(64)" + 
					")";
		
		return query;
	}
}
