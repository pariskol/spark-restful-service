package gr.kgdev;

import org.slf4j.LoggerFactory;

import gr.kgdev.dbconn.MysqlConnector;
import gr.kgdev.rest.core.SparkRESTfulService;
import gr.kgdev.rest.services.ThanouRESTfulService;
import gr.kgdev.utils.PropertiesLoader;

public class ThanouApp {

	public static void main(String[] args) {
		PropertiesLoader.setLogger(LoggerFactory.getLogger("spark"));
		String ip = (String) PropertiesLoader.getProperty("server.ip", String.class);
		Integer port = (Integer) PropertiesLoader.getProperty("server.port", Integer.class);
	    String dbUrl = (String) PropertiesLoader.getProperty("db.url", String.class);
	    String dbUsername = (String) PropertiesLoader.getProperty("db.username", String.class);
	    String dbPassword = (String) PropertiesLoader.getProperty("db.password", String.class);
		
	    SparkRESTfulService restService = new ThanouRESTfulService();
	    restService.configure(ip, port);
		restService.init(new MysqlConnector(dbUrl, dbUsername, dbPassword),
								 (String) PropertiesLoader.getProperty("keystore.path", String.class),
								 (String) PropertiesLoader.getProperty("keystore.pass", String.class));
		restService.start();
	}
}
