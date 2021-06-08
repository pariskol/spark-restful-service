package gr.kgdev.rest.services;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import gr.kgdev.dbconn.SqlConnector;
import gr.kgdev.dbconn.SqlQuery;
import gr.kgdev.dbconn.SqliteConnector;
import gr.kgdev.model.AuthUser;
import gr.kgdev.model.ListOfMaps;
import gr.kgdev.model.User;
import gr.kgdev.model.batmobile.BatmobileUser;
import gr.kgdev.model.batmobile.Message;
import gr.kgdev.rest.core.AuthSystem;
import gr.kgdev.rest.core.BasicAuthDbDefinedAuthSystem;
import gr.kgdev.rest.core.BasicAuthSystem;
import gr.kgdev.rest.core.CRUDRESTfulService;
import gr.kgdev.rest.core.EmailSenderService;
import gr.kgdev.rest.core.EmbeddedWebSiteService;
import gr.kgdev.rest.core.FileUploadingService;
import gr.kgdev.rest.core.JSONMessages;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.rest.core.exceptions.ForbiddenException;
import gr.kgdev.rest.core.exceptions.LoginException;
import gr.kgdev.utils.MappingUtils;
import gr.kgdev.utils.PropertiesLoader;
import spark.Request;
import spark.Response;

public class BATMobileRESTfulService extends CRUDRESTfulService implements EmbeddedWebSiteService, EmailSenderService, FileUploadingService {

	public BATMobileRESTfulService() {
		this.initStaicFilesRoute();
		this.enableUpdateMode(true);
		this.enableDbSecurityMethod(true);
	}

	@Override
	public AuthSystem initAuthSystem() {
		return isDbSecurityMethodEnabled() ? 
				new BasicAuthDbDefinedAuthSystem(this, BatmobileUser.class) :
				new BasicAuthSystem(this, BatmobileUser.class);
	}
	
	@Override
	protected void initStaicFilesRoute() {
		getSparkService().staticFiles.location("/static/");
		getSparkService().staticFiles.header("Cache-Control", "max-age=0");
		getSparkService().staticFiles.expireTime(600); // ten minutes
	}

	@Override
	protected void declareFilters() {
		super.declareFilters();
		filter("/api/download/*", (request, response) -> super.login(request, response));
		filter("/api/upload", (request, response) -> super.login(request, response));
	}
	
	@Override
	protected void declareEndpoints() {
		declareRootPath(this);
		get("/api/get/messages", (request, response) -> getMessages(request, response, null));
		get("/api/get/unread_messages", (request, response) -> getData(request, response, "unread_messages"));
		get("/api/get/active_users_details",
				(request, response) -> getData(request, response, "active_users_details"));
		get("/api/get/notifications", (request, response) -> getData(request, response, "notifications"));
		get("/api/get/files", this::getFiles);
		post("/api/save/message", (request, response) -> saveData(request, response, "messages"));
		post("/api/login", this::login);
		post("/api/logout", this::logout);
		post("/api/signup", this::signUp);
		post("/api/upload", this::uploadFile);
		get("/api/download/:file", this::downloadFile);
	}
	
	private List<Object> getFiles(Request request, Response response) throws ForbiddenException, SQLException, BadRequestException {
		if (request.queryParams("USER_ID") == null)
			throw new BadRequestException("'USER_ID' param is missing");
		
		Integer userId = Integer.parseInt(request.queryParams("USER_ID"));
		AuthUser user = authorize(request, response, "files");
		if (!userId.equals(user.getId()))
			throw new ForbiddenException("Requested files are not accessible");
		
		ListOfMaps data = this.getData(request, response, "files");
		return data.stream().map(x -> x.get("FILE_NAME")).collect(Collectors.toList());
	}

	private void markMessagesAsRead(AuthUser user, List<Message> messages) throws SQLException {
		SqlConnector sqlConnector = this.getSqlConnector();
		List<SqlQuery> queries = new ArrayList<>();

		for (Message message : messages) {

			if (message.getStatus().equals(Message.STATUS_UNREAD)) {
				List<Object> params = null;
				params = new ArrayList<>();
				params.add(message.getId());
				params.add(user.getId());
				String sql = "update messages set SHOWN = 1 where ID = ? and TO_USER = ?";

				queries.add(new SqlQuery(sql, params));
			}
		}

		if (sqlConnector instanceof SqliteConnector) {
			((SqliteConnector) sqlConnector).executeInsertOrUpdateSerially(queries);
		} else {
			for (SqlQuery query : queries) {
				sqlConnector.executeUpdate(query.getSQL(), query.getParams());
			}
		}

	}

	@SuppressWarnings("unchecked")
	private List<Message> getMessages(Request request, Response response, String messagesTable)
			throws SQLException, ForbiddenException, BadRequestException {
		SqlConnector sqlConnector = this.getSqlConnector();
		String table = (String) PropertiesLoader.getProperty("sql.messages", String.class, "messages");
		table = messagesTable != null ? messagesTable : table;
		AtomicBoolean canReadMessages = new AtomicBoolean(false);

		AuthUser user = authorize(request, response, table);

		Integer toUser = Integer.parseInt(request.queryParams("TO_USER"));
		Integer fromUser = Integer.parseInt(request.queryParams("FROM_USER"));

		final Set<String> set = new HashSet<>();

		if (toUser > 0) {
			getSqlConnector().executeQuery(
					"select * from groups g, group_members gm where g.id = ? and gm.group_id = ? and gm.user_id = ?",
					Arrays.asList(toUser, toUser, fromUser), rset -> {
						canReadMessages.set(true);
						set.addAll(request.queryParams().stream().filter(x -> !x.equals("FROM_USER"))
								.collect(Collectors.toSet()));
					});

			if (!canReadMessages.get())
				set.addAll(request.queryParams());
		} else {
			set.addAll(request.queryParams());
		}

		// construct filter from request params
		StringBuilder whereFilter = new StringBuilder(" where 1=1");
		List<Object> params = new ArrayList<>();
		for (String queryParam : set) {
			Object value = request.queryParams(queryParam);
			// if messages requested are not related to user, throw exception
			if (queryParam.equals("FROM_USER") || queryParam.equals("TO_USER")) {
				if (Integer.parseInt(value.toString()) == user.getId())
					canReadMessages.set(true);
			}

			if (queryParam.equals("FROM_ID")) {
				whereFilter.append(" and ID > ?");
			} else {
				whereFilter.append(" and " + queryParam + " = ?");
			}
			params.add(value);
		}

		if (!canReadMessages.get())
			throw new ForbiddenException("No access");

		// execute
		String query = "select * from " + table + whereFilter.toString() + " order by ID";
		getLogger().debug("Executing : " + query + " , [" + params.toString() + "]");
		
		List<Message> messages = (List<Message>) sqlConnector.executeQueryToList(query, params, Message.class);

		this.markMessagesAsRead(user, messages);

		return messages;
	}

	protected String logout(Request request, Response response)
			throws SQLException, ForbiddenException, BadRequestException {
		SqlConnector sqlConnector = this.getSqlConnector();
		AuthUser user = authorize(request, response, "active_users");

		int rowsAffected = sqlConnector.executeUpdate("delete from active_users where USER_ID = ?",
				Arrays.asList(user.getId()));

		if (rowsAffected > 0)
			return JSONMessages.JM_USER_LOGGED_OUT;
		else
			return JSONMessages.JM_NO_ACTIVE_USER_FOUND;
	}

	@Override
	protected AuthUser login(Request request, Response response)
			throws SQLException, LoginException, BadRequestException {
		AuthUser user = super.login(request, response);
		if (response.status() == 200) {
			this.getSqlConnector().executeUpdate("insert into active_users (USER_ID) values (?)",
					Arrays.asList(user.getId()));
		}
		return user;
	}

	private String signUp(Request request, Response response) throws Exception {
		JSONObject json = new JSONObject(request.body());
		User newUser = MappingUtils.convertMapToUser(json.toMap());
		List<Object> params = new ArrayList<>();
		params.add(newUser.getName());
		params.add(getAuthSystem().hashPassword(newUser.getPassword()));
		params.add(newUser.getEmail());
		params.add(0);
		try {
			this.getSqlConnector().executeUpdate("insert into users (NAME, PASSWORD, EMAIL, STATUS) values (?,?,?,?)",
					params);
		} catch (SQLException e) {
			response.status(400);
			return JSONMessages.JM_NOT_AVAILABLE_USERNAME;
		}
		this.sendEmail(json.toMap());

		return JSONMessages.JM_PROCESSED;
	}

	@Override
	public void sendEmail(Map<String, Object> map) throws MessagingException {
		User newUser = MappingUtils.convertMapToUser(map);
		String propsFile = "rest.properties";
		Properties props = PropertiesLoader.getPropertiesFromFile(propsFile);
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						(String) PropertiesLoader.getProperty(propsFile, "mail.auth.user", String.class),
						(String) PropertiesLoader.getProperty(propsFile, "mail.auth.pass", String.class));
			}
		});
		javax.mail.Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress((String) PropertiesLoader.getProperty("mail.from", String.class)));
		msg.setRecipients(javax.mail.Message.RecipientType.TO,
				InternetAddress.parse(newUser.getEmail(), false));
		msg.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse("", false));
		msg.setSubject("Welcome to batmobile " + map.get("username") + "!");
		msg.setText("From now you will enjoy 100% free and private communication with your friends!\n"
				+ "Your credentials:\n"
				+ "Username: " + newUser.getName() + "\n"
				+ "Password: " + newUser.getPassword() + "\n"
				+ "\n"
				+ "Thank you for your registration , you can now use batmobile!\n");
		msg.setSentDate(new Date());
		Transport.send(msg);
		}
		
	@Override
	public String uploadFile(Request request, Response response) throws IOException, ServletException, SQLException, BadRequestException {
		String forUserStr = request.queryParams("TO_USER");
		if (StringUtils.isEmpty(forUserStr))
			throw new BadRequestException("'TO_USER' param is missing");
		
		
		BatmobileUser user = (BatmobileUser) getSqlConnector().executeQueryToObject("select * from users where name = ?", 
				Arrays.asList(forUserStr), BatmobileUser.class);
		
		if (user == null)
			throw new BadRequestException("Receiver does not exists");
		
		FileUploadingService.super.uploadFile(request, response);
		
		String fName = request.raw().getPart("file").getSubmittedFileName();
		Path out = Paths.get(getUploadLoaction() + "/" + fName);
		this.getSqlConnector().executeUpdate("insert into files (FILE_NAME, USER_ID) values (?,?)", 
				Arrays.asList(out.toString(), user.getId()));

		return JSONMessages.create("File has been uploaded");
	}
}
