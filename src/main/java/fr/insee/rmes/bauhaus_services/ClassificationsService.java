package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesNotFoundException;
import org.springframework.web.multipart.MultipartFile;

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

	public void updateClassification(String id, String body) throws RmesException;

	public String getClassificationLevels(String id) throws RmesException;
	
	public String getClassificationLevel(String classificationid, String levelId) throws RmesException;
	
	public String getClassificationLevelMembers(String classificationid, String levelId) throws RmesException;
	
	public String getCorrespondences() throws RmesException;
	
	public String getCorrespondence(String id) throws RmesException;
	
	public String getCorrespondenceAssociations(String id) throws RmesException;
	
	public String getCorrespondenceAssociation(String correspondenceId, String associationId) throws RmesException;

	public String setClassificationValidation(String id) throws RmesException;

	public void uploadClassification(MultipartFile file, String database) throws RmesException;

}
