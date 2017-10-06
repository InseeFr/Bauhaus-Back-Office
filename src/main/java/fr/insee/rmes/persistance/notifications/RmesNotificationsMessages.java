package fr.insee.rmes.persistance.notifications;

public class RmesNotificationsMessages {
	
	public static String conceptCreation(String id, String URI) {
		return "\n"
				+ "<Concept> \n"
				+ "\t<Event>Create</Event> \n"
				+ "\t<Id>" + id + "</Id>\n"
				+ "\t<URI>" + URI + "</URI>\n"
				+ "</Concept>";
	}
	
	public static String conceptUpdate(String id, String URI) {
		return "\n"
				+ "<Concept> \n"
				+ "\t<Event>Update</Event> \n"
				+ "\t<Id>" + id + "</Id>\n"
				+ "\t<URI>" + URI + "</URI>\n"
				+ "</Concept>";
	}
	
	public static String collectionCreation(String id, String URI) {
		return "\n"
				+ "<Collection> \n"
				+ "\t<Event>Create</Event> \n"
				+ "\t<Id>" + id + "</Id>\n"
				+ "\t<URI>" + URI + "</URI>\n"
				+ "</Collection>";
	}
	
	public static String collectionUpdate(String id, String URI) {
		return "\n"
				+ "<Collection> \n"
				+ "\t<Event>Update</Event> \n"
				+ "\t<Id>" + id + "</Id>\n"
				+ "\t<URI>" + URI + "</URI>\n"
				+ "</Collection>";
	}

}
