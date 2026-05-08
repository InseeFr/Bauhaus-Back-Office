package fr.insee.rmes.modules.commons.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ThemeQueries {

    private static final String THEMES_FOLDER = "fr/insee/rmes/modules/commons/";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

    public ThemeQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
    }

    public String getThemesQuery(String conceptSchemeFilter) throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("CONCEPTS_GRAPH", graphs.conceptsGraph());
        params.put("LG1", languages.lg1());
        params.put("CONCEPT_SCHEME_FILTER", conceptSchemeFilter);
        return FreeMarkerUtils.buildRequest(THEMES_FOLDER, "getTheme.ftlh", params);
    }
}