package fr.insee.rmes.persistance.service.sesame.classifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.ClassificationsService;
import fr.insee.rmes.persistance.service.sesame.utils.SesameService;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.CorrespondencesQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.FamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.ItemsQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.LevelsQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.SeriesQueries;

@Service
public class ClassificationsImpl  extends SesameService  implements ClassificationsService {

	static final Logger logger = LogManager.getLogger(ClassificationsImpl.class);
	
	@Override
	public String getFamilies() throws RmesException {
		logger.info("Starting to get classification families");
		return repoGestion.getResponseAsArray(FamiliesQueries.familiesQuery()).toString();
	}
	
	@Override
	public String getFamily(String id) throws RmesException {
		logger.info("Starting to get classification family");
		return repoGestion.getResponseAsObject(FamiliesQueries.familyQuery(id)).toString();
	}
	
	@Override
	public String getFamilyMembers(String id) throws RmesException {
		logger.info("Starting to get classification family members");
		return repoGestion.getResponseAsArray(FamiliesQueries.familyMembersQuery(id)).toString();
	}
	
	@Override
	public String getSeries() throws RmesException {
		logger.info("Starting to get classifications series");
		return repoGestion.getResponseAsArray(SeriesQueries.seriesQuery()).toString();
	}
	
	@Override
	public String getOneSeries(String id) throws RmesException {
		logger.info("Starting to get a classification series");
		return repoGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id)).toString();
	}
	
	@Override
	public String getSeriesMembers(String id) throws RmesException {
		logger.info("Starting to get members of a classification series");
		return repoGestion.getResponseAsArray(SeriesQueries.seriesMembersQuery(id)).toString();
	}
	
	@Override
	public String getClassifications() throws RmesException {
		logger.info("Starting to get classifications");
		return repoGestion.getResponseAsArray(ClassificationsQueries.classificationsQuery()).toString();
	}
	
	@Override
	public String getClassification(String id) throws RmesException{
		logger.info("Starting to get a classification scheme");
		return repoGestion.getResponseAsObject(ClassificationsQueries.classificationQuery(id)).toString();
	}
	
	@Override
	public String getClassificationItems(String id) throws RmesException{
		logger.info("Starting to get a classification scheme");
		return repoGestion.getResponseAsArray(ClassificationsQueries.classificationItemsQuery(id)).toString();
	}
	
	@Override
	public String getClassificationLevels(String id) throws RmesException{
		logger.info("Starting to get levels of a classification scheme");
		return repoGestion.getResponseAsArray(LevelsQueries.levelsQuery(id)).toString();
	}
	
	@Override
	public String getClassificationLevel(String classificationId, String levelId) throws RmesException{
		logger.info("Starting to get a classification level");
		return repoGestion.getResponseAsObject(LevelsQueries.levelQuery(classificationId, levelId)).toString();
	}
	
	@Override
	public String getClassificationLevelMembers(String classificationId, String levelId)throws RmesException {
		logger.info("Starting to get classification level members");
		return repoGestion.getResponseAsArray(LevelsQueries.levelMembersQuery(classificationId, levelId)).toString();
	}
	
	@Override
	public String getClassificationItem(String classificationId, String itemId) throws RmesException{
		logger.info("Starting to get classification item");
		JSONObject item = repoGestion.getResponseAsObject(ItemsQueries.itemQuery(classificationId, itemId));
		JSONArray altLabels = repoGestion.getResponseAsArray(ItemsQueries.itemAltQuery(classificationId, itemId));
		if(altLabels.length() != 0) {
			item.put("altLabels", altLabels);
		}
		return item.toString();
	}
	
	@Override
	public String getClassificationItemNotes(String classificationId, String itemId, int conceptVersion)throws RmesException {
		logger.info("Starting to get classification item notes");
		return repoGestion.getResponseAsObject(ItemsQueries.itemNotesQuery(classificationId, itemId, conceptVersion)).toString();
	}
	
	@Override
	public String getClassificationItemNarrowers(String classificationId, String itemId) throws RmesException {
		logger.info("Starting to get classification item members");
		return repoGestion.getResponseAsArray(ItemsQueries.itemNarrowersQuery(classificationId, itemId)).toString();
	}
	
	@Override
	public String getCorrespondences() throws RmesException{
		logger.info("Starting to get correspondences");
		return repoGestion.getResponseAsArray(CorrespondencesQueries.correspondencesQuery()).toString();
	}

	@Override
	public String getCorrespondence(String id) throws RmesException{
		logger.info("Starting to get a correspondence scheme : {}" , id);
		return repoGestion.getResponseAsObject(CorrespondencesQueries.correspondenceQuery(id)).toString();
	}
	
	@Override
	public String getCorrespondenceAssociations(String id) throws RmesException{
		logger.info("Starting to get correspondence associations : {}" , id);
		return repoGestion.getResponseAsArray(CorrespondencesQueries.correspondenceAssociationsQuery(id)).toString();
	}
	
	@Override
	public String getCorrespondenceAssociation(String correspondenceId, String associationId) throws RmesException{
		logger.info("Starting to get correspondence association : {} - {}" , correspondenceId , associationId);
		return repoGestion.getResponseAsObject(CorrespondencesQueries.correspondenceAssociationQuery(correspondenceId, associationId)).toString();
	}
}
