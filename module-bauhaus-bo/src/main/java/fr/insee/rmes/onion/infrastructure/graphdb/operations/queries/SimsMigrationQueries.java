package fr.insee.rmes.onion.infrastructure.graphdb.operations.queries;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class SimsMigrationQueries extends GenericQueries {

    private SimsMigrationQueries() {
        throw new IllegalStateException("Utility class");
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, params);
    }

    public static String getSimsHtmlTextNodes(int limit, int offset) throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("batchSize", limit);
        params.put("offset", offset);
        return buildRequest("getSimsHtmlTextNodesQuery.ftlh", params);
    }
}