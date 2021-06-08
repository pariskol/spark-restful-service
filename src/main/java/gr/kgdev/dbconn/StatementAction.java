package gr.kgdev.dbconn;

import java.sql.Statement;

@FunctionalInterface
public interface StatementAction {

	public void onStatement(Statement stmt);
}
