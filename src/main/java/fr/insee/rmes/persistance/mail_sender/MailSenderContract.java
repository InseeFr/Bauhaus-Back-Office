package fr.insee.rmes.persistance.mail_sender;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;

public interface MailSenderContract {
	
	public boolean sendMailConcept(String id, String body) throws RmesUnauthorizedException, RmesException;
	
	public boolean sendMailCollection(String id, String body) throws RmesUnauthorizedException, RmesException;

}
