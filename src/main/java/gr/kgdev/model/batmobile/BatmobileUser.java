package gr.kgdev.model.batmobile;

import gr.kgdev.model.AuthUser;
import gr.kgdev.utils.dtomapper.Column;
import gr.kgdev.utils.dtomapper.DTO;

@DTO
public class BatmobileUser implements AuthUser {

	@Column("IS_GROUP")
	private Integer isGroup;
	@Column("ID")
	private Integer id;
	@Column("NAME")
	private String name;

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
	
	public Integer getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Integer isGroup) {
		this.isGroup = isGroup;
	}
	
	public Boolean isGroup() {
		return isGroup == 1;
	}
}
