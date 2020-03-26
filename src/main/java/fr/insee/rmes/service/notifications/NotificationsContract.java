package fr.insee.rmes.service.notifications;

import fr.insee.rmes.exceptions.RmesException;

public interface NotificationsContract {
	
	public void notifyConceptCreation(String id, String uri) throws RmesException;
	
	public void notifyConceptUpdate(String id, String uri) throws RmesException;
	
	public void notifyCollectionCreation(String id, String uri) throws RmesException;
	
	public void notifyCollectionUpdate(String id, String uri) throws RmesException;
	
}
