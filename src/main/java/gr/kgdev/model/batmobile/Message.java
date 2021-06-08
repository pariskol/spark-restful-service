package gr.kgdev.model.batmobile;

import gr.kgdev.utils.dtomapper.Column;
import gr.kgdev.utils.dtomapper.DTO;

@DTO
public class Message {

	public static Integer STATUS_READ = 1;
	public static Integer STATUS_UNREAD = 0;

	@Column("ID")
	private Integer id;
	@Column("MESSAGE")
	private String message;
	@Column("FROM_USER")
	private Integer fromUserId;
	@Column("TO_USER")
	private Integer toUserId;
	@Column("TIMESTAMP")
	private String timestamp;
	@Column("SHOWN")
	private Integer status;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String mesasage) {
		this.message = mesasage;
	}

	public Integer getFromUserId() {
		return fromUserId;
	}

	public void setFromUserId(Integer fromUserId) {
		this.fromUserId = fromUserId;
	}

	public Integer getToUserId() {
		return toUserId;
	}

	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timesatmp) {
		this.timestamp = timesatmp;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

}
