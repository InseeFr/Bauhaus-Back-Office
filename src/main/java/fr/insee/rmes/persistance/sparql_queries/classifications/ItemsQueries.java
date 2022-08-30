package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class ItemsQueries extends GenericQueries{

	private ItemsQueries() {
		throw new IllegalStateException("Utility class");
	}

	private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("classifications/", fileName, params);
	}

	public static String itemQuery(String classificationId, String itemId) throws RmesException {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("CLASSIFICATION_ID", classificationId);
		params.put("ITEM_ID", itemId);

		return buildRequest("getClassificationItem.ftlh", params);
	}

	public static String itemAltQuery(String classificationId, String itemId) throws RmesException {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("CLASSIFICATION_ID", classificationId);
		params.put("ITEM_ID", itemId);

		return buildRequest("getClassificationItemAltLabels.ftlh", params);
	}
	
	public static String itemNotesQuery(String classificationId, String itemId, int conceptVersion) throws RmesException {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("CLASSIFICATION_ID", classificationId);
		params.put("ITEM_ID", itemId);
		params.put("CONCEPT_VERSION", conceptVersion);

		return buildRequest("getClassificationItemNotes.ftlh", params);
	}
	
	public static String itemNarrowersQuery(String classificationId, String itemId) throws RmesException {
		Map<String,Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		params.put("CLASSIFICATION_ID", classificationId);
		params.put("ITEM_ID", itemId);

		return buildRequest("getClassificationItemNarrowers.ftlh", params);
	}

}
