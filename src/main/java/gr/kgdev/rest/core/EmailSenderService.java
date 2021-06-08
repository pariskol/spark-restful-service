package gr.kgdev.rest.core;

import java.util.Map;

import javax.mail.MessagingException;

public interface EmailSenderService {

	public void sendEmail(Map<String, Object> map) throws MessagingException;
	
}
