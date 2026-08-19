package fr.insee.rmes.modules.commons.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.modules.commons.configuration.ThemeProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ThemeQueries {

    private static final String THEMES_FOLDER = "fr/insee/rmes/modules/commons/";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;
    private final ThemeProperties themes;

    public ThemeQueries(BauhausLanguagesProperties languages, GraphsProperties graphs, ThemeProperties themes) {
        this.languages = languages;
        this.graphs = graphs;
        this.themes = themes;
    }

    public String getThemesQuery() throws RmesException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("THEMES_GRAPH", SparqlLiterals.iri(graphs.baseGraph() + themes.graph()));
        params.put("THEME_TYPE", SparqlLiterals.iri(themes.type()));
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        return FreeMarkerUtils.buildRequest(THEMES_FOLDER, "getTheme.ftlh", params);
    }
}
