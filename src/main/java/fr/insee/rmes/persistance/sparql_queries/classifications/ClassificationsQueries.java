package fr.insee.rmes.persistance.sparql_queries.classifications;

import java.util.HashMap;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

public class ClassificationsQueries  extends GenericQueries {
	
	public static String classificationsQuery() throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest("classifications/", "getClassifications.ftlh", params);
	}
	
	public static String getGraphUriById(String classifId) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("classifId", classifId);
		return FreeMarkerUtils.buildRequest("classifications/", "getGraphUriById.ftlh", params);
	}
	
	public static String classificationQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest("classifications/", "getClassification.ftlh", params);
	}

	public static String classificationQueryUri(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest("classifications/", "getClassificationUri.ftlh", params);
	}

	public static String classificationItemsQuery(String id) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest("classifications/", "getClassificationItems.ftlh", params);
	}
	
	
	private ClassificationsQueries() {
	    throw new IllegalStateException("Utility class");
	}


	public static String classificationsUriById(String[] ids) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("IDS", ids);
		return FreeMarkerUtils.buildRequest("classifications/", "getClassificationsUriById.ftlh", params);
	}
}
