package fr.insee.rmes.persistance.sparql_queries.operations.operations;


import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class OperationsQueries extends GenericQueries {
	public static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";

	private static Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}

	private static String buildIndicatorRequest(String fileName, Map<String, Object> params) throws RmesException {
		return FreeMarkerUtils.buildRequest("operations/series/", fileName, params);
	}

	public static String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "/operations/operation/");
		params.put("TYPE", "insee:StatisticalOperation");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public static String operationsQuery() throws RmesException {
		Map<String, Object> params = initParams();
		return FreeMarkerUtils.buildRequest("operations/", "getOperations.ftlh", params);
	}


	public static String operationQuery(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest("operations/", "getOperation.ftlh", params);
	}

	public static String seriesQuery(String idOperation) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", idOperation);
		return FreeMarkerUtils.buildRequest("operations/series/", "getSeries.ftlh", params);
	}


	public static String operationsWithoutSimsQuery(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", idSeries);
		return FreeMarkerUtils.buildRequest("operations/series/", "getOperationsWithoutSimsQuery.ftlh", params);
	}


	public static String operationsWithSimsQuery(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", idSeries);
		return FreeMarkerUtils.buildRequest("operations/series/", "getOperationsWithSimsQuery.ftlh", params);
}
	
	  private OperationsQueries() {
		    throw new IllegalStateException("Utility class");
	}

	public static String seriesWithSimsQuery(String idFamily) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID_FAMILY", idFamily);
		return buildIndicatorRequest("getSeriesWithSimsQuery.ftlh", params);
	}
}
