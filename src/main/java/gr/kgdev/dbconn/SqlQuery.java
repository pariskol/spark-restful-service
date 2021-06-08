package gr.kgdev.dbconn;

import java.util.List;

public class SqlQuery {

	private String query;
	List<Object> params;

	
	public SqlQuery(SqlQueryConfig sqlConf) {
		this.query = sqlConf.getSql();
		this.params = sqlConf.getParams();
	}
	
	public SqlQuery(String query, List<Object> params) {
		this.query = query;
		this.params = params;
	}

	public String getSQL() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		String str=  "";
		str += query;
		str += " { ";
		for (Object param : params) {
			str += param.toString() + ", ";
		}
		str = str.substring(0, str.length() - ", ".length());
		str += " }";
		
		return str;
	}
}
