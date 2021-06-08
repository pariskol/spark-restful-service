package gr.kgdev.rest.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import gr.kgdev.dbconn.SqlConnector;
import gr.kgdev.dbconn.SqlQuery;
import gr.kgdev.dbconn.SqlQueryInsertConfig;
import gr.kgdev.dbconn.SqlQueryUpdateConfig;
import gr.kgdev.dbconn.SqliteConnector;
import gr.kgdev.model.AuthUser;
import gr.kgdev.model.ListOfMaps;
import gr.kgdev.model.User;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.rest.core.exceptions.ForbiddenException;
import gr.kgdev.rest.core.exceptions.LoginException;
import spark.Request;
import spark.Response;

public class CRUDRESTfulService extends SparkRESTfulService {

	private static final String SQLITE_SYNC_MODE_ENABLED = "1";
	
	private boolean isUpdateModeEnabled = false;
	private boolean isDbSecurityMethodEnabled = false;
	private AuthSystem authSystem;

	public AuthSystem initAuthSystem() {
		AuthSystem system = isDbSecurityMethodEnabled() ? 
				new BasicAuthDbDefinedAuthSystem(this, User.class) :
				new BasicAuthSystem(this, User.class);
		system.enablePasswordHashing(false);
		return system;
	}
	
	public static void main(String[] args) {
		SparkRESTfulService service = new CRUDRESTfulService();
		service.configure("localhost", 8090);
		service.init(new SqliteConnector("mplampla"));
		service.start();
	}
	
	@Override
	public void init(SqlConnector sqlConnector) {
		super.init(sqlConnector);
		authSystem = this.initAuthSystem();
	}

	@Override
	protected void declareFilters() {
		filter("/api/get/*", this::authorize);
		filter("/api/save/*", this::authorize);
		filter("/api/delete/*", this::authorize);
	}

	@Override
	protected void declareWebSockets() {
		JsonStreamingWebSocketHandler<Object> socketHandler = new JsonStreamingWebSocketHandler<>();
        websocket("/test", socketHandler);
        new Thread(() -> {
        	while(true) {
        		try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		socketHandler.sendMessage("{'message' : 'test'}");
        	}
        }).start();
	}
	
	@Override
	protected void declareEndpoints() {
		get("/api/get/:table", this::getData);
		post("/api/login", this::login);
		post("/api/save/:table", this::saveData);
		post("/api/delete/:table", this::deleteData);
		
		if (isDbSecurityMethodEnabled())
			get("/api/tables", this::getTables);
	}

	protected ListOfMaps getData(Request request, Response response) throws ForbiddenException, SQLException, BadRequestException {
		return this.getData(request, response, null);
	}
	
	protected ListOfMaps getData(Request request, Response response, String predefinedTable) throws ForbiddenException, SQLException, BadRequestException {
		SqlConnector sqlConnector = this.getSqlConnector();
		String table = predefinedTable == null ? request.params(":table") : predefinedTable;
		if (table == null)
			throw new BadRequestException("Table param is missing");

		// construct filter from request params
		StringBuilder whereFilter = new StringBuilder(" where 1=1");
		List<Object> params = new ArrayList<>();
		for (String queryParam : request.queryParams()) {
			whereFilter.append(" and " + queryParam + " = ?");
			params.add(request.queryParams(queryParam));
		}

		// execute
		String query = "select * from " + table + whereFilter.toString();
		getLogger().debug("Executing : " + query + " , [" + params.toString() + "]");

		return sqlConnector.executeQueryToList(query, params);
	}

	protected ListOfMaps getTables(Request request, Response response) throws SQLException {
		SqlConnector sqlConnector = this.getSqlConnector();
		String query = "select distinct name from editable_tables";
		getLogger().debug("Executing : " + query);

		return sqlConnector.executeQueryToList(query);
	}

	protected AuthUser login(Request request, Response response) throws SQLException, LoginException, BadRequestException {
		String authorizationHeader = request.headers("Authorization");
		if (authorizationHeader == null)
			throw new BadRequestException("No authorization header");
		
		byte[] decodedBytes = Base64.getDecoder().decode(authorizationHeader.replaceAll("Basic ", "").getBytes());
		String decoded = new String(decodedBytes);

		String[] userData = decoded.split(":");
		String username = userData[0];
		String password = userData[1];
		
		AuthUser user = authSystem.authenticateUser(username, password);

		if (user != null)
			return user;
		else
			throw new LoginException();
	}
	
	protected Object deleteData(Request request, Response response) throws ForbiddenException, SQLException, BadRequestException {
		return deleteData(request, response, null);
	}
	
	protected Object deleteData(Request request, Response response, String predefinedTable) throws ForbiddenException, SQLException, BadRequestException {
		SqlConnector sqlConnector = this.getSqlConnector();
		JSONObject jsonObject = new JSONObject(request.body());

		String table = predefinedTable == null ? request.params(":table") : predefinedTable;
		if (table == null)
			throw new BadRequestException("Table param is missing");
		
		String syncOperation = request.queryParams("sync");

		List<Object> params = new ArrayList<>();
		String primaryKey = sqlConnector.findPrimaryKey(table);
		String query = "delete from  " + table + " where " + primaryKey + " = ?";
		for (String key : jsonObject.keySet()) {
			if (key.equals(primaryKey)) {
				params.add(jsonObject.get(key));
			}
		}
		
		getLogger().debug("Executing : " + query + " , [" + params.toString() + "]");
		if (sqlConnector instanceof SqliteConnector && !isSyncMode(syncOperation)) {
			((SqliteConnector) sqlConnector).executeUpdateSerially(query, params);
			return JSONMessages.JM_PROCESSED;
		} else {
			sqlConnector.executeUpdate(query, params);
			return JSONMessages.JM_DELETED;
		}
	}

	protected Object saveData(Request request, Response response) throws ForbiddenException, SQLException, BadRequestException {
		return this.saveData(request, response, null);
	}
	
	protected Object saveData(Request request, Response response, String predefinedTable) throws ForbiddenException, SQLException, BadRequestException {
		SqlConnector sqlConnector = this.getSqlConnector();
		JSONObject jsonBody = new JSONObject(request.body());

		String table = predefinedTable == null ? request.params(":table") : predefinedTable;
		if (table == null)
			throw new BadRequestException("Table param is missing");
			
		String syncOperation = request.queryParams("sync");

		List<SqlQuery> queries = new ArrayList<>();

		int lastId = -1;

		if (isUpdateModeEnabled()) {
			SqlQueryUpdateConfig sqlConf = new SqlQueryUpdateConfig(table);
			try {
				String primaryKey = sqlConnector.findPrimaryKey(table);

				for (String key : jsonBody.keySet()) {
					if (!key.equals(primaryKey))
						sqlConf.addColumn(key, jsonBody.get(key));
				}

				sqlConf.addWhereColumn(primaryKey, jsonBody.get(primaryKey));

				queries.add(new SqlQuery(sqlConf));
			} catch (Exception e) {
				getLogger().debug("Could not update by primary key , " + e.getMessage());
			}
		}
		
		SqlQueryInsertConfig sqlConf = new SqlQueryInsertConfig(table);

		for (String key : jsonBody.keySet()) {
			sqlConf.addColumn(key, jsonBody.get(key));
		}

		this.beforeInsert(sqlConf, jsonBody);

		queries.add(new SqlQuery(sqlConf));

		Object message = null;
		if (sqlConnector instanceof SqliteConnector && !isSyncMode(syncOperation)) {
			((SqliteConnector) sqlConnector).executeInsertOrUpdateSerially(queries);
			message = JSONMessages.JM_PROCESSED;
		} else {
			for (SqlQuery query : queries) {
				getLogger().debug("Executing : " + query.getSQL() + " , [" + query.getParams().toString() + "]");
				try (Connection conn = sqlConnector.getConnection()) {
				if (sqlConnector.executeUpdate(conn, query.getSQL(), query.getParams()) > 0) {
					lastId  = sqlConnector.getLastGeneratedId(conn);
					break;
				}
				}
			}
			
			Map<String, Object> map = new HashMap<>();
			map.put("id", lastId);
			map.put("message", "Data has been updated");
			message = lastId != -1 ?  map : JSONMessages.JM_UPDATED;
		}

		return message;
	}
	
	/**
	 * Use this to further customize insert query before execution
	 * 
	 * @param sqlConf
	 * @param jsonBody
	 */
	protected void beforeInsert(SqlQueryInsertConfig sqlConf, JSONObject jsonBody) {
	}


	protected AuthUser authorize(Request request, Response response) throws SQLException, ForbiddenException, BadRequestException {
		return authorize(request, response, null);
	}
	
	/**
	 * For a table to be accessible , it must be declared in 'editable_tables' table
	 * along the with authentication method for each of the http methods to be
	 * served.
	 * 
	 * @param sqlConnector
	 * @param request
	 * @param response
	 * @param table
	 * @param httpMethod
	 * @throws SQLException
	 * @throws ForbiddenException
	 * @throws BadRequestException 
	 */
	protected AuthUser authorize(Request request, Response response, String predefinedTable) throws SQLException, ForbiddenException, BadRequestException {
		return authSystem.authorize(request, response, predefinedTable);
	}

	/**
	 * Determines if update executor of sqlite should be used.
	 * 
	 * @param syncOperation
	 * @return
	 */
	protected boolean isSyncMode(String syncOperation) {
		return SQLITE_SYNC_MODE_ENABLED.equals(syncOperation);
	}

	protected boolean isDbSecurityMethodEnabled() {
		return isDbSecurityMethodEnabled;
	}


	/**
	 * By enabling db security method , use must declare the following table to your db
	 * 	CREATE TABLE editable_tables (
	 *		ID INTEGER PRIMARY KEY AUTOINCREMENT,
	 *		NAME TEXT,
	 *		AUTH_POLICY TEXT,
	 *		METHOD TEXT -- GET,POST etc
	 *	)
	 *
	 * and specify the accesible tables. Default value is true
	 * 
	 * @param enable
	 */
	public void enableDbSecurityMethod(Boolean enable) {
		isDbSecurityMethodEnabled = enable;
	}

	public void enableUpdateMode(Boolean enable) {
		isUpdateModeEnabled = enable;
	}

	protected boolean isUpdateModeEnabled() {
		return isUpdateModeEnabled;
	}

	public AuthSystem getAuthSystem() {
		return authSystem;
	}

	@Override
	protected void initStaicFilesRoute() {
		//NO USE
	}
}
