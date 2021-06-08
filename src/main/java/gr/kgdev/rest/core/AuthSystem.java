package gr.kgdev.rest.core;

import java.sql.SQLException;

import gr.kgdev.model.AuthUser;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.rest.core.exceptions.ForbiddenException;
import spark.Request;
import spark.Response;

public interface AuthSystem {

	/**
	 * For a table to be accessible , it must be declared in 'editable_tables' table
	 * along the with authentication method for each of the http methods to be
	 * served.
	 * 
	 * @param request
	 * @param response
	 * @throws SQLException
	 * @throws ForbiddenException
	 * @throws BadRequestException
	 */
	public AuthUser authorize(Request request, Response response)
			throws SQLException, ForbiddenException, BadRequestException;

	/**
	 * For a table to be accessible , it must be declared in 'editable_tables' table
	 * along the with authentication method for each of the http methods to be
	 * served.
	 * 
	 * @param request
	 * @param response
	 * @param table
	 * @throws SQLException
	 * @throws ForbiddenException
	 * @throws BadRequestException
	 */
	public AuthUser authorize(Request request, Response response, Object extraData)
			throws SQLException, ForbiddenException, BadRequestException;

	public AuthUser authenticateUser(String username, String password) throws SQLException;
	
	public String hashPassword(String password);
	
	default public void enablePasswordHashing(boolean enable) {}
}
