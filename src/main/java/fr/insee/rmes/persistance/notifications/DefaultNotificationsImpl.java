package fr.insee.rmes.persistance.notifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultNotificationsImpl implements NotificationsContract {
	
	final static Logger logger = LogManager.getLogger(DefaultNotificationsImpl.class);
	
	public void notifyConceptCreation(String id, String URI) {
		logger.info("Notification : concept creation, id : " + id);
	}
	
	public void notifyConceptUpdate(String id, String URI) {
		logger.info("Notification : concept update, id : " + id);
	}
	
	public void notifyCollectionCreation(String id, String URI) {
		logger.info("Notification : collection creation, id : " + id);
	}
	
	public void notifyCollectionUpdate(String id, String URI) {
		logger.info("Notification : collection update, id : " + id);
	}

}
