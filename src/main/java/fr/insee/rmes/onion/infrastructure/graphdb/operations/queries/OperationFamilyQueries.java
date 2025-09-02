package fr.insee.rmes.onion.infrastructure.graphdb.operations.queries;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries.OPERATIONS_GRAPH;

@Component
public class OperationFamilyQueries {

    private final String lg1;
    private final String baseGraph;
    private final String operationsGraph;

    public OperationFamilyQueries(
            @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.operations.graph}") String operationsGraph
    ) {
        this.lg1 = lg1;
        this.baseGraph = baseGraph;
        this.operationsGraph = operationsGraph;
    }

    public String familiesQuery() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, baseGraph + operationsGraph);
        params.put("LG1", this.lg1);
        return  buildRequest("getFamilies.ftlh", params);
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
    }
}
