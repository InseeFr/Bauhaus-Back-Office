package fr.insee.rmes.persistance.service;

public interface ClassificationsService {
	
	public String getFamilies() throws Exception;
	
	public String getFamily(String id) throws Exception;
	
	public String getFamilyMembers(String id) throws Exception;

	public String getSeries() throws Exception;
	
	public String getOneSeries(String id) throws Exception;
	
	public String getSeriesMembers(String id) throws Exception;
	
	public String getClassifications() throws Exception;
	
	public String getClassification(String id) throws Exception;
	
	public String getClassificationItems(String id) throws Exception;
	
	public String getClassificationLevels(String id) throws Exception;
	
	public String getClassificationLevel(String classificationid, String levelId) throws Exception;
	
	public String getClassificationLevelMembers(String classificationid, String levelId) throws Exception;
	
	public String getClassificationItem(String classificationid, String itemId) throws Exception;
	
	public String getClassificationItemNotes(String classificationid, String itemId, int conceptVersion) throws Exception;

	public String getClassificationItemNarrowers(String classificationid, String itemId) throws Exception;
	
	public String getCorrespondences() throws Exception;
	
	public String getCorrespondence(String id) throws Exception;
}
