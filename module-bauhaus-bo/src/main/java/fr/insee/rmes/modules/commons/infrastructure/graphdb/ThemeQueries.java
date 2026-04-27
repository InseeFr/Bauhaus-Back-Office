package fr.insee.rmes.modules.commons.infrastructure.graphdb;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ThemeQueries {

    private static final String THEMES_FOLDER = "fr/insee/rmes/modules/commons/";

    private final Config config;

    public ThemeQueries(Config config) {
        this.config = config;
    }

    public String getThemesQuery(String conceptSchemeFilter) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CONCEPTS_GRAPH", config.getConceptsGraph());
        params.put("LG1", config.getLg1());
        params.put("CONCEPT_SCHEME_FILTER", conceptSchemeFilter);
        return FreeMarkerUtils.buildRequest(THEMES_FOLDER, "getTheme.ftlh", params);
    }
}