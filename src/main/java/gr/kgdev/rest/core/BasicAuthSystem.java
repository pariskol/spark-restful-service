package gr.kgdev.rest.core;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

import gr.kgdev.dbconn.SqlConnector;
import gr.kgdev.model.AuthUser;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.rest.core.exceptions.ForbiddenException;
import spark.Request;
import spark.Response;

public class BasicAuthSystem implements AuthSystem {

	private SparkRESTfulService service;
	private Class<? extends AuthUser> authUserClass;
	private boolean doHashPassword = true;
	
	public BasicAuthSystem(SparkRESTfulService service, Class<? extends AuthUser> authUserClass) {
		this.service = service;
		this.authUserClass = authUserClass;
		
		SqlConnector sqlConnector = getService().getSqlConnector();
		try {
			if (sqlConnector.executeUpdate(AuthPolicy.getUsersCreateQuery(sqlConnector)) > 0)
				getService().getLogger().info("table 'users' has been created!");

		} catch (SQLException e) {
			getService().getLogger().error("Could not create 'users' table!", e);
		}
	}
	
	@Override
	public AuthUser authorize(Request request, Response response, Object predefinedTable)
			throws SQLException, ForbiddenException, BadRequestException {
		return authorize(request, response);
	}
	
	@Override
	public AuthUser authorize(Request request, Response response) throws SQLException, ForbiddenException, BadRequestException {

		String authorizationHeader = request.headers("Authorization");
		if (authorizationHeader == null) {
			throw new BadRequestException("No authorization header");
		}
		byte[] decodedBytes = Base64.getDecoder().decode(authorizationHeader.replaceAll("Basic ", "").getBytes());
		String decoded = new String(decodedBytes);

		String[] userData = decoded.split(":");
		String username = userData[0];
		String password = userData[1];
		
		AuthUser user = this.authenticateUser(username, password);

		if (user == null) {
			throw new ForbiddenException("No access for user");
		}
		
		return user;
	}

	@Override
	public String hashPassword(String password) {
		return doHashPassword ? DigestUtils.sha1Hex(password) : password;
	}

	@Override
	public void enablePasswordHashing(boolean doHashPassword) {
		this.doHashPassword = doHashPassword;
	}
	
	@Override
	public AuthUser authenticateUser(String username, String password)
			throws SQLException {
		password = this.hashPassword(password);
		return (AuthUser) service.getSqlConnector()
								 .executeQueryToObject("select * from users where NAME = ? and PASSWORD = ?",
										 Arrays.asList(username, password),
										 authUserClass);
	}
	
	protected SparkRESTfulService getService() {
		return service;
	}
}
