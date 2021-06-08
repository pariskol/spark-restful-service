package gr.kgdev.rest.core;

import org.json.JSONObject;

public class JSONMessages {

	public static final String NOT_FOUND = "Ops not found!";
	public static final String INTERNAL_ERROR = "Ops something went wrong!";
	public static final String UNAUTHORIZED = "Unauthorized";
	public static final String FORBIDDEN = "Forbidden";
	
	public static final String JM_DELETED = create("Data has been deleted");
	public static final String JM_UPDATED = create("Data has been updated");
	public static final String JM_PROCESSED = create("Your request wll be processed");
	public static final String JM_NO_ACTIVE_USER_FOUND = create("No active user found");
	public static final String JM_USER_LOGGED_OUT = create("User logged out successfully");
	public static final String JM_NOT_AVAILABLE_USERNAME = create("Username not available!");
	
	public static String create(String message) {
		return "{ \"message\": \"" + message + "\"}";
	}
	
	public static String create(String message, String uuid) {
			JSONObject json = new JSONObject();
			json.put("message", message);
			json.put("tag", uuid);
			return json.toString();
	}
}
