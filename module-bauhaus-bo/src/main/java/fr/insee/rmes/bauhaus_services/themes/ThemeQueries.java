package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Optional;

public class ThemeQueries  extends GenericQueries  {
    private static final String ROOT_DIRECTORY = "theme/";

    public static String getThemes(String schemeFilter, String conceptsGraph, String themesConceptSchemeFilter) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CONCEPTS_GRAPH", config.getBaseGraph() + conceptsGraph);
        params.put("LG1", config.getLg1());
        params.put("CONCEPT_SCHEME_FILTER", Optional.ofNullable(schemeFilter).orElse(themesConceptSchemeFilter));
        return FreeMarkerUtils.buildRequest(ROOT_DIRECTORY, "getThemes.ftlh", params);
    }
}