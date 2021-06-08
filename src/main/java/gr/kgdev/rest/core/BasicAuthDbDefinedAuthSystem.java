package gr.kgdev.rest.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import gr.kgdev.dbconn.SqlConnector;
import gr.kgdev.model.AuthUser;
import gr.kgdev.model.DBTable;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.rest.core.exceptions.ForbiddenException;
import gr.kgdev.utils.dtomapper.DTOMapper;
import spark.Request;
import spark.Response;

public class BasicAuthDbDefinedAuthSystem extends BasicAuthSystem {

	private HashMap<String, List<DBTable>> tablesMap;

	public BasicAuthDbDefinedAuthSystem(SparkRESTfulService service, Class<? extends AuthUser> authUserClass) {
		super(service, authUserClass);
		initAccesibleTables();
	}
	
	/**
	 * Get the list of accessible tables from database.
	 * Use this only if db security mode is enabled.
	 * 
	 * CREATE TABLE IF NOT EXISTS editable_tables (
	 *		ID INTEGER PRIMARY KEY AUTOINCREMENT,
	 *		NAME TEXT,
	 *		AUTH_POLICY TEXT,
	 *		METHOD TEXT -- GET,POST etc
	 *	)
	 * 
	 * @param sqlConnector
	 */
	private void initAccesibleTables() {
		
		SqlConnector sqlConnector = getService().getSqlConnector();
		tablesMap = new HashMap<>();
		try {
			if (sqlConnector.executeUpdate(AuthPolicy.getEditableTablesCreateQuery(sqlConnector)) > 0)
				getService().getLogger().info("table 'editable_tables' has been created");

			sqlConnector.executeQuery("select * from editable_tables", rset -> {
				DBTable table = (DBTable) DTOMapper.mapU(rset, DBTable.class);
				if (!tablesMap.containsKey(table.getName())) {
					List<DBTable> tables = new ArrayList<>();
					tables.add(table);
					tablesMap.put(table.getName(), tables);
				} else {
					tablesMap.get(table.getName()).add(table);
				}
			});
			
			if (tablesMap.isEmpty())
				getService().getLogger().warn("table 'editable_tables' is empty! Check your database and restart service!");
		} catch (Throwable e) {
			getService().getLogger().error("Could not initialize tables map, ensure that 'editable_tables' table exists!", e);
		}
	}

	@Override
	public AuthUser authorize(Request request, Response response) throws SQLException, ForbiddenException, BadRequestException {
		return authorize(request, response, null);
	}
	
	@Override
	public AuthUser authorize(Request request, Response response, Object predefinedTable) throws SQLException, ForbiddenException, BadRequestException {

		assert (predefinedTable instanceof String);
		
		String table = (String) predefinedTable;
		if (table == null) {
			table = Pattern.compile(".*/").matcher(request.pathInfo()).replaceAll("");
			if (table == null)
					throw new BadRequestException("Table param is missing");
		}
		
		String httpMethod = request.requestMethod();
		
		// if table is not declared in 'editable_tables' it is not accessible.
		if (!tablesMap.keySet().contains(table))
			throw new ForbiddenException("Table is not accesible");
		
		if (isTableProtectedForMethod(table, httpMethod)) {
			return super.authorize(request, response);
		}
		
		return null;
	}

	private boolean isTableProtectedForMethod(String name, String httpMethod) {
		List<DBTable> tables = tablesMap.get(name);
		if (tables != null) {
			for (DBTable table : tables) {
				if (table.getHttpMethod().equals(httpMethod)) {
					return table.getAuthPolicy().equals(AuthPolicy.BASIC_AUTH);
				}
			}
		}
		return false;
	}
	
}
