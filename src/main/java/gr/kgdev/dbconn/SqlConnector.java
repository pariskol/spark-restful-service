package gr.kgdev.dbconn;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import gr.kgdev.model.ListOfMaps;
import gr.kgdev.utils.dtomapper.DTOMapper;


public abstract class SqlConnector {

	private static final List<Object> EMPTY_LIST = Collections.emptyList();
	private DataSource dataSource;
	private String driver;
	private String url;
	private String user;
	private String password;
	private ExecutorService executorService;

	public SqlConnector() {
		executorService = Executors.newCachedThreadPool();
	}

	public SqlConnector(String driver, String url, String user, String password) {
		this();
		this.url = url;
		this.driver = driver;
		this.user = user;
		this.password = password;
		dataSource = this.initDatasource();
	}

	abstract protected DataSource initDatasource();

	private PreparedStatement prepareStatementWithParams(Connection conn, String query, List<Object> params)
			throws SQLException {
		PreparedStatement statement = conn.prepareStatement(query);
		int i = 1;
		for (Object param : params) {
			if (param == null || param.toString().equals(""))
				statement.setNull(i++, Types.VARCHAR);
			else if (param instanceof byte[]) {
				statement.setBytes(i++, (byte[]) param);
			} else
				statement.setObject(i++, param);
		}

		return statement;
	}

	public void checkConnection() throws SQLException {
		try (Connection conn = dataSource.getConnection();) {
		}
	}

	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(String query, ResultSetAction action) throws SQLException {

		try (Connection conn = dataSource.getConnection();
				Statement statement = conn.createStatement();
				ResultSet rset = statement.executeQuery(query);) {
			while (rset.next()) {
				action.onResultSet(rset);
			}
		}
	}
	
	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(Connection conn, String query, ResultSetAction action) throws SQLException {
		try (Statement statement = conn.createStatement();
			 ResultSet rset = statement.executeQuery(query);) {
			while (rset.next()) {
				action.onResultSet(rset);
			}
		}
	}

	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(String query, List<Object> params, ResultSetAction action) throws SQLException {
		try (Connection conn = dataSource.getConnection();
				PreparedStatement statement = prepareStatementWithParams(conn, query, params);
				ResultSet rset = statement.executeQuery();) {
			while (rset.next()) {
				action.onResultSet(rset);
			}
		}
	}
	
	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(Connection conn, String query, List<Object> params, ResultSetAction action) throws SQLException {
		try (PreparedStatement statement = prepareStatementWithParams(conn, query, params);
			 ResultSet rset = statement.executeQuery();) {
			while (rset.next()) {
				action.onResultSet(rset);
			}
		}
	}
	
	/**
	 * Executes query asynchronously through
	 * {@link gr.kgdev.dbconn.SqlConnector} executor service. Action provided
	 * is applied to each row of result set.
	 * 
	 * @param query
	 * @param action
	 * @throws SQLException
	 */
	public void executeQueryAsync(String query, List<Object> params, ResultSetAction action) {
		executorService.execute(() -> {
			try {
				this.executeQuery(query, params, action);
			} catch (SQLException e) {
				LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
			}
		});
	}


	/**
	 * Executes query. It tries to map rows to map.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public ListOfMaps executeQueryToList(String query) throws SQLException {
		return executeQueryToList(query, EMPTY_LIST);
	}
	
	/**
	 * Executes query. It tries to map rows to map.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public ListOfMaps executeQueryToList(String query, List<Object> params) throws SQLException {
		ListOfMaps l = new ListOfMaps();
		executeQuery(query, params, rset -> l.add(DTOMapper.mapU(rset)));
		return l;
	}
	
	/**
	 * Executes query. It tries to map rows to the given class.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public List<?> executeQueryToList(String query, Class<?> clazz) throws SQLException {
		return executeQueryToList(query, EMPTY_LIST, clazz);
	}
	
	/**
	 * Executes query. It tries to map rows to the given class.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public List<?> executeQueryToList(String query, List<Object> params, Class<?> clazz) throws SQLException {
		List<Object> l = new ArrayList<>();
		executeQuery(query, params, rset -> l.add(DTOMapper.mapU(rset, clazz)));
		return l;
	}

	
	/**
	 * Executes query. It tries to map first row of result set to a map.
	 * Run this if you expect your query to have a single row result set.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public Map<String, Object> executeQueryToMap(String query) throws SQLException {
		return executeQueryToMap(query, EMPTY_LIST);
	}
	
	/**
	 * Executes query. It tries to map first row of result set to a map.
	 * Run this if you expect your query to have a single row result set.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public Map<String, Object> executeQueryToMap(String query, List<Object> params) throws SQLException {
		try (Connection conn = dataSource.getConnection();
				PreparedStatement statement = prepareStatementWithParams(conn, query, params);
				ResultSet rset = statement.executeQuery();) {
			while (rset.next()) {
				return DTOMapper.mapU(rset);
			}
		}
		return null;
	}
	
	/**
	 * Executes query. It tries to map first row of result set to the given class.
	 * Run this if you expect your query to have a single row result set.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public Object executeQueryToObject(String query, Class<?> clazz) throws SQLException {
		return executeQueryToObject(query, EMPTY_LIST, clazz);
	}
	
	/**
	 * Executes query. It tries to map first row of result set to the given class.
	 * Run this if you expect your query to have a single row result set.
	 * You have to cast the result.
	 * It throws unchecked exceptions.
	 * 
	 */
	public Object executeQueryToObject(String query, List<Object> params, Class<?> clazz) throws SQLException {
		try (Connection conn = dataSource.getConnection();
				PreparedStatement statement = prepareStatementWithParams(conn, query, params);
				ResultSet rset = statement.executeQuery();) {
			while (rset.next()) {
				return DTOMapper.mapU(rset,clazz);
			}
		}
		return null;
	}
	
	public int executeUpdate(String query) throws SQLException {
		return executeUpdate(query, EMPTY_LIST);
	}

	public int executeUpdate(String query, List<Object> params) throws SQLException {
		try (Connection conn = dataSource.getConnection();
				PreparedStatement statement = prepareStatementWithParams(conn, query, params);) {
			return statement.executeUpdate();
		}
	}

	public int executeUpdate(Connection conn, String query, List<Object> params) throws SQLException {
		try (PreparedStatement statement = prepareStatementWithParams(conn, query, params);) {
			return statement.executeUpdate();
		}
	}
	
	public void executeAsync(Runnable runnable) {
		executorService.execute(runnable);
	}

	public String findPrimaryKey(String tableName) throws SQLException {
		String primaryKey = null;

		try (Connection conn = dataSource.getConnection();) {
			DatabaseMetaData meta = conn.getMetaData();
			ResultSet rset = meta.getPrimaryKeys(null, null, tableName);
			while (rset.next())
				primaryKey = rset.getString("COLUMN_NAME");
		}

		return primaryKey;
	}

	public void rollbackQuitely(Connection conn) {
		try {
			conn.rollback();
		} catch (SQLException e) {
			LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	}
	
	public List<String> findForeignKeys(String tableName) throws SQLException {
		List<String> foreignKeys = new ArrayList<>();
		try (Connection conn = dataSource.getConnection();) {
			DatabaseMetaData meta = conn.getMetaData();
			try (ResultSet rset = meta.getImportedKeys(null, null, tableName);) {
				while (rset.next()) {
					String foreignKey = rset.getString("FKCOLUMN_NAME");
					foreignKeys.add(foreignKey);
				}
			}
		}
		return foreignKeys;
	}

	public String findFoireignKeyReference(String tableName, String key) throws SQLException {
		try (Connection conn = dataSource.getConnection();) {
			DatabaseMetaData meta = conn.getMetaData();
			try (ResultSet rset = meta.getImportedKeys(null, null, tableName);) {
				while (rset.next()) {
					String referenceKey = rset.getString("PKCOLUMN_NAME");
					String referenceTable = rset.getString("PKTABLE_NAME");
					String foreignKey = rset.getString("FKCOLUMN_NAME");

					if (foreignKey.equals(key))
						return referenceKey + " : " + referenceTable;

				}
			}
		}
		return null;
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	
	public String getDriver() {
		return driver;
	}

	public Integer getLastGeneratedId(Connection conn) throws SQLException {
		return -1;
	}

}
