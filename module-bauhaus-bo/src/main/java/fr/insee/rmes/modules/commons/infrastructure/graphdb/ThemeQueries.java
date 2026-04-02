package fr.insee.rmes.modules.commons.infrastructure.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.GenericQueries;

import java.util.HashMap;

public class ThemeQueries extends GenericQueries {

    private static final String THEMES_FOLDER = "fr/insee/rmes/modules/commons/";

    public static String getThemesQuery(String conceptSchemeFilter) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CONCEPTS_GRAPH", config.getConceptsGraph());
        params.put("LG1", config.getLg1());
        params.put("CONCEPT_SCHEME_FILTER", conceptSchemeFilter);
        return FreeMarkerUtils.buildRequest(THEMES_FOLDER, "getTheme.ftlh", params);
    }

    private ThemeQueries() {
        throw new IllegalStateException("Utility class");
    }
}