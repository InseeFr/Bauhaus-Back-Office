package fr.insee.rmes.persistance.service.sesame.operations.operations;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.JsonUtils;
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
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.utils.JSONUtils;

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
		if (! new SeriesUtils().checkIfSeriesExists(idSeries)) throw new RmesNotFoundException("Unknown series: ",idSeries);

		URI seriesURI = SesameUtils.objectIRI(ObjectType.SERIES,idSeries);
		createRdfOperation(operation, seriesURI);
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
		createRdfOperation(operation, null);
		logger.info("Update operation : " + operation.getId() + " - " + operation.getPrefLabelLg1());
		return operation.getId();
	}


	public static URI getOperationUriById(String id) throws RmesException {
		JSONObject operation = RepositoryGestion.getResponseAsObject(OperationsQueries.operationUriQuery(id));
		if (operation == null || operation.length()==0) {
			return null;
		}
		String uriStr = operation.get("uri").toString();
		return SesameUtils.toURI(uriStr);
	}


	private void createRdfOperation(Operation operation, URI serieUri) throws RmesException {
		Model model = new LinkedHashModel();
		URI operationURI = SesameUtils.objectIRI(ObjectType.OPERATION,operation.getId());
		/*Const*/
		model.add(operationURI, RDF.TYPE, INSEE.OPERATION, SesameUtils.operationsGraph());
		/*Required*/
		model.add(operationURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(operation.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
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

}
