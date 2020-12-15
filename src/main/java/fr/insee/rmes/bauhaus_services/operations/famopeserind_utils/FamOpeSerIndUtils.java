package fr.insee.rmes.bauhaus_services.operations.famopeserind_utils;

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
public class FamOpeSerIndUtils  extends RdfService {

	static final Logger logger = LogManager.getLogger(FamOpeSerIndUtils.class);

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
		return repoGestion.getResponseAsBoolean(FamOpeSerQueries.checkIfFamOpeSerExists(RdfUtils.objectIRI(type, id).toString()));
	}
	
	public String getValidationStatus(String id) throws RmesException{
		try {		
			return repoGestion.getResponseAsObject(FamOpeSerQueries.getPublicationState(id)).getString("state"); }
		catch (JSONException e) {
			return Constants.UNDEFINED;
		}
	}
	
	public IdLabelTwoLangs buildIdLabelTwoLangsFromJson(JSONObject jsonFamOpeSer) {
		IdLabelTwoLangs idLabelTwoLangs = new IdLabelTwoLangs();
		idLabelTwoLangs.setId(jsonFamOpeSer.getString("id"));
		if(jsonFamOpeSer.has("labelLg1")) {
			idLabelTwoLangs.setLabelLg1(jsonFamOpeSer.getString("labelLg1"));
		}
		if(jsonFamOpeSer.has("labelLg2")) {
			idLabelTwoLangs.setLabelLg2(jsonFamOpeSer.getString("labelLg2"));
		}
		return idLabelTwoLangs;
	}
	
	public List<String> buildStringListFromJson(JSONArray items) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < items.length(); i++) {
			String item = items.getString(i);
			result.add(item);
		}	
		return result;
	}
	
	public List<Object> buildObjectListFromJson(JSONArray items, String className) {
		List<Object> result = new ArrayList<>();
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
		for (int i = 0; i < items.length(); i++) {
			Object item = buildObjectFromJson(items.getJSONObject(i),cls);
			result.add(item);
		}	} catch (ClassNotFoundException e) {
			logger.error("JsonArray cannot be parsed to this class: ".concat(e.getMessage()));
		}
		return result;
	}
	
	public Object buildObjectFromJson(JSONObject objectJson, Class<?> cls) {
		ObjectMapper mapper = new ObjectMapper();
		Object result = new Object();
		try {
			result = mapper.readValue(objectJson.toString(), cls);
		} catch (JsonProcessingException e) {
			logger.error("OperationsLink Json cannot be parsed: ".concat(e.getMessage()));
		}
		return result;
	}
	
	public OperationsLink buildOperationsLinkFromJson(JSONObject operationsLinkJson) {
		ObjectMapper mapper = new ObjectMapper();
		OperationsLink operationsLink = new OperationsLink();
		try {
			operationsLink = mapper.readValue(operationsLinkJson.toString(), OperationsLink.class);
		} catch (JsonProcessingException e) {
			logger.error("OperationsLink Json cannot be parsed: ".concat(e.getMessage()));
		}
		return operationsLink;
	}

	public void fixOrganizationsNames(JSONObject series) {
		if(series.has(Constants.PUBLISHER)) {
			series.put(Constants.PUBLISHERS, series.get(Constants.PUBLISHER));
			series.remove(Constants.PUBLISHER);
		}
		if(series.has(Constants.CONTRIBUTOR)) {
			series.put(Constants.CONTRIBUTORS, series.get(Constants.CONTRIBUTOR));
			series.remove(Constants.CONTRIBUTOR);
		}
		if(series.has(Constants.DATA_COLLECTOR)) {
			series.put(Constants.DATA_COLLECTORS, series.get(Constants.DATA_COLLECTOR));
			series.remove(Constants.DATA_COLLECTOR);
		}
	}
	
}
