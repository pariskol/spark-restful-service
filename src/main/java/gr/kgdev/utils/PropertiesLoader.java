package gr.kgdev.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoader {

	private static HashMap<String, Properties> propertiesMap = new HashMap<>();
	private static Logger logger = LoggerFactory.getLogger("spark");
	
	static {
		loadProperties("./rest.properties");
	}
	
	public static void setLogger(Logger logger) {
		PropertiesLoader.logger = logger;
	}
	
	public static void loadProperties(String rootPath) {
		try {
			Files.walk(Paths.get(rootPath))
	        .filter(Files::isRegularFile)
	        .filter(path -> path.toString().endsWith(".properties"))
	        .forEach(path -> {
	        	try (InputStream inputStream = new FileInputStream(path.toFile())) {
					Properties props = new Properties();
					props.load(inputStream);
					propertiesMap.put(path.getFileName().toString(), props);
		        } catch (IOException e) {
		        	if (logger != null)
		    			logger.error(e.getMessage());
		        	else
		        		e.printStackTrace();
				}
	        });
		} catch (IOException e) {
			if (logger != null)
    			logger.error(e.getMessage());
        	else
        		e.printStackTrace();
		}
	}
	
	public static Object getProperty(String fileKey, String key, Class<?> clazz) {
		Object value = propertiesMap.get(fileKey).get(key);
		try {
			Constructor<?> cons = clazz.getConstructor(String.class);
			Object returnedValue = cons.newInstance(value.toString());
			return returnedValue;
		} catch (Exception e) {
			if (logger != null)
    			logger.error(e.getMessage());
        	else
        		e.printStackTrace();
		}
		return null;
	}
	
	public static Object getProperty(String fileKey, String key, Class<?> clazz, Object defaultValue) {
		Object value = getProperty(fileKey, key, clazz);
		return value != null ? value : defaultValue;
	}
	
	/**
	 * Returns the first matching key from all loaded properties
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	public static Object getProperty(String key, Class<?> clazz) {
		Object value = null;
		for (Properties props : propertiesMap.values()) {
			value = props.get(key);
			if (value != null)
				break;
		}
		try {
			Constructor<?> cons = clazz.getConstructor(String.class);
			Object returnedValue = cons.newInstance(value.toString());
			return returnedValue;
		} catch (Exception e) {
			if (logger != null)
    			logger.error(e.getMessage());
        	else
        		e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the first matching key from all loaded properties
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	public static Object getProperty(String key, Class<?> clazz, Object defaultValue) {
		Object value = getProperty(key, clazz);
		return value != null ? value : defaultValue;
	}
	
	public static Properties getPropertiesFromFile(String fileNamePart) {
		for (String key : propertiesMap.keySet()) {
			if (key.contains(fileNamePart))
				return propertiesMap.get(key);
		}
		return null;
	}
	
	
}
