package fr.insee.rmes.persistance.mailSender;

public interface MailSenderContract {
	
	public boolean sendMailConcept(String id, String body);
	
	public boolean sendMailCollection(String id, String body);

}
