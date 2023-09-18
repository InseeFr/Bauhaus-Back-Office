package fr.insee.rmes.external_services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotificationsImpl implements NotificationsContract {
	
	static final Logger logger = LoggerFactory.getLogger(DefaultNotificationsImpl.class);
	
	public void notifyConceptCreation(String id, String uri) {
		logger.info("Notification : concept creation, id : {}, uri {}", id, uri);
	}
	
	public void notifyConceptUpdate(String id, String uri) {
		logger.info("Notification : concept update, id : {}, uri {}", id, uri);
	}
	
	public void notifyCollectionCreation(String id, String uri) {
		logger.info("Notification : collection creation, id : {}, uri {}", id, uri);
	}
	
	public void notifyCollectionUpdate(String id, String uri) {
		logger.info("Notification : collection update, id : {}, uri {}", id, uri);
	}

}
