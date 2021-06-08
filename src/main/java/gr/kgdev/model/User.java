package gr.kgdev.model;

import gr.kgdev.utils.dtomapper.Column;
import gr.kgdev.utils.dtomapper.DTO;

@DTO
public class User implements AuthUser {

	@Column("ID")
	private Integer id;
	@Column("NAME")
	private String name;
	private String password;
	private String email;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
