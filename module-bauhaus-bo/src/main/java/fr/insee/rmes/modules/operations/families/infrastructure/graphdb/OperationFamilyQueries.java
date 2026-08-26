package fr.insee.rmes.modules.operations.families.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
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

    private final BauhausLanguagesProperties languages;
    private final String baseGraph;
    private final String operationsGraph;

    public OperationFamilyQueries(
            BauhausLanguagesProperties languages,
            @Value("${fr.insee.rmes.bauhaus.baseGraph}") String baseGraph,
            @Value("${fr.insee.rmes.bauhaus.operations.graph}") String operationsGraph
    ) {
        this.languages = languages;
        this.baseGraph = baseGraph;
        this.operationsGraph = operationsGraph;
    }

    public String familiesQuery() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        return  buildRequest("getFamilies.ftlh", params);
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
    }

    public String familyQuery(String id) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("ID", SparqlLiterals.literal(id));
        params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + id));
        return  buildRequest("getFamily.ftlh", params);
    }

    public String getSeries(String idFamily) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + idFamily));
        return  buildRequest("getSeries.ftlh", params);
    }

    public String getSubjects(String idFamily) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, SparqlLiterals.iri(baseGraph + operationsGraph));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));
        params.put("FAMILY_URI_SUFFIX", SparqlLiterals.literal("/operations/famille/" + idFamily));
        return  buildRequest("getSubjects.ftlh", params);
    }
}