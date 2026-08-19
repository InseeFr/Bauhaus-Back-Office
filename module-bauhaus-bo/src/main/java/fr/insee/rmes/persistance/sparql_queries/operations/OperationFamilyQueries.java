package fr.insee.rmes.persistance.sparql_queries.operations;

import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component("legacyOperationFamilyQueries")
public class OperationFamilyQueries {

	private static final String OPERATIONS_GRAPH = "OPERATIONS_GRAPH";

    private final GraphsProperties graphs;

	public OperationFamilyQueries(GraphsProperties graphs) {
        this.graphs = graphs;
	}

	public String checkPrefLabelUnicity(String id, String label, String lang) throws RmesException {
		HashMap<String, Object> params = new HashMap<>();
		params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(graphs.operationsGraph()));
		params.put("LABEL", SparqlLiterals.literal(label, lang));
		params.put("URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + id));
		params.put("TYPE", "insee:StatisticalOperationFamily");
		return FreeMarkerUtils.buildRequest("operations/", "checkFamilyPrefLabelUnicity.ftlh", params);
	}

}