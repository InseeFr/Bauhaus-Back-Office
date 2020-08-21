package fr.insee.rmes.bauhaus_services.operations.famopeser_utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils.FamOpeSerQueries;

@Component
public class FamOpeSerUtils  extends RdfService {

	static final Logger logger = LogManager.getLogger(FamOpeSerUtils.class);

	public String createId() throws RmesException {
		logger.info("Generate famOpeSer id");
		JSONObject json = repoGestion.getResponseAsObject(FamOpeSerQueries.lastId());
		logger.debug("JSON for famOpeSer id : {}", json);
		if (json.length()==0) {return "1000";}
		String id = json.getString(Constants.ID);
		if (id.equals(Constants.UNDEFINED)) {return "1000";}
		return "s" + (Integer.parseInt(id)+1);
	}

	public boolean checkIfObjectExists(ObjectType type, String id) throws RmesException {
		return repoGestion.getResponseAsBoolean(FamOpeSerQueries.checkIfOperationExists(RdfUtils.objectIRI(type, id).toString()));
	}
	
	public String getValidationStatus(String id) throws RmesException{
		try {		
			return repoGestion.getResponseAsObject(FamOpeSerQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}
	
	public IdLabelTwoLangs buildIdLabelTwoLangsFromJson(JSONObject jsonFamOpeSer) {
		IdLabelTwoLangs series = new IdLabelTwoLangs();
		series.setId(jsonFamOpeSer.getString("id"));
		if(jsonFamOpeSer.has("labelLg1")) {
			series.setLabelLg1(jsonFamOpeSer.getString("labelLg1"));
		}
		if(jsonFamOpeSer.has("labelLg2")) {
			series.setLabelLg2(jsonFamOpeSer.getString("labelLg2"));
		}
		return series;
	}
	
	public List<String> buildStringListFromJson(JSONArray items) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < items.length(); i++) {
			String item = items.getString(i);
			result.add(item);
		}	
		return result;
	}
	
	public List<Object> buildObjectListFromJson(JSONArray items, String className) throws RmesException {
		List<Object> result = new ArrayList<Object>();
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		for (int i = 0; i < items.length(); i++) {
			Object item = buildObjectFromJson(items.getJSONObject(i),cls);
			result.add(item);
		}	} catch (ClassNotFoundException e) {
			logger.error("JsonArray cannot be parsed to this class");
			e.printStackTrace();
		}
		return result;
	}
	
	public Object buildObjectFromJson(JSONObject objectJson, Class<?> cls) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		Object result = new Object();
		try {
			result = mapper.readValue(objectJson.toString(), cls);
		} catch (JsonProcessingException e) {
			logger.error("OperationsLink Json cannot be parsed");
			e.printStackTrace();
		}
		return result;
	}
	
	public OperationsLink buildOperationsLinkFromJson(JSONObject operationsLinkJson) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		OperationsLink operationsLink = new OperationsLink();
		try {
			operationsLink = mapper.readValue(operationsLinkJson.toString(), OperationsLink.class);
		} catch (JsonProcessingException e) {
			logger.error("OperationsLink Json cannot be parsed");
			e.printStackTrace();
		}
		return operationsLink;
	}

	
}
