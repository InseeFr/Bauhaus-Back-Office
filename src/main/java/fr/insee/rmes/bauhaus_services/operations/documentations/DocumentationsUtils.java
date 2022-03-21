package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.code_list.LangService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.utils.DateUtils;


@Component
public class DocumentationsUtils extends RdfService{

	static final Logger logger = LogManager.getLogger(DocumentationsUtils.class);


	@Autowired
	private DocumentationsRubricsUtils documentationsRubricsUtils;
	
	@Autowired
	private DocumentationPublication documentationPublication;

	@Autowired
	LangService langService;

	@Autowired
	OrganizationsService organizationsServiceImpl;

	@Autowired
	CodeListService codeListServiceImpl;
	
	@Autowired
	ParentUtils parentUtils;

	/**
	 * GETTER
	 * @param idSims
	 * @return
	 * @throws RmesException
	 */
	public JSONObject getDocumentationByIdSims(String idSims) throws RmesException {

		// Get general informations
		JSONObject doc = repoGestion.getResponseAsObject(DocumentationsQueries.getDocumentationTitleQuery(idSims));
		if (doc.length() == 0) {
			throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_ID, "Documentation not found", "");
		}
		doc.put(Constants.ID, idSims);

		// Get all rubrics
		documentationsRubricsUtils.getAllRubricsJson(idSims, doc);
		return doc;
	}

	public Documentation getFullSimsForXml(String id) throws RmesException {
		return buildDocumentationFromJson(getDocumentationByIdSims(id),true);
	}

	public JSONObject getFullSimsForJson(String id) throws RmesException {
		return getDocumentationByIdSims(id);
	}
	
	/**
	 * Java Object	Builder
	 * @param jsonSims
	 * @return Sims
	 * @throws RmesException
	 */

	public Documentation buildDocumentationFromJson(JSONObject jsonSims, Boolean forXml) throws RmesException {

		Documentation sims = new Documentation();
		String idSims=jsonSims.getString(Constants.ID);
		sims.setId(idSims);
		sims.setLabelLg1(jsonSims.getString(Constants.LABEL_LG1));
		sims.setLabelLg2(jsonSims.getString(Constants.LABEL_LG2));

		String[] target = parentUtils.getDocumentationTargetTypeAndId(idSims);
		String targetType = target[0];
		String idDatabase = target[1];

		switch(targetType) {
		case Constants.OPERATION_UP : sims.setIdOperation(idDatabase); break;
		case Constants.SERIES_UP : sims.setIdSeries(idDatabase); break;
		case Constants.INDICATOR_UP : sims.setIdIndicator(idDatabase); break;
		default : break;
		}

		List<DocumentationRubric> rubrics = new ArrayList<>();

		if(jsonSims.has("rubrics")) {
			JSONArray docRubrics = jsonSims.getJSONArray("rubrics");
			DocumentationRubric currentRubric ;

			for (int i = 0; i < docRubrics.length(); i++) {
				JSONObject rubric = docRubrics.getJSONObject(i);
				currentRubric = documentationsRubricsUtils.buildRubricFromJson(rubric,forXml);
				rubrics.add(currentRubric);
			}	
			sims.setRubrics(rubrics);
		}
		return sims;
	}

	/**
	 * CREATE or UPDATE
	 * @param id, body
	 * @return
	 * @throws RmesException 
	 */
	public String setMetadataReport(String id, String body, boolean create) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		Documentation sims = new Documentation();
		try {
			sims = mapper.readValue(body, Documentation.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesNotAcceptableException(ErrorCodes.SIMS_INCORRECT, e.getMessage(), "IOException: cannot parse input");
		}

		// Check idOperation/idSerie/IdIndicator and Init or check id sims
		String idTarget = sims.getIdTarget();
		if (create) {
			id = checkTargetHasNoSimsAndcreateSimsId(idTarget);
			sims.setId(id);
			parentUtils.checkIfParentIsASeriesWithOperations(idTarget);
		} else {
			checkIdsBeforeUpdate(id, sims.getId(), idTarget);
		}
		IRI targetUri = getTarget(sims);

		String status = getDocumentationValidationStatus(id);

		// Create or update rdf
		IRI seriesOrIndicatorUri = targetUri;
		if (RdfUtils.toString(targetUri).contains(Config.OPERATIONS_BASE_URI)) {
			seriesOrIndicatorUri = parentUtils.getSeriesUriByOperationId(idTarget);
		}
		if (create) {
			if (!stampsRestrictionsService.canCreateSims(seriesOrIndicatorUri)) {
				throw new RmesUnauthorizedException(ErrorCodes.SIMS_CREATION_RIGHTS_DENIED,
						"Only an admin or a manager can create a new sims.");
			}
			sims.setCreated(DateUtils.getCurrentDate());
			sims.setUpdated(DateUtils.getCurrentDate());
			saveRdfMetadataReport(sims, targetUri, ValidationStatus.UNPUBLISHED);
		} else {
			if (!stampsRestrictionsService.canModifySims(seriesOrIndicatorUri)) {
				throw new RmesUnauthorizedException(ErrorCodes.SIMS_MODIFICATION_RIGHTS_DENIED,
						"Only an admin, CNIS, or a manager can modify this sims.", id);
			}
			sims.setUpdated(DateUtils.getCurrentDate());
			if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
				saveRdfMetadataReport(sims, targetUri, ValidationStatus.UNPUBLISHED);
			} else {
				saveRdfMetadataReport(sims, targetUri, ValidationStatus.MODIFIED);
			}
		}

		logger.info("Create or update sims : {} - {}", sims.getId(), sims.getLabelLg1());
		return sims.getId();
	}


	private String getDocumentationValidationStatus(String id) throws RmesException {
		try {
			return repoGestion.getResponseAsObject(DocumentationsQueries.getPublicationState(id)).getString("state");
		} catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}

	/**
	 * PUBLISH
	 * @param id
	 * @return
	 * @throws RmesException 
	 */
	public String publishMetadataReport(String id) throws RmesException {
		Model model = new LinkedHashModel();
		Resource graph = RdfUtils.simsGraph(id);

		// Find target
		String[] target = parentUtils.getDocumentationTargetTypeAndId(id);
		String targetType = target[0];
		String targetId = target[1];
		IRI targetUri = null;

		if (targetId.isEmpty()) {
			throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_TARGET, "target not found for this Sims", id);
		}

		switch(targetType) {
		case Constants.OPERATION_UP : targetUri = RdfUtils.objectIRI(ObjectType.OPERATION, targetId); break;
		case Constants.SERIES_UP : targetUri = RdfUtils.objectIRI(ObjectType.SERIES, targetId); break;
		case Constants.INDICATOR_UP : targetUri = RdfUtils.objectIRI(ObjectType.INDICATOR, targetId); break;
		default : break;
		}

		/* Check rights */
		IRI seriesOrIndicatorUri = targetUri;
		if (targetType.equals(Constants.OPERATION_UP)) {
			seriesOrIndicatorUri = parentUtils.getSeriesUriByOperationId(targetId);
		}
		if (!stampsRestrictionsService.canCreateSims(seriesOrIndicatorUri)) {
			throw new RmesUnauthorizedException(ErrorCodes.SIMS_CREATION_RIGHTS_DENIED,
					"Only an admin or a manager can create a new sims.");
		}

		/* Check if the target is already published - otherwise an unauthorizedException is thrown. */
		String status = parentUtils.getValidationStatus(targetId);
		if (PublicationUtils.isPublished(status)) {
			throw new RmesUnauthorizedException(ErrorCodes.SIMS_VALIDATION_UNPUBLISHED_TARGET,
					"This metadataReport cannot be published before its target is published. ",
					"MetadataReport: " + id + " ; Indicator/Series/Operation: " + targetId);
		}

		documentationPublication.publishSims(id);

		IRI simsURI = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, id);
		model.add(simsURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), graph);
		model.remove(simsURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graph);
		model.remove(simsURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), graph);
		logger.info("Validate sims : {}", simsURI);

		repoGestion.objectValidation(simsURI, model);

		return id;
	}

	/**
	 * get Operation URI to create or update SIMS
	 * @param sims
	 * @return URItarget
	 * @throws RmesException
	 */
	private IRI getTarget(Documentation sims) throws RmesException {
		IRI target = null;

		if (StringUtils.isNotEmpty(sims.getIdOperation())) {
			target = RdfUtils.objectIRI(ObjectType.OPERATION, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdSeries())) {
			target = RdfUtils.objectIRI(ObjectType.SERIES, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdIndicator())) {				 
			target = RdfUtils.objectIRI(ObjectType.INDICATOR, sims.getIdTarget());
		}
		if (!parentUtils.checkIfParentExists(RdfUtils.toString(target))) target = null; 
		if (target == null) {
			logger.error("Create or Update sims cancelled - no target");
			throw new RmesException(HttpStatus.BAD_REQUEST, "Operation/Series/Indicator doesn't exist",
					"id Operation/Series/Indicator doesn't match with an existing Operation/Series/Indicator");
		}
		return target;
	}



	/**
	 * Check the existing id is the same that the id to set
	 * Update only
	 * @param idRequest
	 * @param idSims
	 * @param idTarget
	 * @throws RmesException
	 */
	private void checkIdsBeforeUpdate(String idRequest, String idSims, String idTarget) throws RmesException {
		// Check idSims
		if (idRequest == null || idSims == null || !idRequest.equals(idSims)) {
			logger.error("Can't update a documentation if idSims or id don't exist");
			throw new RmesException(HttpStatus.BAD_REQUEST, "idSims can't be null, and must be the same in request",
					"idSims in param : " + idRequest + " /id in body : " + idSims);
		}
		// Check id Operation/Serie/Indicator
		if (idTarget == null) {
			logger.error("Can't update a documentation if id Operation/Serie/Indicator doesn't exist");
			throw new RmesException(HttpStatus.BAD_REQUEST, "id Operation/Serie/Indicator can't be null",
					"id Operation/Serie/Indicator or id is null");
		}
		JSONObject existingIdTarget = repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));
		String idDatabase = null;
		if (existingIdTarget != null) {
			idDatabase = (String) existingIdTarget.get(Constants.ID_OPERATION);
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get(Constants.ID_SERIES);
			}
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get(Constants.ID_INDICATOR);
			}
		}
		if (existingIdTarget == null || idDatabase == null) {
			logger.error("Can't find Operation/Serie/Indicator linked to the documentation");
			throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_TARGET, "Operation/Serie/Indicator not found",
					"Maybe this is a creation");
		}
		if (!idTarget.equals(idDatabase)) {
			logger.error("id Operation/Serie/Indicator and idSims don't match");
			throw new RmesException(HttpStatus.BAD_REQUEST, "id Operation/Serie/Indicator and idSims don't match",
					"Documentation linked to Operation/Serie/Indicator : " + existingIdTarget);
		}
	}

	/**
	 * check idTarget is not null and has no sims yet
	 * create only
	 * @param idTarget
	 * @return
	 * @throws RmesException
	 */
	private String checkTargetHasNoSimsAndcreateSimsId(String idTarget) throws RmesException {
		if (idTarget == null) {
			logger.error("Can't create a documentation if operation/serie/indicator doesn't exist");
			throw new RmesException(HttpStatus.BAD_REQUEST, "id operation/serie/indicator can't be null",
					"id is null");
		}
		JSONObject existingIdSims = repoGestion.getResponseAsObject(DocumentationsQueries.getSimsByTarget(idTarget));
		if (existingIdSims != null && existingIdSims.has(Constants.ID_SIMS)) {
			logger.error("Documentation already exists");
			throw new RmesException(HttpStatus.BAD_REQUEST, "Operation/Series/Indicator already has a documentation",
					"Maybe this is an update");
		}
		return createSimsID();
	}

	/**
	 * Load in database the metadataReport to create or update
	 * @param sims
	 * @param target
	 * @throws RmesException
	 */
	private void saveRdfMetadataReport(Documentation sims, IRI target, ValidationStatus state) throws RmesException {
		Model model = new LinkedHashModel();
		IRI simsUri = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, sims.getId());
		Resource graph = RdfUtils.simsGraph(sims.getId());
		/*Const*/
		model.add(simsUri, RDF.TYPE, SDMX_MM.METADATA_REPORT, graph);
		model.add(simsUri, SDMX_MM.TARGET, target, graph);
		model.add(simsUri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(state), graph);

		/*Optional*/
		RdfUtils.addTripleString(simsUri, RDFS.LABEL, sims.getLabelLg1(), Config.LG1, model, graph);
		RdfUtils.addTripleString(simsUri, RDFS.LABEL, sims.getLabelLg2(), Config.LG2, model, graph);

		RdfUtils.addTripleDateTime(simsUri, DCTERMS.CREATED, sims.getCreated(), model, graph);
		RdfUtils.addTripleDateTime(simsUri, DCTERMS.MODIFIED, sims.getUpdated(), model, graph);

		documentationsRubricsUtils.addRubricsToModel(model, sims.getId(), graph, sims.getRubrics());

		repoGestion.replaceGraph(graph, model, null);
	}

	/**
	 * Generate a new ID
	 * Prefer to call prepareCreation instead
	 * @return
	 * @throws RmesException
	 */
	private String createSimsID() throws RmesException {
		logger.info("Generate documentation id");
		JSONObject json = repoGestion.getResponseAsObject(DocumentationsQueries.lastID());
		logger.debug("JSON for documentation id : {}", json);
		if (json.length() == 0) {
			return "1000";
		}
		String id = json.getString(Constants.ID_SIMS);
		if (id.equals(Constants.UNDEFINED)) {
			return "1000";
		}
		int newId = Integer.parseInt(id) + 1;
		return String.valueOf(newId);
	}

	


	public MSD buildMSDFromJson(JSONArray jsonMsd) {
		List<MAS> msd = new ArrayList<>();
		MAS currentRubric;

		for (int i = 0; i < jsonMsd.length(); i++) {
			JSONObject rubric = jsonMsd.getJSONObject(i);
			currentRubric = buildMSDRubricFromJson(rubric);
			msd.add(currentRubric);
		}	
		return new MSD(msd) ;
	}

	public MAS buildMSDRubricFromJson(JSONObject jsonMsdRubric) {
		MAS msd = new MAS();
		if (jsonMsdRubric.has("idMas")) {
			msd.setIdMas(jsonMsdRubric.getString("idMas"));
		}
		if (jsonMsdRubric.has("masLabelLg1")) {
			msd.setMasLabelLg1(jsonMsdRubric.getString("masLabelLg1"));
		}
		if (jsonMsdRubric.has("masLabelLg2")) {
			msd.setMasLabelLg2(jsonMsdRubric.getString("masLabelLg2"));
		}
		if (jsonMsdRubric.has("idParent")) {
			msd.setIdParent(jsonMsdRubric.getString("idParent"));
		}
		if (jsonMsdRubric.has("isPresentational")) {
			msd.setIsPresentational(jsonMsdRubric.getBoolean("isPresentational"));
		}

		return msd ;
	}
	

	public MSD getMSD() throws RmesException {
		return buildMSDFromJson(repoGestion.getResponseAsArray(DocumentationsQueries.msdQuery()));
	}


	public HttpStatus deleteMetadataReport(String id) throws RmesException {
		String[] target = parentUtils.getDocumentationTargetTypeAndId(id);
		String targetType = target[0];

		if (!Constants.SERIES_UP.equals(targetType)) {
			throw new RmesNotAcceptableException(ErrorCodes.SIMS_DELETION_FOR_NON_SERIES, "Only a sims that documents a series can be deleted", id);
		}

		if (!stampsRestrictionsService.canDeleteSims()) {
			throw new RmesUnauthorizedException(ErrorCodes.SIMS_DELETION_RIGHTS_DENIED,
					"Only an admin or a manager can delete a sims.");
		}		
		Resource graph = RdfUtils.simsGraph(id);

		HttpStatus result =  repoGestion.executeUpdate(DocumentationsQueries.deleteGraph(graph));
		if (result.equals(HttpStatus.OK)) {
			result = RepositoryPublication.executeUpdate(DocumentationsQueries.deleteGraph(graph));	
		}

		return result;

	}


	public void updateDocumentationTitle(String idSims, String prefLabeLg1, String prefLabelLg2) throws RmesException {
		Model model = new LinkedHashModel();
		IRI simsUri = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, idSims);
		Resource graph = RdfUtils.simsGraph(idSims);

		/*Optional*/
		RdfUtils.addTripleString(simsUri, RDFS.LABEL, Config.DOCUMENTATIONS_TITLE_PREFIX_LG1 + " " + prefLabeLg1, Config.LG1, model, graph);
		RdfUtils.addTripleString(simsUri, RDFS.LABEL, Config.DOCUMENTATIONS_TITLE_PREFIX_LG2 + " " + prefLabelLg2, Config.LG2, model, graph);

		repoGestion.overrideTriplets(simsUri, model, graph);
	}
	
}
