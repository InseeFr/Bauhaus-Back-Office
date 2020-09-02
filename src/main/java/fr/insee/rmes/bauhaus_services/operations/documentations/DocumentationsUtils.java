package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
import fr.insee.rmes.bauhaus_services.operations.famopeser_utils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
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
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.ExtensiveSims;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.utils.XMLUtils;


@Component
public class DocumentationsUtils extends RdfService{

	private static final String ID_INDICATOR = "idIndicator";

	private static final String ID_OPERATION = "idOperation";

	private static final String ID_SERIES = "idSeries";

	private static final String PUBLISHER = "publisher";

	static final Logger logger = LogManager.getLogger(DocumentationsUtils.class);

	@Autowired
	private SeriesUtils seriesUtils;

	@Autowired
	private OperationsUtils operationsUtils;

	@Autowired
	private DocumentationExport docExport;

	@Autowired
	private DocumentationsRubricsUtils documentationsRubricsUtils;

	@Autowired
	private IndicatorsUtils indicatorsUtils;

	@Autowired
	private DocumentationPublication documentationPublication;

	@Autowired
	CodeListUtils codeListUtils;

	@Autowired
	DocumentationsUtils documentationsUtils;

	@Autowired
	private FamOpeSerUtils famOpeSerUtils;




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

	public Documentation getFullSims(String id) throws RmesException {
		return buildDocumentationFromJson(getDocumentationByIdSims(id));
	}


	/**
	 * Java Object	Builder
	 * @param JsonSims
	 * @return ExtensiveSims
	 * @throws RmesException
	 */

	public ExtensiveSims buildExtensiveDocumentationFromJson(JSONObject JsonSims) throws RmesException {
		Documentation sims = buildDocumentationFromJson(JsonSims);
		ExtensiveSims extensiveSims = new ExtensiveSims(sims);

		return extensiveSims;
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
			currentRubric = documentationsRubricsUtils.buildRubricFromJson(rubric);
			rubrics.add(currentRubric);
		}	
		sims.setRubrics(rubrics);

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

		String status = getDocumentationValidationStatus(id);

		// Create or update rdf
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
		JSONObject simsJson = getDocumentationByIdSims(id);
		Resource graph = RdfUtils.simsGraph(id);

		// Find target
		String targetId = null;
		IRI targetUri = null;
		try {
			targetId = simsJson.getString(ID_INDICATOR);
			if (!targetId.isEmpty()) {
				targetUri = RdfUtils.objectIRI(ObjectType.INDICATOR, targetId);
			} else {
				targetId = simsJson.getString(ID_OPERATION);
				if (!targetId.isEmpty()) {
					targetUri = RdfUtils.objectIRI(ObjectType.OPERATION, targetId);
				} else {
					targetId = simsJson.getString(ID_SERIES);
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

		if (StringUtils.isNotEmpty(sims.getIdOperation())
				&& famOpeSerUtils.checkIfObjectExists(ObjectType.OPERATION, sims.getIdTarget())) {
			target = RdfUtils.objectIRI(ObjectType.OPERATION, sims.getIdTarget());
		}
		if (StringUtils.isNotEmpty(sims.getIdSeries()) && famOpeSerUtils.checkIfObjectExists(ObjectType.SERIES, sims.getIdTarget())) {
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
			idDatabase = (String) existingIdTarget.get(ID_OPERATION);
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get(ID_SERIES);
			}
			if (idDatabase == null || StringUtils.isEmpty(idDatabase)) {
				idDatabase = (String) existingIdTarget.get(ID_INDICATOR);
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
		String id = json.getString("idSims");
		if (id.equals(Constants.UNDEFINED)) {
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
		return new String[] { targetType, idDatabase };	
	}

	public String getDocumentationOwnerByIdSims(String idSims) throws RmesException {
		logger.info("Search Sims Owner's Stamp");
		String stamp = null;
		JSONObject target = repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(idSims));		
		if (target != null) {
			String idOperation = target.getString(ID_OPERATION);
			String idSerie = target.getString(ID_SERIES);
			String idIndicator = target.getString(ID_INDICATOR);

			if (idOperation != null && !idOperation.isEmpty()) {
				stamp = seriesUtils.getSeriesJsonById(
						operationsUtils.getOperationJsonById(idOperation).getJSONObject("series").getString(ID_SERIES))
						.getString(PUBLISHER);
			} else if (idSerie != null && !idSerie.isEmpty()) {
				stamp = seriesUtils.getSeriesJsonById(idSerie).getString(PUBLISHER);
			} else if (idIndicator != null && !idIndicator.isEmpty()) {
				stamp = indicatorsUtils.getIndicatorJsonById(idIndicator).getString(PUBLISHER);
			} else {
				throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Documentation has no target",
						"Check your documentation creation");
			}
		}
		return stamp;
	}

	public File exportMetadataReport(String id) throws Exception {

		InputStream is;
		InputStream is2;
		Path tempDir= Files.createTempDirectory("forExport");

		Path tempFile = Files.createTempFile(tempDir, "target",".xml");
		String absolutePath = tempFile.toFile().getAbsolutePath();

		Path accessoryTempFile = Files.createTempFile(tempDir, "series",".xml");
		String accessoryAbsolutePath = accessoryTempFile.toFile().getAbsolutePath();
		
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };

		String[] target = getDocumentationTargetTypeAndId(id);
		String targetType = target[0];
		String idDatabase = target[1];

		if (targetType.equals("OPERATION")) {
			Operation operation=operationsUtils.getOperationById(idDatabase);
			String idSeries=operation.getSeries().getId();
			Series series=seriesUtils.getSeriesById(idSeries);
			is = IOUtils.toInputStream(XMLUtils.produceXMLResponse(operation), "UTF-8");
			Files.copy(is, tempFile, options);
			is2 = IOUtils.toInputStream(XMLUtils.produceXMLResponse(series), "UTF-8");
			Files.copy(is2, accessoryTempFile, options);
		}

		if (targetType.equals("SERIES")) {
			String response=XMLUtils.produceXMLResponse(
					seriesUtils.getSeriesById(idDatabase));
			is = IOUtils.toInputStream(response, "UTF-8");
			Files.copy(is, accessoryTempFile, options);
			is2 = IOUtils.toInputStream(response, "UTF-8");
			Files.copy(is2, tempFile, options);
		}

		if (targetType.equals("INDICATOR")) {
			is = IOUtils.toInputStream(XMLUtils.produceXMLResponse(
					indicatorsUtils.getIndicatorById(idDatabase)), "UTF-8");
			Files.copy(is, tempFile, options);
		}

		InputStream simsInputStream = IOUtils.toInputStream(XMLUtils.produceResponse(getFullSims(id), "application/xml"), "UTF-8");

		 return docExport.export(simsInputStream,absolutePath,accessoryAbsolutePath,targetType);
		 //	return docExport.transfoTest(simsInputStream);
	}

	public MSD buildMSDFromJson(JSONArray jsonMsd) {
		List<MAS> msd = new ArrayList<MAS>();
		MAS currentRubric = new MAS();

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

	public String buildShellSims() throws RmesException {
		MSD msd= operationsUtils.getMSD();
		return XMLUtils.produceXMLResponse(msd);
	}

}
