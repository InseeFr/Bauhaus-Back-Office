package fr.insee.rmes.onion.infrastructure.graphdb.operations.queries;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.GenericQueries;

public class SimsMigrationQueries extends GenericQueries {

    private SimsMigrationQueries() {
        throw new IllegalStateException("Utility class");
    }

    private static String buildRequest(String fileName) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, null);
    }

    public static String getSimsHtmlTextNodes() throws RmesException {
        return buildRequest("getSimsHtmlTextNodesQuery.ftlh");
    }
}