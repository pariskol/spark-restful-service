package gr.kgdev.dbconn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import gr.kgdev.utils.PropertiesLoader;

public class MysqlConnector extends SqlConnector {

	public MysqlConnector(String url, String user, String password) {
		super("com.mysql.cj.jdbc.Driver", url, user, password);
	}

	
	@Override
	protected DataSource initDatasource() {
		BasicDataSource dbcp2DataSource = new BasicDataSource();
		dbcp2DataSource.setDriverClassName(this.getDriver());
		dbcp2DataSource.setUrl(this.getUrl());
		dbcp2DataSource.setUsername(this.getUser());
		dbcp2DataSource.setPassword(this.getPassword());
		dbcp2DataSource.setInitialSize((Integer) PropertiesLoader.getProperty("db.pool.initialsize", Integer.class, 4));
		dbcp2DataSource.setMaxIdle((Integer) PropertiesLoader.getProperty("db.pool.idlesize", Integer.class, 16));
		dbcp2DataSource.setMaxTotal((Integer) PropertiesLoader.getProperty("db.pool.maxsize", Integer.class, 32));
		dbcp2DataSource.setMaxWaitMillis((Integer) PropertiesLoader.getProperty("db.pool.maxwaitmillis", Integer.class, 10000));
		return dbcp2DataSource;
	}

	
	@Override
	public Integer getLastGeneratedId(Connection conn) throws SQLException {
		AtomicInteger lastId = new AtomicInteger();
		this.executeQuery(conn, "select last_insert_id()", rset -> {
			lastId.set(rset.getInt(1));
		});
		return lastId.get();
	}
}
