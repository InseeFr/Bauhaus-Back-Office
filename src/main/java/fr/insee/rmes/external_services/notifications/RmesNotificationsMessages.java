package fr.insee.rmes.external_services.notifications;

public class RmesNotificationsMessages {
	
	public static String conceptCreation(String id, String uri) {
		return createNotificationMessage("Concept","Create",id,uri);

	}
	
	public static String conceptUpdate(String id, String uri) {
		return createNotificationMessage("Concept","Update",id,uri);

	}
	
	public static String collectionCreation(String id, String uri) {
		return createNotificationMessage("Collection","Create",id,uri);

	}
	
	public static String collectionUpdate(String id, String uri) {
		return createNotificationMessage("Collection","Update",id,uri);
	}
	
	private static String createNotificationMessage(String tag, String event, String id, String uri) {
		return "\n"
				+ "<"+tag+"> \n"
				+ "\t<Event>"+event+"</Event> \n"
				+ "\t<Id>" + id + "</Id>\n"
				+ "\t<URI>" + uri + "</URI>\n"
				+ "</"+tag+">";
	}
	
	 private RmesNotificationsMessages() {
		    throw new IllegalStateException("Utility class");
	 }

}
