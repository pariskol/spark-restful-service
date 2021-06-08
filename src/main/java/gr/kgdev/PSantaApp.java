package gr.kgdev;

import org.slf4j.LoggerFactory;

import gr.kgdev.dbconn.SqliteConnector;
import gr.kgdev.rest.services.PSantaRESTfulService;
import gr.kgdev.utils.PropertiesLoader;

public class PSantaApp {

	public static void main(String[] args) {
		PropertiesLoader.setLogger(LoggerFactory.getLogger("spark"));
		String ip = (String) PropertiesLoader.getProperty("server.ip", String.class);
		Integer port = (Integer) PropertiesLoader.getProperty("server.port", Integer.class);
	    String databaseUrl = (String) PropertiesLoader.getProperty("db.url", String.class);
	    Boolean sendInstantMails = (Boolean) PropertiesLoader.getProperty("mail.instant.send", Boolean.class, false);
		
	    PSantaRESTfulService restService = new PSantaRESTfulService();
	    restService.configure(ip, port);
	    restService.enableInstantEmails(sendInstantMails);
		restService.init(new SqliteConnector(databaseUrl),
								 (String) PropertiesLoader.getProperty("keystore.path", String.class),
								 (String) PropertiesLoader.getProperty("keystore.pass", String.class));
		restService.start();
	}
}
