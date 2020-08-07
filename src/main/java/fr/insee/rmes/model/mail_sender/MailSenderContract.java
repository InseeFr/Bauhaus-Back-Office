package fr.insee.rmes.model.mail_sender;

import fr.insee.rmes.exceptions.RmesException;

public interface MailSenderContract {
	
	public boolean sendMailConcept(String id, String body) throws  RmesException;
	
	public boolean sendMailCollection(String id, String body) throws  RmesException;

}
