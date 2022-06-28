package fr.insee.rmes.model.mail_sender;

import java.io.InputStream;
import java.util.Map;

import fr.insee.rmes.exceptions.RmesException;

public interface MailSenderContract {
	
	public boolean sendMailConcept(String id, String body,  Map<String,InputStream> getFileToJoin) throws  RmesException;
	
	public boolean sendMailCollection(String id, String body, Map<String,InputStream> getFileToJoin) throws  RmesException;

}
