package gr.kgdev.rest.services;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import gr.kgdev.dbconn.SqlQueryInsertConfig;
import gr.kgdev.rest.core.CRUDRESTfulService;
import gr.kgdev.rest.core.EmailSenderService;
import gr.kgdev.rest.core.EmbeddedWebSiteService;
import gr.kgdev.utils.PropertiesLoader;

public class PSantaRESTfulService extends CRUDRESTfulService implements EmbeddedWebSiteService, EmailSenderService {
	
	protected boolean sendInstantEmail = false;

	public PSantaRESTfulService() {
		super();
		this.initStaicFilesRoute();
		this.enableUpdateMode(true);
	    this.enableInstantEmails(true);
		this.enableDbSecurityMethod(true);
	}

	public void enableInstantEmails(Boolean enable) {
		this.sendInstantEmail = enable;
	}
	
	@Override
	protected void initStaicFilesRoute() {
		getSparkService().staticFiles.location("/");
		getSparkService().staticFiles.header("Cache-Control", "max-age=0");
		getSparkService().staticFiles.expireTime(600); // ten minutes
	}
	
	@Override
	protected void declareEndpoints() {
		declareRootPath(this);
		super.declareEndpoints();

	}
	
	@Override
	public void sendEmail(Map<String, Object> map) throws MessagingException {
		String propsFile = "rest.properties";
		Properties props = PropertiesLoader.getPropertiesFromFile(propsFile);
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						(String) PropertiesLoader.getProperty(propsFile, "mail.auth.user", String.class),
						(String) PropertiesLoader.getProperty(propsFile, "mail.auth.pass", String.class));
			}
		});
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress((String) PropertiesLoader.getProperty("mail.from", String.class)));
		msg.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse((String) PropertiesLoader.getProperty("mail.to", String.class), false));
		msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse("", false));
		msg.setSubject(map.get("name").toString() + " > psantamouris.gr");
		msg.setText("\n\nfrom : " + map.get("email") + " " + new Date() + "\n\n" + map.get("question"));
		msg.setSentDate(new Date());
		Transport.send(msg);
	}
	
	@Override
	protected void beforeInsert(SqlQueryInsertConfig sqlConf, JSONObject jsonBody) {
		if (sendInstantEmail && sqlConf.getTable().equals("messages")) {
			try {
				this.sendEmail(jsonBody.toMap());
				sqlConf.addColumn("send", 1);
			} catch (Throwable e) {
				LoggerFactory.getLogger("spark").error("Fail to send message", e);
			}
		}
	}
}
