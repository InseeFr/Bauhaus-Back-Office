package fr.insee.rmes.persistance.mailSender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class DefaultMailSenderImpl implements MailSenderContract {
	
	final static Logger logger = LogManager.getLogger(DefaultMailSenderImpl.class);

	@Override
	public boolean sendMailConcept(String id, String body) {
		logger.info("Concept mail sent");
		return true;
	}
	
	@Override
	public boolean sendMailCollection(String id, String body) {
		logger.info("Collection mail sent");
		return true;
	}

}
