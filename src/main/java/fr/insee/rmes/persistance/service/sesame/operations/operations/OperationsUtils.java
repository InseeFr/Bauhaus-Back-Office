package fr.insee.rmes.persistance.service.sesame.operations.operations;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
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

	public void setOperation(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Operation operation = new Operation(id);
		try {
			operation = mapper.readerForUpdating(operation).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfOperation(operation);
		logger.info("Update operation : " + operation.getId() + " - " + operation.getPrefLabelLg1());
		
	}


	public void createRdfOperation(Operation operation) throws RmesException {
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
		
		RepositoryGestion.keepHierarchicalOperationLinks(operationURI,model);
		
		RepositoryGestion.loadSimpleObject(operationURI, model, null);
	}

}
