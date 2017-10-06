package fr.insee.rmes.persistance.notifications;

public interface NotificationsContract {
	
	public void notifyConceptCreation(String id, String URI);
	
	public void notifyConceptUpdate(String id, String URI);
	
	public void notifyCollectionCreation(String id, String URI);
	
	public void notifyCollectionUpdate(String id, String URI);
	
}
