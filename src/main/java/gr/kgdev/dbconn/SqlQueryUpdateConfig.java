package gr.kgdev.dbconn;

import java.util.ArrayList;
import java.util.List;

public class SqlQueryUpdateConfig implements SqlQueryConfig {

	private String sql;
	private String table;
	private String columns;
	private String values;
	private String whereClause;
	private List<Object> params;
	
	public SqlQueryUpdateConfig(String table) {
		this.table = table;
		this.sql = "update " + table + " set ";
		this.columns = "";
		this.values = "";
		this.whereClause = "";
		this.params = new ArrayList<>();
	}
	
	public String getColumns() {
		this.fix();
		return columns;
	}

	public String getValues() {
		this.fix();
		return values;
	}

	public void addColumn(String key, Object value) {
		this.sql += key + " = ?, ";
		this.params.add(value);
	}
	
	public void addWhereColumn(String key, Object value) {
		if (this.whereClause.isEmpty())
			this.whereClause = " where 1=1";
		
		this.whereClause += " and " + key + " = ? ";
		this.params.add(value);
	}

	public String getWhereClause() {
		return whereClause;
	}
	
	private void fix() {
		if (columns.endsWith(", "))
			columns = columns.substring(0, columns.length() - ", ".length());
		if (values.endsWith(", "))
			values = values.substring(0, values.length() - ", ".length());
	}


	@Override
	public String getSql() {
		sql = sql.substring(0, sql.length() - ", ".length());
		sql += getWhereClause();
		return sql;
	}
	
	@Override
	public final List<Object> getParams() {
		return params;
	}

	public String getTable() {
		return table;
	}
}
