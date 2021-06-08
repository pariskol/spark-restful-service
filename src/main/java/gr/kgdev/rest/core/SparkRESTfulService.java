package gr.kgdev.rest.core;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.kgdev.dbconn.SqlConnector;
import gr.kgdev.rest.core.exceptions.BadRequestException;
import gr.kgdev.rest.core.exceptions.ForbiddenException;
import gr.kgdev.rest.core.exceptions.LoginException;
import gr.kgdev.utils.PropertiesLoader;
import spark.Route;
import spark.Service;

public abstract class SparkRESTfulService {

	private  Logger logger = LoggerFactory.getLogger("spark");
	
	private Service spark;

	private  String keystorePath;
	private  String keystorePass;
	private SqlConnector sqlConnector;
	protected  JsonTransformer jsonTransformer;
	private boolean isProperlyInitialized = false;

	public SparkRESTfulService() {
		Integer max = (Integer) PropertiesLoader.getProperty("jetty.threadpool.max", Integer.class, 32);
		Integer min = (Integer) PropertiesLoader.getProperty("jetty.threadpool.min", Integer.class, 4);
		Integer timeoutMillis = (Integer) PropertiesLoader.getProperty("jetty.threadpool.timeoutmillis", Integer.class, 60000);

		this.spark = Service.ignite();
		this.spark.threadPool(max, min, timeoutMillis);
		this.jsonTransformer = new JsonTransformer();
	}
	
	public void init(SqlConnector sqlConnector, String p12Path, String p12Pass) {
		keystorePath = p12Path;
		keystorePass = p12Pass;
		init(sqlConnector);
	}
	
	public void init(SqlConnector sqlConnector) {
		this.sqlConnector = sqlConnector;

		if (keystorePath != null && !keystorePath.isEmpty())
			spark.secure(keystorePath, keystorePass, null, null);
		
		declareWebSockets();

		spark.initExceptionHandler((e) -> logger.error(e.getMessage(), e));
		
		spark.exception(Exception.class, (exception, request, response) -> {
			String tag = UUID.randomUUID().toString();
			logger.error(tag + " " + exception.getMessage(), exception);
			if (exception instanceof ForbiddenException) {
				String message = JSONMessages.create(JSONMessages.FORBIDDEN, tag);
				response.body(message);
				response.status(403);
			}
			else if (exception instanceof LoginException) {
				String message = JSONMessages.create(JSONMessages.UNAUTHORIZED, tag);
				response.body(message);
				response.status(401);
			}
			else if (exception instanceof BadRequestException) {
				response.body(JSONMessages.create(exception.getMessage(), tag));
				response.status(400);
			}
			else {
				String message = JSONMessages.create(JSONMessages.INTERNAL_ERROR, tag);
				response.body(message);
				response.status(500);
			}
		
		});
		
		spark.notFound((request, response) -> JSONMessages.create(JSONMessages.NOT_FOUND, UUID.randomUUID().toString()));
		
		// CORS filter
		spark.options("/*", (request, response) -> {
		    String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
		    if (accessControlRequestHeaders != null) {
		        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
		    }

		    String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
		    if (accessControlRequestMethod != null) {
		        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
		    }
		    return "OK";
		});
		
		spark.before((request, response) -> {
			response.header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Allow-Headers", "*");
			response.header("Access-Control-Allow-Credentials", "true");
			response.header("Content-Type", "application/json");
		});
		
		declareFilters();
		declareEndpoints();

		isProperlyInitialized = true;
	}

	protected abstract void initStaicFilesRoute();
	
	protected abstract void declareWebSockets();
	protected abstract void declareFilters();
	protected abstract void declareEndpoints();
	
	/**
	 * Spark's 'before' functionality
	 * 
	 * @param path
	 * @param route
	 */
	protected void filter(String path, Route route) {
		getSparkService().before(path,(request, response) -> route.handle(request, response));
	}
	/**
	 * Spark's 'get' functionality with default json transformer,
	 * in order to serialize returned object as json
	 * 
	 * @param path
	 * @param route
	 */
	protected void get(String path, Route route) {
		getSparkService().get(path,(request, response) -> route.handle(request, response), jsonTransformer);
	}
	
	/**
	 * Spark's 'post' functionality with default json transformer,
	 * in order to serialize returned object as json
	 * 
	 * @param path
	 * @param route
	 */
	protected void post(String path, Route route) {
		getSparkService().post(path,(request, response) -> route.handle(request, response), jsonTransformer);
	}
	
	protected void websocket(String path, WebSocketHandler handler) {
		spark.webSocket(path, handler);
	}
	
	public  void configure(String ip, int port) {
		spark.ipAddress(ip);
		spark.port(port);
	}
	
	protected  Logger getLogger() {
		return logger;
	}
	
	protected Service getSparkService() {
		return spark;
	}
	public  void start() {
		if (!isProperlyInitialized)
			throw new RuntimeException(getClass() + " is not properly initialized , you have to call 'init' method");
		spark.init();
	}
	
	public void stop() {
		spark.stop();
	}

	protected SqlConnector getSqlConnector() {
		return sqlConnector;
	}
}
