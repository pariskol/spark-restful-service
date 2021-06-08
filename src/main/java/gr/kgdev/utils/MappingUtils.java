package gr.kgdev.utils;

import java.util.Map;

import gr.kgdev.model.User;

public class MappingUtils {

	public static User convertMapToUser(Map<String, Object> map) {
		User user = new User();
		user.setName(map.get("username").toString());
		user.setPassword(map.get("password").toString());
		user.setEmail(map.get("email").toString());
		return user;
	}
}
