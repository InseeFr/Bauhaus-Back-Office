package fr.insee.rmes.persistance.notifications;

import fr.insee.rmes.exceptions.RmesException;

public interface NotificationsContract {
	
	public void notifyConceptCreation(String id, String URI) throws RmesException;
	
	public void notifyConceptUpdate(String id, String URI) throws RmesException;
	
	public void notifyCollectionCreation(String id, String URI) throws RmesException;
	
	public void notifyCollectionUpdate(String id, String URI) throws RmesException;
	
}
