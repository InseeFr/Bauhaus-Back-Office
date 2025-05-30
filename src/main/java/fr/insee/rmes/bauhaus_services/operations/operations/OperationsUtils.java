package fr.insee.rmes.bauhaus_services.operations.operations;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.*;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OperationsUtils extends RdfService{

	static final Logger logger = LoggerFactory.getLogger(OperationsUtils.class);

	@Autowired
	private FamOpeSerIndUtils famOpeSerIndUtils;

	@Autowired
	private DocumentationsUtils documentationsUtils;
	
	@Autowired
	ParentUtils parentUtils;


	@Autowired
	private OperationPublication operationPublication;

	private void validate(Operation operation) throws RmesException {
		if(repoGestion.getResponseAsBoolean(OperationsQueries.checkPrefLabelUnicity(operation.getId(), operation.getPrefLabelLg1(), config.getLg1()))){
			throw new RmesBadRequestException(ErrorCodes.OPERATION_OPERATION_EXISTING_PREF_LABEL_LG1, "This prefLabelLg1 is already used by another operation.");
		}
		if(repoGestion.getResponseAsBoolean(OperationsQueries.checkPrefLabelUnicity(operation.getId(), operation.getPrefLabelLg2(), config.getLg2()))){
			throw new RmesBadRequestException(ErrorCodes.OPERATION_OPERATION_EXISTING_PREF_LABEL_LG2, "This prefLabelLg2 is already used by another operation.");
		}
	}

	public Operation getOperationById(String id) throws RmesException {
		return buildOperationFromJson(getOperationJsonById(id));
	}

	public JSONObject getOperationJsonById(String id) throws RmesException {
		JSONObject operation = repoGestion.getResponseAsObject(OperationsQueries.operationQuery(id));
		getOperationSeries(id, operation);
		return operation;
	}

	private void getOperationSeries(String id, JSONObject operation) throws RmesException {
		JSONObject series = repoGestion.getResponseAsObject(OperationsQueries.seriesQuery(id));
		JSONArray creators = repoGestion.getResponseAsJSONList(OpSeriesQueries.getCreatorsById(series.getString(Constants.ID)));
		series.put(Constants.CREATORS, creators);
		operation.put("series", series);
	}
	

	private Operation buildOperationFromJson(JSONObject operationJson) throws RmesException {
		Operation operation = Deserializer.deserializeJsonString(operationJson.toString(), Operation.class);
		IdLabelTwoLangs series = famOpeSerIndUtils.buildIdLabelTwoLangsFromJson(operationJson.getJSONObject("series"));
		operation.setSeries(series);
		return operation;
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 * @throws RmesException
	 */
	public String setOperation(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String id = famOpeSerIndUtils.createId();
		Operation operation = new Operation();
		try {
			operation = mapper.readValue(body, Operation.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		operation.setId(id);
		// Tester l'existence de la série
		String idSeries= operation.getSeries().getId();
		if (! famOpeSerIndUtils.checkIfObjectExists(ObjectType.SERIES,idSeries)) {
			throw new RmesNotFoundException(ErrorCodes.OPERATION_UNKNOWN_SERIES,"Unknown series: ",idSeries) ;
		}
		// Tester que la série n'a pas de Sims
		IRI seriesURI = RdfUtils.objectIRI(ObjectType.SERIES,idSeries);
		if (parentUtils.checkIfSeriesHasSims(seriesURI.stringValue())) {
			throw new RmesNotAcceptableException(ErrorCodes.SERIES_OPERATION_OR_SIMS,"A series cannot have both a Sims and Operation(s)", 
					idSeries +" ; "+operation.getPrefLabelLg1());
		}

		operation.setCreated(DateUtils.getCurrentDate());
		operation.setModified(DateUtils.getCurrentDate());

		createRdfOperation(operation, seriesURI, ValidationStatus.UNPUBLISHED);
		logger.info("Create operation : {} - {}" , operation.getId() , operation.getPrefLabelLg1());

		return operation.getId();
	}


	public void setOperation(String id, String body) throws RmesException {
		Operation operation = new Operation(id);
		try {
			operation = Deserializer.deserializeJsonString(body, Operation.class);
		} catch (RmesException e) {
			logger.error(e.getMessage());
		}

		operation.setModified(DateUtils.getCurrentDate());

		String status= parentUtils.getValidationStatus(id);
		documentationsUtils.updateDocumentationTitle(operation.getIdSims(), operation.getPrefLabelLg1(), operation.getPrefLabelLg2());
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) || status.equals(Constants.UNDEFINED)) {
			createRdfOperation(operation,null,ValidationStatus.UNPUBLISHED);
		} else {
			createRdfOperation(operation,null,ValidationStatus.MODIFIED);
		}
		logger.info("Update operation : {} - {}" , operation.getId() , operation.getPrefLabelLg1());
	}

	private void createRdfOperation(Operation operation, IRI serieUri, ValidationStatus newStatus) throws RmesException {
		validate(operation);

		Model model = new LinkedHashModel();
		IRI operationURI = RdfUtils.objectIRI(ObjectType.OPERATION,operation.getId());
		/*Const*/
		model.add(operationURI, RDF.TYPE, INSEE.OPERATION, RdfUtils.operationsGraph());
		/*Required*/
		model.add(operationURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(operation.getPrefLabelLg1(), config.getLg1()), RdfUtils.operationsGraph());
		model.add(operationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newStatus.toString()), RdfUtils.operationsGraph());
		/*Optional*/
		RdfUtils.addTripleString(operationURI, SKOS.PREF_LABEL, operation.getPrefLabelLg2(), config.getLg2(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleString(operationURI, SKOS.ALT_LABEL, operation.getAltLabelLg1(), config.getLg1(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleString(operationURI, SKOS.ALT_LABEL, operation.getAltLabelLg2(), config.getLg2(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleDateTime(operationURI, DCTERMS.CREATED, operation.getCreated(), model, RdfUtils.operationsGraph());
		RdfUtils.addTripleDateTime(operationURI, DCTERMS.MODIFIED, operation.getModified(), model, RdfUtils.operationsGraph());

		if(operation.getYear() != null){
			model.add(operationURI, DCTERMS.TEMPORAL, RdfUtils.createLiteral(operation.getYear().toString(), XSD.GYEAR), RdfUtils.operationsGraph());
		}

		if (serieUri != null) {
			//case CREATION : link operation to series
			RdfUtils.addTripleUri(operationURI, DCTERMS.IS_PART_OF, serieUri, model, RdfUtils.operationsGraph());
			RdfUtils.addTripleUri(serieUri, DCTERMS.HAS_PART, operationURI, model, RdfUtils.operationsGraph());
		}

		repoGestion.keepHierarchicalOperationLinks(operationURI,model);
		repoGestion.loadSimpleObject(operationURI, model);
	}


	public void setOperationValidation(String idOperation)  throws RmesException  {
		Model model = new LinkedHashModel();

		//PUBLISH
		JSONObject operationJson = getOperationJsonById(idOperation);
		operationPublication.publishOperation(idOperation, operationJson);

		//UPDATE GESTION TO MARK AS PUBLISHED
		IRI operationURI = RdfUtils.objectIRI(ObjectType.OPERATION, idOperation);
		model.add(operationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), RdfUtils.operationsGraph());
		model.remove(operationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), RdfUtils.operationsGraph());
		model.remove(operationURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), RdfUtils.operationsGraph());
		logger.info("Validate operation : {}", operationURI);
		repoGestion.objectValidation(operationURI, model);

	}

}
