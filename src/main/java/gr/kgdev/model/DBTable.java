package gr.kgdev.model;

import gr.kgdev.utils.dtomapper.Column;
import gr.kgdev.utils.dtomapper.DTO;

@DTO
public class DBTable {

	@Column("NAME")
	private String name;
	@Column("AUTH_POLICY")
	private String authPolicy;
	@Column("METHOD")
	private String httpMethod;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthPolicy() {
		return authPolicy;
	}

	public void setAuthPolicy(String authPolicy) {
		this.authPolicy = authPolicy;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

}
