package gr.kgdev.dbconn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.LoggerFactory;

import gr.kgdev.utils.PropertiesLoader;

public class SqliteConnector extends SqlConnector {

	private LinkedBlockingQueue<Object> updateQueriesQueue;
	
	public SqliteConnector(String database) {
		super("org.sqlite.JDBC", "jdbc:sqlite:" + database, null, null);
		this.updateQueriesQueue = new LinkedBlockingQueue<>();
		this.startUpdateExecutor();
	}

	private void startUpdateExecutor() {
		Thread updatesExecutorThread = new Thread(() -> {
			try (Connection conn = this.getConnection();) {
				while(!Thread.currentThread().isInterrupted()) {
					Object job;
					try {
						job = updateQueriesQueue.take();
						if (job instanceof SqlQuery) {
							LoggerFactory.getLogger(getClass()).debug("Executing update " + job.toString());
							SqlQuery query =  (SqlQuery) job; 
							super.executeUpdate(conn, query.getSQL(), query.getParams());
						}
						// update or insert mode
						else if (job instanceof List<?>) {
							@SuppressWarnings("unchecked")
							List<SqlQuery> queries = (List<SqlQuery>) job;
							for (SqlQuery query : queries) {
								LoggerFactory.getLogger(getClass()).debug("Executing update " + query);
								int rowsAffected = super.executeUpdate(conn, query.getSQL(), query.getParams());
								if (rowsAffected > 0) {
									LoggerFactory.getLogger(getClass()).debug(rowsAffected + " rows affected");
									break;
								}
							}
						}
					} catch (Throwable e) {
						LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
					}
				}
			} catch (SQLException e1) {
				LoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
			}
		}, "SQLite updates executor");
		updatesExecutorThread.setDaemon(true);
		updatesExecutorThread.start();
	}

//	@Override
//	protected DataSource initDatasource() {
//		SQLiteDataSource datasource = new SQLiteDataSource();
//		datasource.setUrl(this.getUrl());
//		return datasource;
//	}
	
	@Override
	protected DataSource initDatasource() {
		BasicDataSource dbcp2DataSource = new BasicDataSource();
		dbcp2DataSource.setDriverClassName(this.getDriver());
		dbcp2DataSource.setUrl(this.getUrl());
		dbcp2DataSource.setInitialSize((Integer) PropertiesLoader.getProperty("db.pool.initialsize", Integer.class, 4));
		dbcp2DataSource.setMaxIdle((Integer) PropertiesLoader.getProperty("db.pool.idlesize", Integer.class, 16));
		dbcp2DataSource.setMaxTotal((Integer) PropertiesLoader.getProperty("db.pool.maxsize", Integer.class, 32));
		dbcp2DataSource.setMaxWaitMillis((Integer) PropertiesLoader.getProperty("db.pool.maxwaitmillis", Integer.class, 10000));
		return dbcp2DataSource;
	}
	
	public int executeUpdateSerially(String query, List<Object> params) throws SQLException {
		try {
			this.updateQueriesQueue.put(new SqlQuery(query, params));
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
		return 2;
	}
	
	public int executeInsertOrUpdateSerially(List<SqlQuery> queries) throws SQLException {
		try {
			this.updateQueriesQueue.put(queries);
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
		return 2;
	}
	
}
