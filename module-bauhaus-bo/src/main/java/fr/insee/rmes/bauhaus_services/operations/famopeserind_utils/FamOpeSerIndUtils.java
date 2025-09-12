package fr.insee.rmes.bauhaus_services.operations.famopeserind_utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.persistance.sparql_queries.operations.famOpeSerUtils.FamOpeSerQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FamOpeSerIndUtils  extends RdfService {

	static final Logger logger = LoggerFactory.getLogger(FamOpeSerIndUtils.class);

	public String createId() throws RmesException {
		logger.info("Generate famOpeSer id");
		JSONObject json = repoGestion.getResponseAsObject(FamOpeSerQueries.lastId());
		logger.debug("JSON for famOpeSer id : {}", json);
		if (json.isEmpty()) {return "1000";}
		String id = json.getString(Constants.ID);
		if (id.equals(Constants.UNDEFINED)) {return "1000";}
		return "s" + (Integer.parseInt(id)+1);
	}

	public boolean checkIfObjectExists(ObjectType type, String id) throws RmesException {
		return repoGestion.getResponseAsBoolean(FamOpeSerQueries.checkIfFamOpeSerExists(RdfUtils.toString(RdfUtils.objectIRI(type, id))));
	}
	
	public IdLabelTwoLangs buildIdLabelTwoLangsFromJson(JSONObject jsonFamOpeSer) {
		IdLabelTwoLangs idLabelTwoLangs = new IdLabelTwoLangs();
		idLabelTwoLangs.setId(jsonFamOpeSer.getString(Constants.ID));
		if(jsonFamOpeSer.has(Constants.LABEL_LG1)) {
			idLabelTwoLangs.setLabelLg1(jsonFamOpeSer.getString(Constants.LABEL_LG1));
		}
		if(jsonFamOpeSer.has(Constants.LABEL_LG2)) {
			idLabelTwoLangs.setLabelLg2(jsonFamOpeSer.getString(Constants.LABEL_LG2));
		}
		if(jsonFamOpeSer.has(Constants.CREATORS)) {
			List<String> stringList = new ArrayList<>();
			for (int i = 0; i < jsonFamOpeSer.getJSONArray(Constants.CREATORS).length(); i++) {
				Object element = jsonFamOpeSer.getJSONArray(Constants.CREATORS).get(i);
				if (element instanceof String) {
					stringList.add((String) element);
				}
			}
			idLabelTwoLangs.setCreators(stringList);
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
		Class<?> cls;
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
