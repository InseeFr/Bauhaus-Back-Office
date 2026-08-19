package fr.insee.rmes.modules.operations.families.infrastructure.graphdb;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries.OPERATIONS_GRAPH;

@Component
public class OperationFamilyQueries {

    private final String lg1;
    private final String lg2;
    private final String baseGraph;
    private final String operationsGraph;

    public OperationFamilyQueries(
            @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
            @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.operations.graph}") String operationsGraph
    ) {
        this.lg1 = lg1;
        this.lg2 = lg2;
        this.baseGraph = baseGraph;
        this.operationsGraph = operationsGraph;
    }

    public String familiesQuery() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(this.lg1));
        return  buildRequest("getFamilies.ftlh", params);
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
    }

    public String familyQuery(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(lg1));
        params.put("LG2", SparqlLiterals.literal(lg2));
        params.put("ID", SparqlLiterals.literal(id));
        params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + id));
        return  buildRequest("getFamily.ftlh", params);
    }

    public String getSeries(String idFamily) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(lg1));
        params.put("LG2", SparqlLiterals.literal(lg2));
        params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + idFamily));
        return  buildRequest("getSeries.ftlh", params);
    }

    public String getSubjects(String idFamily) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(lg1));
        params.put("LG2", SparqlLiterals.literal(lg2));
        params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + idFamily));
        return  buildRequest("getSubjects.ftlh", params);
    }
}