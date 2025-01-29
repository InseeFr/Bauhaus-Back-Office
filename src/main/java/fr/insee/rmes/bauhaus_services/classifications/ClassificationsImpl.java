package fr.insee.rmes.bauhaus_services.classifications;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.classification.Classification;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.classifications.*;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ClassificationsImpl implements ClassificationsService {
	private static final String CAN_T_READ_REQUEST_BODY = "Can't read request body";

	private final StampsRestrictionsService stampsRestrictionsService;
	private final RepositoryGestion repoGestion;
	private final ClassificationUtils classificationUtils;
	private final ClassificationPublication classificationPublication;
	
	static final Logger logger = LoggerFactory.getLogger(ClassificationsImpl.class);

	public ClassificationsImpl(StampsRestrictionsService stampsRestrictionsService, RepositoryGestion repoGestion, ClassificationUtils classificationUtils, ClassificationPublication classificationPublication) {
		this.stampsRestrictionsService = stampsRestrictionsService;
		this.repoGestion = repoGestion;
		this.classificationUtils = classificationUtils;
		this.classificationPublication = classificationPublication;
	}

	@Override
	public String getFamilies() throws RmesException {
		logger.info("Starting to get classification families");
		return repoGestion.getResponseAsArray(ClassifFamiliesQueries.familiesQuery()).toString();
	}
	
	@Override
	public String getFamily(String id) throws RmesException {
		logger.info("Starting to get classification family");
		return repoGestion.getResponseAsObject(ClassifFamiliesQueries.familyQuery(id)).toString();
	}
	
	@Override
	public String getFamilyMembers(String id) throws RmesException {
		logger.info("Starting to get classification family members");
		return repoGestion.getResponseAsArray(ClassifFamiliesQueries.familyMembersQuery(id)).toString();
	}
	
	@Override
	public String getSeries() throws RmesException {
		logger.info("Starting to get classifications series");
		return repoGestion.getResponseAsArray(ClassifSeriesQueries.seriesQuery()).toString();
	}
	
	@Override
	public String getOneSeries(String id) throws RmesException {
		logger.info("Starting to get a classification series");
		return repoGestion.getResponseAsObject(ClassifSeriesQueries.oneSeriesQuery(id)).toString();
	}
	
	@Override
	public String getSeriesMembers(String id) throws RmesException {
		logger.info("Starting to get members of a classification series");
		return repoGestion.getResponseAsArray(ClassifSeriesQueries.seriesMembersQuery(id)).toString();
	}
	
	@Override
	public String getClassifications() throws RmesException {
		logger.info("Starting to get classifications");
		return repoGestion.getResponseAsArray(ClassificationsQueries.classificationsQuery()).toString();
	}
	
	@Override
	public String getClassification(String id) throws RmesException{
		logger.info("Starting to get a classification scheme");
		JSONObject classification = repoGestion.getResponseAsObject(ClassificationsQueries.classificationQuery(id));
		XhtmlToMarkdownUtils.convertJSONObject(classification);
		return classification.toString();
	}

	@Override
	public void updateClassification(String id, String body) throws RmesException {
		logger.info("Starting to update the classification {}", id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Classification classification = new Classification();
		classification.setId(id);
		try {
			classification = mapper.readerForUpdating(classification).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesNotFoundException(ErrorCodes.CLASSIFICATION_INCORRECT_BODY, e.getMessage(), CAN_T_READ_REQUEST_BODY);
		}
		String uri = repoGestion.getResponseAsObject(ClassificationsQueries.classificationQueryUri(classification.getId())).getString("iri");

		classificationUtils.updateClassification(classification, uri);
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

	@Override
	public String setClassificationValidation(String classificationId) throws RmesException {
		//GET graph
		JSONObject listGraph = repoGestion.getResponseAsObject(ClassificationsQueries.getGraphUriById(classificationId));
		logger.debug("JSON for listGraph id : {}", listGraph);
		if (listGraph.isEmpty()) {throw new RmesNotFoundException(ErrorCodes.CLASSIFICATION_UNKNOWN_ID, "Classification not found", classificationId);}
		String graph = listGraph.getString("graph");
		String classifUriString = listGraph.getString(Constants.URI);
		Resource graphIri = RdfUtils.createIRI(graph);
		
		
		if(!stampsRestrictionsService.canValidateClassification((SimpleIRI) graphIri)) {
			throw new RmesUnauthorizedException(ErrorCodes.CLASSIFICATION_VALIDATION_RIGHTS_DENIED, "Only authorized users can validate classifications.");
		}

		//PUBLISH
		classificationPublication.publishClassification(graphIri);

		//UPDATE GESTION TO MARK AS PUBLISHED
		Model model = new LinkedHashModel();
		IRI classifURI = RdfUtils.toURI(classifUriString);
		model.add(classifURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), graphIri);
		model.remove(classifURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graphIri);
		model.remove(classifURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), graphIri);
		logger.info("Validate classification : {}", classifUriString);
		repoGestion.objectValidation(classifURI, model);

		return classificationId;
	}
}
