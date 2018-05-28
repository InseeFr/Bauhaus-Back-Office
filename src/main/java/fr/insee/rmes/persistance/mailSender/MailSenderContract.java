package fr.insee.rmes.persistance.mailSender;

public interface MailSenderContract {
	
	public boolean sendMailConcept(String id, String body) throws Exception;
	
	public boolean sendMailCollection(String id, String body) throws Exception;

}
