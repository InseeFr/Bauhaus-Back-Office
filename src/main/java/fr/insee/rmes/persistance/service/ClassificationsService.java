package fr.insee.rmes.persistance.service;

import fr.insee.rmes.exceptions.RmesException;

public interface ClassificationsService {
	
	public String getFamilies() throws RmesException;
	
	public String getFamily(String id) throws RmesException;
	
	public String getFamilyMembers(String id) throws RmesException;

	public String getSeries() throws RmesException;
	
	public String getOneSeries(String id) throws RmesException;
	
	public String getSeriesMembers(String id) throws RmesException;
	
	public String getClassifications() throws RmesException;
	
	public String getClassification(String id) throws RmesException;
	
	public String getClassificationItems(String id) throws RmesException;
	
	public String getClassificationLevels(String id) throws RmesException;
	
	public String getClassificationLevel(String classificationid, String levelId) throws RmesException;
	
	public String getClassificationLevelMembers(String classificationid, String levelId) throws RmesException;
	
	public String getClassificationItem(String classificationid, String itemId) throws RmesException;
	
	public String getClassificationItemNotes(String classificationid, String itemId, int conceptVersion) throws RmesException;

	public String getClassificationItemNarrowers(String classificationid, String itemId) throws RmesException;
	
	public String getCorrespondences() throws RmesException;
	
	public String getCorrespondence(String id) throws RmesException;
	
	public String getCorrespondenceAssociations(String id) throws RmesException;
	
	public String getCorrespondenceAssociation(String correspondenceId, String associationId) throws RmesException;
}
