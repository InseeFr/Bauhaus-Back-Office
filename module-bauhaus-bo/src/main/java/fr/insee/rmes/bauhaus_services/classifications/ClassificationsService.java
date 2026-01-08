package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.classifications.nomenclatures.model.PartialClassification;
import fr.insee.rmes.modules.classifications.families.model.PartialClassificationFamily;
import fr.insee.rmes.modules.classifications.series.model.PartialClassificationSeries;

import java.util.List;

public interface ClassificationsService {
	
	List<PartialClassificationFamily> getFamilies() throws RmesException;
	
	String getFamily(String id) throws RmesException;
	
	String getFamilyMembers(String id) throws RmesException;

	List<PartialClassificationSeries> getSeries() throws RmesException;
	
	String getOneSeries(String id) throws RmesException;
	
	String getSeriesMembers(String id) throws RmesException;
	
	List<PartialClassification> getClassifications() throws RmesException;
	
	String getClassification(String id) throws RmesException;

	void updateClassification(String id, String body) throws RmesException;

	String getClassificationLevels(String id) throws RmesException;
	
	String getClassificationLevel(String classificationid, String levelId) throws RmesException;
	
	String getClassificationLevelMembers(String classificationid, String levelId) throws RmesException;
	
	String getCorrespondences() throws RmesException;
	
	String getCorrespondence(String id) throws RmesException;
	
	String getCorrespondenceAssociations(String id) throws RmesException;
	
	String getCorrespondenceAssociation(String correspondenceId, String associationId) throws RmesException;

	void setClassificationValidation(String id) throws RmesException;
}
