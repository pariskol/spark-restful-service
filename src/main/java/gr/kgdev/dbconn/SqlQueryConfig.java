package gr.kgdev.dbconn;

import java.util.List;

public interface SqlQueryConfig {

	public String getSql();
	public List<Object> getParams();
}
