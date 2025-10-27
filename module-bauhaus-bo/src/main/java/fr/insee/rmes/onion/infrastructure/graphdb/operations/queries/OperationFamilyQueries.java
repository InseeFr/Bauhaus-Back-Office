package fr.insee.rmes.onion.infrastructure.graphdb.operations.queries;

import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries.OPERATIONS_GRAPH;

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
        params.put(OPERATIONS_GRAPH, baseGraph + operationsGraph);
        params.put("LG1", this.lg1);
        return  buildRequest("getFamilies.ftlh", params);
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/famOpeSer/", fileName, params);
    }

    public String familyQuery(String id, boolean familiesRichTextNexStructure) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, baseGraph + operationsGraph);
        params.put("LG1", lg1);
        params.put("LG2", lg2);
        params.put("ID", id);
        params.put("FAMILIES_RICH_TEXT_NEXT_STRUCTURE", familiesRichTextNexStructure);
        return  buildRequest("getFamily.ftlh", params);
    }

    public String getSeries(String idFamily) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put(OPERATIONS_GRAPH, baseGraph + operationsGraph);
        params.put("LG1", lg1);
        params.put("LG2", lg2);
        params.put("ID", idFamily);
        return  buildRequest("getSeries.ftlh", params);
    }

    public String getSubjects(String idFamily) {
        return "SELECT  ?id ?labelLg1 ?labelLg2 \n"
                + " FROM <"+baseGraph + operationsGraph+"> \n"
                + "WHERE { \n"

                + "?family dcterms:subject ?subjectUri . \n"
                + "?subjectUri skos:prefLabel ?labelLg1 . \n"
                + "FILTER (lang(?labelLg1) = '" + lg1 + "') . \n"
                + "?subjectUri skos:prefLabel ?labelLg2 . \n"
                + "FILTER (lang(?labelLg2) = '" + lg2 + "') . \n"

                + "?subjectUri skos:notation ?id . \n"

                + "FILTER(STRENDS(STR(?family),'/operations/famille/" + idFamily + "')) . \n"
                + "}"
                + " ORDER BY ?subjectUri"
                ;
    }
}