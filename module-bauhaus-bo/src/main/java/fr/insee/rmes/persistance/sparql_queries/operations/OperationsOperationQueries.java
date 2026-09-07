package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OperationsOperationQueries {
	public static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";
	public static final String OPERATIONS_SERIES_FOLDER = "operations/series/";
	public static final String OPERATIONS_FOLDER = "operations/";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

	public OperationsOperationQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
	}

	private Map<String, Object> initParams() {
		Map<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		return params;
	}

	public String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
		params.put("LABEL", SparqlLiterals.literal(label, lang));
		params.put("URI_SUFFIX", SparqlLiterals.literal("/operations/operation/" + id));
		params.put("TYPE", "insee:StatisticalOperation");
		return FreeMarkerUtils.buildRequest(OPERATIONS_FOLDER, "checkFamilyPrefLabelUnicity.ftlh", params);
	}

	public String operationsQuery() throws RmesException {
		Map<String, Object> params = initParams();
		return FreeMarkerUtils.buildRequest(OPERATIONS_FOLDER, "getOperations.ftlh", params);
	}

	public String operationQuery(String id) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("OPERATION_URI_SUFFIX", SparqlLiterals.literal("/operations/operation/" + id));
		return FreeMarkerUtils.buildRequest(OPERATIONS_FOLDER, "getOperation.ftlh", params);
	}

	public String seriesQuery(String idOperation) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("OPERATION_URI_SUFFIX", SparqlLiterals.literal("/operations/operation/" + idOperation));
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getSeries.ftlh", params);
	}

	public String operationsWithoutSimsQuery(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("SERIES_URI_SUFFIX", SparqlLiterals.literal("/operations/serie/" + idSeries));
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getOperationsWithoutSimsQuery.ftlh", params);
	}

	public String operationsWithSimsQuery(String idSeries) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("SERIES_URI_SUFFIX", SparqlLiterals.literal("/operations/serie/" + idSeries));
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getOperationsWithSimsQuery.ftlh", params);
	}

	public String seriesWithSimsQuery(String idFamily) throws RmesException {
		Map<String, Object> params = initParams();
		params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + idFamily));
		return FreeMarkerUtils.buildRequest(OPERATIONS_SERIES_FOLDER, "getSeriesWithSimsQuery.ftlh", params);
	}
}
