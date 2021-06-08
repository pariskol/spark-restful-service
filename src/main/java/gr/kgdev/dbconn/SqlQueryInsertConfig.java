package gr.kgdev.dbconn;

import java.util.ArrayList;
import java.util.List;

public class SqlQueryInsertConfig implements SqlQueryConfig {

	private String table;
	private String columns;
	private String values;
	private List<Object> params;

	public SqlQueryInsertConfig(String table) {
		this.table = table;
		this.columns = "";
		this.values = "";
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
		this.columns += key + ", ";
		this.values += "?, ";
		this.params.add(value);
	}

	private void fix() {
		if (columns.endsWith(", "))
			columns = columns.substring(0, columns.length() - ", ".length());
		if (values.endsWith(", "))
			values = values.substring(0, values.length() - ", ".length());
	}


	@Override
	public String getSql() {
		return "insert into " + table + " (" + getColumns() + ") values (" + getValues() + ")";
	}
	
	@Override
	public final List<Object> getParams() {
		return params;
	}

	public String getTable() {
		return table;
	}

}
