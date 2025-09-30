package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;

import java.util.HashMap;

public class ClassificationsQueries  extends GenericQueries {

	public static final String CLASSIFICATIONS = "classifications/";

	public static String classificationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassifications.ftlh", params);
	}
	
	public static String getGraphUriById(String classifId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("classifId", classifId);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getGraphUriById.ftlh", params);
	}
	
	public static String classificationQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassification.ftlh", params);
	}

	public static String classificationQueryUri(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationUri.ftlh", params);
	}

	public static String classificationItemsQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationItems.ftlh", params);
	}
	
	
	private ClassificationsQueries() {
	    throw new IllegalStateException("Utility class");
	}


	public static String classificationsUriById(String[] ids) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IDS", ids);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationsUriById.ftlh", params);
	}

	public static String classificationItemQueryUri(String classificationId, String itemId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("CLASSIFICATION_ID", classificationId);
		params.put("CLASSIFICATION_ITEM_ID", itemId);
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getClassificationItemUri.ftlh", params);
	}
}
