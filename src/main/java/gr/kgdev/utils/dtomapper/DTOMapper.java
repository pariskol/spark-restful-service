package gr.kgdev.utils.dtomapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;



public class DTOMapper {

	/**
	 * Maps a row of result set into a HashMap. 
	 * For mapping columns aliases are used.
	 * 
	 * @param rset
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	public static LinkedHashMap<String, Object> map(ResultSet rset) throws Exception {

		LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
		ResultSetMetaData rsmd = rset.getMetaData();
		
		for (int i=1;i<= rsmd.getColumnCount();i++) {
			Object value = dto.get(rsmd.getColumnLabel(i));
			if (value != null)
				dto.put(rsmd.getTableName(i) + "." + rsmd.getColumnLabel(i), rset.getObject(i));
			else dto.put(rsmd.getColumnLabel(i), rset.getObject(i));
		}
		
		return dto;
	}


	/**
	 * Maps a row of result set into a HashMap. 
	 * For mapping columns real names are used.
	 * 
	 * @param rset
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	public static LinkedHashMap<String, Object> mapR(ResultSet rset) throws Exception {

		LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
		ResultSetMetaData rsmd = rset.getMetaData();
		
		for (int i=1;i<= rsmd.getColumnCount();i++) {
			Object value = dto.get(rsmd.getColumnName(i));
			if (value != null)
				dto.put(rsmd.getTableName(i) + "." + rsmd.getColumnName(i), rset.getObject(i));
			else dto.put(rsmd.getColumnName(i), rset.getObject(i));
		}
		
		return dto;
	}
	
	public static LinkedHashMap<String, Object> mapWithCamelCase(ResultSet rset) throws Exception {

		LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
		ResultSetMetaData rsmd = rset.getMetaData();
		
		for (int i=1;i<= rsmd.getColumnCount();i++) {
			String key = toCamelCase(rsmd.getColumnLabel(i));
			Object value = dto.get(key);
			if (value != null)
				dto.put(key + " (" + rsmd.getTableName(i) + ")", rset.getObject(i));
			else
				dto.put(key, rset.getObject(i));
		}
		
		return dto;
	}
	
	/**
	 * Maps a row of result set into a LinkedHashMap.
	 * Throws unchecked exception.
	 * Use this if a more generic exception handling system exists.
	 * 
	 * @param rset
	 * @return HashMap
	 * @throws RuntimeException
	 */
	public static LinkedHashMap<String, Object> mapU(ResultSet rset) throws RuntimeException {

		LinkedHashMap<String, Object> dto = null;
		try {
			dto = map(rset);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return dto;
	}
	
	public static LinkedHashMap<String, Object> mapUwithCamelCase(ResultSet rset) throws RuntimeException {

		LinkedHashMap<String, Object> dto = null;
		try {
			dto = mapWithCamelCase(rset);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return dto;
	}
	
	/**
	 * Maps a row of result set into the given class (which has @DTO annotation and its fields have @Column annotation).
	 * Database's column types and class fields must be type compatible.
	 * Throws unchecked exception.
	 * Use this if a more generic exception handling system exists.
	 * 
	 * @param rset
	 * @param clazz
	 * @return
	 * @throws RuntimeException 
	 */
	public static Object mapU(ResultSet rset, Class<?> clazz) throws RuntimeException {
		Object dto = null;
		try {
			dto = map(rset, clazz);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return dto;
	}
	/**
	 * Maps a row of result set into the given class (which has @DTO annotation and its fields have @Column annotation).
	 * Database's column types and class fields must be type compatible.
	 * 
	 * @param rset
	 * @param clazz
	 * @return
	 * @throws Exception 
	 */
	public static Object map(ResultSet rset, Class<?> clazz) throws Exception {

		if (clazz.getAnnotation(DTO.class) == null)
			throw new IllegalAccessException(
					"Class " + clazz.getSimpleName() + " has no annotation " + DTO.class.getName());

		Object dto = clazz.getDeclaredConstructor().newInstance();

		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			Annotation annotation = field.getAnnotation(Column.class);
			if (annotation != null) {
				// get object from result set and cast it to field's class
				Object value = null;
				try {
					value = rset.getObject(((Column) annotation).value(), field.getType());
				} catch(Exception e) {
					// getObject(name, class) may be unsupported in some jdbc drivers (ex sqlite)
					value = rset.getObject(((Column) annotation).value());
				}
				field.set(dto, value);
			}
		}

		return dto;
	}
	
	/**
	 * Maps a row of result set in a given class.It tries to predict column names by
	 * mapping class' fields names to snake case upper keys (commonly used in sql).
	 * ex. field 'totalAmount -> TOTAL_AMOUNT'. This method works if pojo and db
	 * table has same keys as fileds and columns , deffering only in format (camel
	 * case for java , snake case for db tables) Database's column types and class
	 * fields must be type compatible.
	 *
	 * @param rset
	 * @param clazz
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object tryToMap(ResultSet rset, Class<?> clazz)
			throws SQLException, IllegalAccessException, InstantiationException {

		Object dto = clazz.newInstance();

		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			String columnName = camelToSnakeUpperCase(field.getName());
			// get object from result set and cast it to field's class
			Object value = null;
			try {
				value = rset.getObject(columnName, field.getType());
			} catch(Exception e) {
				// getObject(name, class) may be unsupported in some jdbc drivers (ex sqlite)
				value = rset.getObject(columnName);
			}
			field.set(dto, value);
		}

		return dto;
	}
	
	/**
	 * Maps a row of result set in a given class.It tries to predict column names by
	 * mapping class' fields names to snake case upper keys (commonly used in sql).
	 * ex. field 'totalAmount -> TOTAL_AMOUNT'. This method works if pojo and db
	 * table has same keys as fileds and columns , deffering only in format (camel
	 * case for java , snake case for db tables) Database's column types and class
	 * fields must be type compatible.
	 * Throws unchecked exception.
	 * Use this if a more generic exception handling system exists.
	 *
	 * @param rset
	 * @param clazz
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static Object tryToMapU(ResultSet rset, Class<?> clazz)
			throws RuntimeException {

		Object dto = null;
		try {
			dto = tryToMap(rset, clazz);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return dto;
	}

	private static String camelToSnakeUpperCase(String str) {

		String result = "";

		// Append first character(in lower case)
		char c = str.charAt(0);
		result += Character.toLowerCase(c);

		for (int i = 1; i < str.length(); i++) {
			char ch = str.charAt(i);
			// Check if the character is upper case
			// then append '_' and such character
			// (in lower case) to result string
			if (Character.isUpperCase(ch)) {
				result += '_';
				result += Character.toLowerCase(ch);
			} else {
				result += ch;
			}
		}

		return result.toUpperCase();
	}

	private static String toCamelCase(String str) {
		String[] parts = str.toLowerCase().split("_");
		String camelCaseString = "";
		for (String part : parts) {
			camelCaseString += 
					part.substring(0, 1).toUpperCase() +
					part.substring(1, part.length());
		}
		if (!camelCaseString.isEmpty())
			camelCaseString = camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1, camelCaseString.length());
		else
			camelCaseString = str;
		
		return camelCaseString;
	}

}