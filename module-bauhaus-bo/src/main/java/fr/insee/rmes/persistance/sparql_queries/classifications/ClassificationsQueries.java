package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ClassificationsQueries {

	public static final String CLASSIFICATIONS = "classifications/";

	private final Config config;

	public ClassificationsQueries(Config config) {
		this.config = config;
	}

	public String classificationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassifications.ftlh", params);
	}

	public String getGraphUriById(String classifId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("classifId", classifId);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getGraphUriById.ftlh", params);
	}

	public String classificationQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassification.ftlh", params);
	}

	public String classificationQueryUri(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationUri.ftlh", params);
	}

	public String classificationItemsQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationItems.ftlh", params);
	}

	public String classificationsUriById(String[] ids) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IDS", ids);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationsUriById.ftlh", params);
	}

	public String classificationItemQueryUri(String classificationId, String itemId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("CLASSIFICATION_ID", classificationId);
		params.put("CLASSIFICATION_ITEM_ID", itemId);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationItemUri.ftlh", params);
	}
}