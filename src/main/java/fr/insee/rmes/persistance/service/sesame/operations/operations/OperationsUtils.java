package fr.insee.rmes.persistance.service.sesame.operations.operations;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class OperationsUtils {

	final static Logger logger = LogManager.getLogger(OperationsUtils.class);


	public JSONObject getOperationById(String id) throws RmesException {
		JSONObject operation = RepositoryGestion.getResponseAsObject(OperationsQueries.operationQuery(id));
		getOperationSeries(id, operation);
		return operation;
	}

	private void getOperationSeries(String id, JSONObject operation) throws RmesException {
		JSONObject series = RepositoryGestion.getResponseAsObject(OperationsQueries.seriesQuery(id));
		operation.put("series", series);
	}

	/**
	 * CREATE
	 * @param body
	 * @return
	 * @throws RmesException
	 */
	public String setOperation(String body) throws RmesException {
		SeriesUtils seriesUtils= new SeriesUtils();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Operation operation = new Operation();
		try {
			operation = mapper.readValue(body, Operation.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		// Tester l'existence de la série
		String idSeries= operation.getSeries().getId();
		if (! FamOpeSerUtils.checkIfObjectExists(ObjectType.SERIES,idSeries)) throw new RmesNotFoundException("Unknown series: ",idSeries);
		// Tester que la série n'a pas de Sims
		if (seriesUtils.hasSims(idSeries)){
			throw new RmesNotAcceptableException("A series cannot have both a Sims and Operation(s)", 
					seriesUtils.getSeriesById(idSeries).getString("prefLabelLg1")+" ; "+operation.getPrefLabelLg1());
		}
		URI seriesURI = SesameUtils.objectIRI(ObjectType.SERIES,idSeries);
		createRdfOperation(operation, seriesURI, INSEE.UNPUBLISHED);
		logger.info("Create operation : " + operation.getId() + " - " + operation.getPrefLabelLg1());

		return operation.getId();
	}

	/**
	 * UPDATE
	 * @param id
	 * @param body
	 * @return
	 * @throws RmesException
	 */
	public String setOperation(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Operation operation = new Operation(id);
		try {
			operation = mapper.readerForUpdating(operation).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		String status=FamOpeSerUtils.getValidationStatus(id);
		if(status.equals(INSEE.UNPUBLISHED) | status.equals("UNDEFINED")) {
			createRdfOperation(operation,null,INSEE.UNPUBLISHED);
		}
		else 	createRdfOperation(operation,null,INSEE.MODIFIED);
		logger.info("Update operation : " + operation.getId() + " - " + operation.getPrefLabelLg1());
		return operation.getId();
	}

	private void createRdfOperation(Operation operation, URI serieUri, String newStatus) throws RmesException {
		Model model = new LinkedHashModel();
		URI operationURI = SesameUtils.objectIRI(ObjectType.OPERATION,operation.getId());
		/*Const*/
		model.add(operationURI, RDF.TYPE, INSEE.OPERATION, SesameUtils.operationsGraph());
		/*Required*/
		model.add(operationURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(operation.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
		model.add(operationURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(newStatus.toString()), SesameUtils.operationsGraph());
		/*Optional*/
		SesameUtils.addTripleString(operationURI, SKOS.PREF_LABEL, operation.getPrefLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(operationURI, SKOS.ALT_LABEL, operation.getAltLabelLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(operationURI, SKOS.ALT_LABEL, operation.getAltLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		if (serieUri != null) {
			//case CREATION : link operation to series
			SesameUtils.addTripleUri(operationURI, DCTERMS.IS_PART_OF, serieUri, model, SesameUtils.operationsGraph());
			SesameUtils.addTripleUri(serieUri, DCTERMS.HAS_PART, operationURI, model, SesameUtils.operationsGraph());
		}

		RepositoryGestion.keepHierarchicalOperationLinks(operationURI,model);
		RepositoryGestion.loadSimpleObject(operationURI, model, null);
	}


	public String setOperationValidation(String id)  throws RmesUnauthorizedException, RmesException  {
		Model model = new LinkedHashModel();
		
		//TODO Check autorisation
		OperationPublication.publishOperation(id);
		
			URI operationURI = SesameUtils.objectIRI(ObjectType.OPERATION, id);
			model.add(operationURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.VALIDATED), SesameUtils.operationsGraph());
			model.remove(operationURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.UNPUBLISHED), SesameUtils.operationsGraph());
			model.remove(operationURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.MODIFIED), SesameUtils.operationsGraph());
			logger.info("Validate operation : " + operationURI);

			RepositoryGestion.objectsValidation(operationURI, model);
			
		return id;
	}
	
}
