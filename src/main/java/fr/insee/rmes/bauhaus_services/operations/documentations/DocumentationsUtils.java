package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.code_list.CodeListUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.organizations.OrganizationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.RangeType;
import fr.insee.rmes.persistance.ontologies.DCMITYPE;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.utils.DateParser;
import fr.insee.rmes.utils.XMLUtils;

@Component
public class DocumentationsUtils extends RdfService {

	private static final String VALUE = "value";

	private static final String HAS_DOC = "hasDoc";

	static final Logger logger = LogManager.getLogger(DocumentationsUtils.class);

	@Autowired
	MetadataStructureDefUtils msdUtils;

	@Autowired
	DocumentsUtils docUtils;

	@Autowired
	SeriesUtils seriesUtils;

	@Autowired
	OperationsUtils operationsUtils;

	@Autowired
	DocumentationExport docExport;

	@Autowired
	FamOpeSerUtils famOpeSerUtils;

	@Autowired
	OrganizationUtils organizationUtils;

	@Autowired
	IndicatorsUtils indicatorsUtils;

	@Autowired
	CodeListUtils codeListUtils;

	@Autowired
	DocumentationsUtils operationsService;

	@Autowired
	DocumentationPublication documentationPublication;

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

		JSONArray docRubrics = repoGestion
				.getResponseAsArray(DocumentationsQueries.getDocumentationRubricsQuery(idSims));
		if (docRubrics.length() != 0) {
			docRubrics = clearRubrics(idSims, docRubrics);
			doc.put("rubrics", docRubrics);
		}
		return doc;
	}

	public Documentation getFullSims(String id) throws RmesException {
		return buildDocumentationFromJson(getDocumentationByIdSims(id));
	}

	/**
	 * Java Object	Builder
	 * @param JsonSims
	 * @return Sims
	 * @throws RmesException
	 */

	public Documentation buildDocumentationFromJson(JSONObject JsonSims) throws RmesException {

		Documentation sims = new Documentation();
		String idSims=JsonSims.getString(Constants.ID);
		sims.setId(idSims);
		sims.setLabelLg1(JsonSims.getString("labelLg1"));
		sims.setLabelLg2(JsonSims.getString("labelLg2"));

		String[] target = getDocumentationTargetTypeAndId(idSims);
		String targetType = target[0];
		String idDatabase = target[1];

		switch(targetType) {
		case "OPERATION" : sims.setIdOperation(idDatabase); break;
		case "SERIES" : sims.setIdSeries(idDatabase); break;
		case "INDICATOR" : sims.setIdIndicator(idDatabase); break;
		}

		List<DocumentationRubric> rubrics = new ArrayList<DocumentationRubric>();
		JSONArray docRubrics = JsonSims.getJSONArray("rubrics");
		DocumentationRubric currentRubric = new DocumentationRubric();

		for (int i = 0; i < docRubrics.length(); i++) {
			JSONObject rubric = docRubrics.getJSONObject(i);
			currentRubric = buildRubricFromJson(rubric);
			rubrics.add(currentRubric);
		}	
		sims.setRubrics(rubrics);

		return sims;
	}



	/**
	 * Java Object	Builder
	 * @param JsonRubric
	 * @return documentationRubric
	 * @throws RmesException
	 */

	private DocumentationRubric buildRubricFromJson(JSONObject rubric) throws RmesException {
		DocumentationRubric documentationRubric = new DocumentationRubric();
		if (rubric.has("idAttribute"))		documentationRubric.setIdAttribute(rubric.getString("idAttribute"));
		if (rubric.has("value"))		documentationRubric.setValue(rubric.getString("value"));
		if (rubric.has("labelLg1"))		documentationRubric.setLabelLg1(rubric.getString("labelLg1"));
		if (rubric.has("labelLg2"))		documentationRubric.setLabelLg2(rubric.getString("labelLg2"));
		if (rubric.has("codeList"))		documentationRubric.setCodeList(rubric.getString("codeList"));
		if (rubric.has("rangeType"))		documentationRubric.setRangeType(rubric.getString("rangeType"));


		if (rubric.has("documents")) {	
			List<Document> docs = new ArrayList<Document>();

			JSONArray documents = rubric.getJSONArray("documents");
			Document currentDoc = new Document();

			for (int i = 0; i < documents.length(); i++) {
				JSONObject doc = documents.getJSONObject(i);
				currentDoc = buildDocumentFromJson(doc);
				docs.add(currentDoc);
			}	
			documentationRubric.setDocuments(docs);
		}
		return documentationRubric;
	}

	private Document buildDocumentFromJson(JSONObject jsonDoc) {

		Document doc= new Document();
		if (jsonDoc.has("url")) {	
			doc.setUrl(jsonDoc.getString("url"));
		}
		if (jsonDoc.has("labelLg1")) {	
			doc.setLabelLg1(jsonDoc.getString("labelLg1"));
		}
		if (jsonDoc.has("labelLg2")) {	
			doc.setLabelLg1(jsonDoc.getString("labelLg2"));
		}
		return(doc);
	}

	/**
	 * Get documents if exist, format date and format list of values for codelist
	 * @param idSims
	 * @param docRubrics
	 * @return
	 * @throws RmesException
	 */
	private JSONArray clearRubrics(String idSims,  JSONArray docRubrics) throws RmesException {
		Map<String, JSONObject> tempMultipleCodeList = new HashMap<>();

		for (int i = docRubrics.length()-1; i >= 0 ; i--) {
			JSONObject rubric = docRubrics.getJSONObject(i);

			//Get documents
			if (rubric.has(HAS_DOC) ) {
				if (rubric.getBoolean(HAS_DOC)) {
					JSONArray listDoc = docUtils.getListDocumentLink(idSims, rubric.getString("idAttribute"));
					rubric.put("documents", listDoc);
				}
				rubric.remove(HAS_DOC);
			}

			//Format date
			else if (rubric.get("rangeType").equals(RangeType.DATE)) {
				rubric.put(VALUE, DateParser.getDate(rubric.getString(VALUE)));
			}

			//Format codelist with multiple value
			else if (rubric.has("maxOccurs")) {
				String newValue = rubric.getString(VALUE);
				String attribute = rubric.getString("idAttribute");

				if (tempMultipleCodeList.containsKey(attribute)) {
					JSONObject tempObject = tempMultipleCodeList.get(attribute);
					tempObject.accumulate(VALUE, newValue);
					tempMultipleCodeList.replace(attribute, tempObject);
				}else {
					List<String> listValue = new ArrayList<>();
					listValue.add(newValue);
					tempMultipleCodeList.put(attribute, rubric);
				}
				docRubrics.remove(i);		
			}
		}
		if (tempMultipleCodeList.size() != 0) {
			tempMultipleCodeList.forEach((k,v) -> docRubrics.put(v));
		}
		return docRubrics;
	}

	/**
	 * CREATE or UPDATE
	 * @param id, body
	 * @return
	 * @throws RmesException 
	 */
	public String setMetadataReport(String id, String body, boolean create) throws RmesException {
		//TODO maxOccurs
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Documentation sims = new Documentation();
		try {
			sims = mapper.readValue(body, Documentation.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_METHOD_FAILURE, e.getMessage(), "IOException");
		}
		// Check idOperation/idSerie/IdIndicator and Init or check id sims
		String idTarget = sims.getIdTarget();
		if (create) {
			id = prepareCreation(idTarget);
			sims.setId(id);
			checkIfTargetIsASeriesWithOperations(idTarget);
		} else {
			checkIdsBeforeUpdate(id, sims.getId(), idTarget);
		}
		IRI targetUri = getTarget(sims);

		// Update rubrics
		if (create) {
			if (!stampsRestrictionsService.canCreateSims(targetUri)) {
				throw new RmesUnauthorizedException(ErrorCodes.SIMS_CREATION_RIGHTS_DENIED,
						"Only an admin or a manager can create a new sims.");
			}
			saveRdfMetadataReport(sims, targetUri, ValidationStatus.UNPUBLISHED);
		} else {
			if (!stampsRestrictionsService.canModifySims(targetUri)) {
				throw new RmesUnauthorizedException(ErrorCodes.SIMS_MODIFICATION_RIGHTS_DENIED,
						"Only an admin, CNIS, or a manager can modify this sims.", id);
			}
			String status = getValidationStatus(id);
			if (status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
				saveRdfMetadataReport(sims, targetUri, ValidationStatus.UNPUBLISHED);
			} else {
				saveRdfMetadataReport(sims, targetUri, ValidationStatus.MODIFIED);
			}
		}
		logger.info("Create or update sims : {} - {}", sims.getId(), sims.getLabelLg1());
		return sims.getId();
	}

	private String getValidationStatus(String id) throws RmesException {
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
		JSONObject simsJson = getDocumentationByIdSims(id);
		Resource graph = RdfUtils.simsGraph(id);

		// Find target
		String targetId = null;
		IRI targetUri = null;
		try {
			targetId = simsJson.getString("idIndicator");
			if (!targetId.isEmpty()) {
				targetUri = RdfUtils.objectIRI(ObjectType.INDICATOR, targetId);
			} else {
				targetId = simsJson.getString("idOperation");
				if (!targetId.isEmpty()) {
					targetUri = RdfUtils.objectIRI(ObjectType.OPERATION, targetId);
				} else {
					targetId = simsJson.getString("idSeries");
					targetUri = RdfUtils.objectIRI(ObjectType.SERIES, targetId);
				}
			}
		} catch (JSONException e) {
			throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_TARGET, "target not found for this Sims", id);
		}
		if (targetId.isEmpty()) {
			throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_TARGET, "target not found for this Sims", id);
		}

		/* Check rights */
		if (!stampsRestrictionsService.canCreateSims(targetUri)) {
			throw new RmesUnauthorizedException(ErrorCodes.SIMS_CREATION_RIGHTS_DENIED,
					"Only an admin or a manager can create a new sims.");
		}

		/* Check if the target is already published - otherwise an unauthorizedException is thrown. */
		String status = famOpeSerUtils.getValidationStatus(targetId);
		if (status.equals(Constants.UNDEFINED)) {
			status = indicatorsUtils.getValidationStatus(targetId);
		}
		if (PublicationUtils.isPublished(status)) {
			throw new RmesUnauthorizedException(ErrorCodes.SIMS_VALIDATION_UNPUBLISHED_TARGET,
					"This metadataReport cannot be published before its target is published. ",
					"MetadataReport: " + id + " ; Indicator/Series/Operation: " + targetId);
		}

		documentationPublication.publishSims(id);

		IRI simsURI = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, id);
		model.add(simsURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), graph);
		model.remove(simsURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED),
				graph);
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

		if (StringUtils.isNotEmpty(sims.getIdOperation())
				&& famOpeSerUtils.checkIfObjectExists(ObjectType.OPERATION, sims.getIdTarget())) {
			target = RdfUtils.objectIRI(ObjectType.OPERATION, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdSeries())
				&& famOpeSerUtils.checkIfObjectExists(ObjectType.SERIES, sims.getIdTarget())) {
			target = RdfUtils.objectIRI(ObjectType.SERIES, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdIndicator())
				&& indicatorsUtils.checkIfIndicatorExists(sims.getIdTarget())) {
			target = RdfUtils.objectIRI(ObjectType.INDICATOR, sims.getIdTarget());
		}
		if (target == null) {
			logger.error("Create or Update sims cancelled - no target");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Operation/Series/Indicator doesn't exist",
					"id Operation/Series/Indicator doesn't match with an existing Operation/Series/Indicator");
		}
		return target;
	}

	private void checkIfTargetIsASeriesWithOperations(String idTarget) throws RmesException {
		if (famOpeSerUtils.checkIfObjectExists(ObjectType.SERIES, idTarget) && seriesUtils.hasOperations(idTarget)) {
			throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,
					"Cannot create Sims for a series which already has operations", idTarget);
		}

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
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idSims can't be null, and must be the same in request",
					"idSims in param : " + idRequest + " /id in body : " + idSims);
		}
		// Check id Operation/Serie/Indicator
		if (idTarget == null) {
			logger.error("Can't update a documentation if id Operation/Serie/Indicator doesn't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "id Operation/Serie/Indicator can't be null",
					"id Operation/Serie/Indicator or id is null");
		}
		JSONObject existingIdTarget = repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));
		String idDatabase = null;
		if (existingIdTarget != null) {
			idDatabase = (String) existingIdTarget.get("idOperation");
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get("idSeries");
			}
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get("idIndicator");
			}
		}
		if (existingIdTarget == null || idDatabase == null) {
			logger.error("Can't find Operation/Serie/Indicator linked to the documentation");
			throw new RmesNotFoundException(ErrorCodes.SIMS_UNKNOWN_TARGET, "Operation/Serie/Indicator not found",
					"Maybe this is a creation");
		}
		if (!idTarget.equals(idDatabase)) {
			logger.error("id Operation/Serie/Indicator and idSims don't match");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "id Operation/Serie/Indicator and idSims don't match",
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
	private String prepareCreation(String idTarget) throws RmesException {
		if (idTarget == null) {
			logger.error("Can't create a documentation if operation/serie/indicator doesn't exist");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "id operation/serie/indicator can't be null",
					"id is null");
		}
		JSONObject existingIdSims = repoGestion.getResponseAsObject(DocumentationsQueries.getSimsByTarget(idTarget));
		if (existingIdSims != null && existingIdSims.has("idSims")) {
			logger.error("Documentation already exists");
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Operation/Series/Indicator already has a documentation",
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

		addRubricsToModel(model, sims.getId(), graph, sims.getRubrics());

		repoGestion.replaceGraph(graph, model, null);
	}

	/**
	 * Add all rubrics to the specified metadata report
	 * @param model
	 * @param simsId
	 * @param graph
	 * @param rubrics
	 * @throws RmesException
	 */
	private void addRubricsToModel(Model model, String simsId, Resource graph, List<DocumentationRubric> rubrics)
			throws RmesException {
		Map<String, String> attributesUriList = msdUtils.getMetadataAttributesUri();
		IRI simsUri = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, simsId);

		for (DocumentationRubric rubric : rubrics) {
			RangeType type = getRangeType(rubric);
			IRI predicateUri;
			IRI attributeUri;
			try {
				String predicate = attributesUriList.get(rubric.getIdAttribute());
				predicateUri = RdfUtils.toURI(predicate);
				attributeUri = getAttributeUri(simsId, predicate);
			} catch (Exception e) {
				throw new RmesException(HttpStatus.SC_BAD_REQUEST, "idAttribute not found", rubric.getIdAttribute());
			}
			RdfUtils.addTripleUri(attributeUri, SDMX_MM.METADATA_REPORT_PREDICATE, simsUri, model, graph);
			addRubricByRangeType(model, graph, rubric, type, predicateUri, attributeUri);
		}
	}

	/**
	 * Add one rubric to the model
	 * @param model
	 * @param graph
	 * @param rubric
	 * @param type
	 * @param predicateUri
	 * @param attributeUri
	 * @throws RmesException
	 */
	private void addRubricByRangeType(Model model, Resource graph, DocumentationRubric rubric, RangeType type,
			IRI predicateUri, IRI attributeUri) throws RmesException {
		switch (type) {
		case DATE:
			RdfUtils.addTripleDateTime(attributeUri, predicateUri, rubric.getValue(), model, graph);
			break;
		case CODELIST:
			String codeUri = codeListUtils.getCodeUri(rubric.getCodeList(), rubric.getValue());
			if (codeUri != null) {
				RdfUtils.addTripleUri(attributeUri, predicateUri, RdfUtils.toURI(codeUri), model, graph);//TODO
			}
			break;
		case RICHTEXT:
			if (rubric.isEmpty()) {
				break;
			}
			addRichTextToModel(model, graph, rubric, predicateUri, attributeUri);
			break;
		case ORGANIZATION:
			String orgaUri = organizationUtils.getUri(rubric.getValue());
			if (orgaUri != null) {
				RdfUtils.addTripleUri(attributeUri, predicateUri, RdfUtils.toURI(orgaUri), model, graph);
			}
			break;
		case STRING:
			if (rubric.isEmpty()) {
				break;
			}
			addSimpleTextToModel(model, graph, rubric, predicateUri, attributeUri);
			break;
		default:
			break;
		}
	}

	private void addSimpleTextToModel(Model model, Resource graph, DocumentationRubric rubric, IRI predicateUri,
			IRI attributeUri) {
		RdfUtils.addTripleUri(attributeUri, RDF.TYPE, SDMX_MM.REPORTED_ATTRIBUTE, model, graph);
		if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
			RdfUtils.addTripleString(attributeUri, predicateUri, rubric.getLabelLg1(), Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
			RdfUtils.addTripleString(attributeUri, predicateUri, rubric.getLabelLg2(), Config.LG2, model, graph);
		}
	}

	private void addRichTextToModel(Model model, Resource graph, DocumentationRubric rubric, IRI predicateUri,
			IRI attributeUri) throws RmesException {
		IRI textUri = RdfUtils.toURI(attributeUri.stringValue().concat("/texte"));
		RdfUtils.addTripleUri(attributeUri, predicateUri, textUri, model, graph);
		RdfUtils.addTripleUri(textUri, RDF.TYPE, DCMITYPE.TEXT, model, graph);
		if (StringUtils.isNotEmpty(rubric.getLabelLg1())) {
			RdfUtils.addTripleStringMdToXhtml(textUri, RDF.VALUE, rubric.getLabelLg1(), Config.LG1, model, graph);
		}
		if (StringUtils.isNotEmpty(rubric.getLabelLg2())) {
			RdfUtils.addTripleStringMdToXhtml(textUri, RDF.VALUE, rubric.getLabelLg2(), Config.LG2, model, graph);
		}
		docUtils.addDocumentsToRubric(model, graph, rubric, textUri);
	}

	private RangeType getRangeType(DocumentationRubric rubric) throws RmesException {
		if (rubric.getRangeType() == null) {
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, "At least one rubric doesn't have rangeType",
					"Rubric :" + rubric.getIdAttribute());
		}
		return RangeType.getEnumByJsonType(rubric.getRangeType());
	}

	/**
	 * Get attribute uri for a metadata report and the associated attribute definition
	 * @param simsId
	 * @param predicate
	 * @return
	 */
	private IRI getAttributeUri(String simsId, String predicate) {
		String newUri = predicate.replace("/simsv2fr/attribut/", "/attribut/" + simsId + "/");
		return RdfUtils.toURI(newUri);
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
		String id = json.getString("idSims");
		if (id.equals("undefined")) {
			return "1000";
		}
		int newId = Integer.parseInt(id) + 1;
		return String.valueOf(newId);
	}



	public String[] getDocumentationTargetTypeAndId(String idSims) throws RmesException {
		logger.info("Search Sims Target Type and id");

		JSONObject existingIdTarget =  repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));
		String idDatabase = null;
		String targetType = null;
		if (existingIdTarget != null ) {
			idDatabase = (String) existingIdTarget.get("idOperation");

			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get("idSeries");

				if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
					idDatabase = (String) existingIdTarget.get("idIndicator");
					targetType = "INDICATOR";
				} else {
					targetType = "SERIES";
				}
			} else {
				targetType = "OPERATION";
			}
		}
		return new String[] { targetType, idDatabase };	}

	public String getDocumentationOwnerByIdSims(String idSims) throws RmesException {
		logger.info("Search Sims Owner's Stamp");

		String[] target = getDocumentationTargetTypeAndId(idSims);
		String targetType = target[0];
		String idDatabase = target[1];
		String stamp=null;

		switch(targetType) {
		case "OPERATION" : stamp=seriesUtils.getSeriesById(operationsUtils.getOperationById(idDatabase).getJSONObject("series").getString("idSeries")).getString("creator");
		case "SERIES" : stamp=seriesUtils.getSeriesById(idDatabase).getString("creator");
		case "INDICATOR" : stamp=indicatorsUtils.getIndicatorById(idDatabase).getString("creator");
		}
		return stamp;
	}

	public File exportMetadataReport(String id) throws Exception {
		InputStream test;
		if(id=="toto") {
			test = getClass().getClassLoader().getResourceAsStream("Sims1908XML.xml");
		}
		else {
			test = getClass().getClassLoader().getResourceAsStream(XMLUtils.produceResponse(operationsService.getFullSims(id), "application/xml"));
		}
		return docExport.export(test);
	}

}
