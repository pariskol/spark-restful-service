package gr.kgdev.rest.services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;

import gr.kgdev.model.ListOfMaps;
import gr.kgdev.rest.core.CRUDRESTfulService;
import gr.kgdev.rest.core.EmailSenderService;
import gr.kgdev.rest.core.EmbeddedWebSiteService;
import gr.kgdev.rest.core.FileUploadingService;
import gr.kgdev.rest.core.JSONMessages;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.utils.PropertiesLoader;
import gr.kgdev.utils.dtomapper.DTOMapper;
import spark.Request;
import spark.Response;

public class ThanouRESTfulService extends CRUDRESTfulService implements EmbeddedWebSiteService, EmailSenderService, FileUploadingService {

	public ThanouRESTfulService() {
		super();
		this.enableUpdateMode(false);
		this.enableDbSecurityMethod(true);
	}
	
	@Override
	protected void initStaicFilesRoute() {
		getSparkService().staticFiles.location("/site/");
		getSparkService().staticFiles.header("Cache-Control", "max-age=0");
		getSparkService().staticFiles.expireTime(600); // ten minutes
	}
	
	@Override
	protected void declareFilters() {
		super.declareFilters();
		filter("/api/action/*", this::login);
	}
	
	@Override
	protected void declareEndpoints() {
		declareRootPath(this);
		post("/api/action/upload",this::uploadFile);
		post("/api/action/sendMessage", this::sendMessageViaEmail);
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
		String fullName = map.get("firstName") != null ?  map.get("firstName").toString() : "";
		fullName += map.get("lastName") != null ?  " " + map.get("lastName").toString() : "";
		msg.setSubject(fullName + " > FCP");
		
		String message = "Όνοματεπώνυμο : " + fullName;
		message += map.get("phone") != null ? "\nΤηλέφωνο : " + map.get("phone") : "";
		message += map.get("email") != null ? "\nEmail : " + map.get("email") : "";
        message += "\n\n" + map.get("message") + "\n\nΗμερομηνία παραλαβής : " + map.get("deliveryDate");
        
		msg.setText(message);
		msg.setSentDate(new Date());

		MimeBodyPart textBodyPart = new MimeBodyPart();
		textBodyPart.setText(message);
		
        MimeBodyPart attachementBodyPart = new MimeBodyPart();

		Multipart multipart = new MimeMultipart();
        
        String file = map.get("filePath").toString();
        String[] split = file.split("/");
        String fileName = split[split.length -1];
        DataSource source = new FileDataSource(file);
        attachementBodyPart.setDataHandler(new DataHandler(source));
        attachementBodyPart.setFileName(fileName);
        
        multipart.addBodyPart(textBodyPart);
        multipart.addBodyPart(attachementBodyPart);

        msg.setContent(multipart);
		Transport.send(msg);
	}

	private String sendMessageViaEmail(Request request, Response response) throws BadRequestException, SQLException, MessagingException, FailedLoginException {
		
		String messageIdStr = request.queryParams("messageId");
		if (StringUtils.isEmpty(messageIdStr))
			throw new BadRequestException("'messageId' param is missing");
		
		Integer messageId = Integer.parseInt(messageIdStr);
		
		ListOfMaps messages = new ListOfMaps();
		getSqlConnector().executeQuery("select * from v_messages_with_files where id = ?",
			Arrays.asList(messageId),
			rset -> {
				Map<String, Object> message = DTOMapper.mapU(rset);
				messages.add(message);
			});
		
		
		if (messages.isEmpty())
			throw new BadRequestException("MessageId not found");
			
		Map<String, Object> map = messages.get(0);
		map.put("files", messages.stream().map(x -> x.get("filePath")).collect(Collectors.toList()));
		this.sendEmail(map);
		
		return JSONMessages.create("Message with id : " + messageId + " has been sent via email");
	}
	
	@Override
	public String uploadFile(Request request, Response response) throws IOException, ServletException, SQLException, BadRequestException {
		String messageIdStr = request.queryParams("messageId");
		if (StringUtils.isEmpty(messageIdStr))
			throw new BadRequestException("'messageId' param is missing");
		
		Integer messageId = Integer.parseInt(messageIdStr);
		
		FileUploadingService.super.uploadFile(request, response);
		
		String fName = request.raw().getPart("file").getSubmittedFileName();
		Path out = Paths.get(getUploadLoaction() + "/" + fName);
		this.getSqlConnector().executeUpdate("insert into message_files (messageId, filePath) values (?,?)", 
				Arrays.asList(messageId, out.toString()));

		return JSONMessages.create("File has been uploaded");
	}

}
