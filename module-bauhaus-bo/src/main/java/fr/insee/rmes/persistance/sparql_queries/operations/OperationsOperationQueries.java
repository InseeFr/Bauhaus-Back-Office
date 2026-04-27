package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.Config;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperationsOperationQueries {
	public static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
	public static final String OPERATIONS_SERIES_FOLDER = "operations/series/";
	public static final String OPERATIONS_FOLDER = "operations/";

	private final Config config;

	public OperationsOperationQueries(Config config) {
		this.config = config;
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return params;
	}

	public String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, config.getOperationsGraph());
		params.put("LANG", lang);
		params.put("ID", id);
		params.put("LABEL", label);
		params.put("URI_PREFIX", "/operations/operation/");
		params.put("TYPE", "insee:StatisticalOperation");
		return FreeMarkerUtils.buildRequest(OPERATIONS_FOLDER, "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public String operationsQuery() throws RmesException {
		Map<String, Object> params = initParams();
		return FreeMarkerUtils.buildRequest(OPERATIONS_FOLDER, "getOperations.ftlh", params);
	}

	public String operationQuery(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", id);
		return FreeMarkerUtils.buildRequest(OPERATIONS_FOLDER, "getOperation.ftlh", params);
	}

	public String seriesQuery(String idOperation) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", idOperation);
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getSeries.ftlh", params);
	}

	public String operationsWithoutSimsQuery(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", idSeries);
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getOperationsWithoutSimsQuery.ftlh", params);
	}

	public String operationsWithSimsQuery(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID", idSeries);
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getOperationsWithSimsQuery.ftlh", params);
	}

	public String seriesWithSimsQuery(String idFamily) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("ID_FAMILY", idFamily);
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getSeriesWithSimsQuery.ftlh", params);
	}
}
