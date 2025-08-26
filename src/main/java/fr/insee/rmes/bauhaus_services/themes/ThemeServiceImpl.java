package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ThemeServiceImpl implements ThemeService {
    private final String datasetsThemeGraph;
    private final String themesConceptSchemeFilter;
    private final RepositoryGestion repositoryGestion;

    public ThemeServiceImpl(
            @Value("${fr.insee.rmes.bauhaus.theme.graph}") String datasetsThemeGraph,
            @Value("${fr.insee.rmes.bauhaus.theme.conceptSchemeFilter}") String themesConceptSchemeFilter,
            RepositoryGestion repositoryGestion) {
        this.datasetsThemeGraph = datasetsThemeGraph;
        this.themesConceptSchemeFilter = themesConceptSchemeFilter;
        this.repositoryGestion = repositoryGestion;
    }


    @Override
    public JSONArray getThemes(String schemeFilter) throws RmesException {
        return this.repositoryGestion.getResponseAsArray(ThemeQueries.getThemes(schemeFilter, datasetsThemeGraph, themesConceptSchemeFilter));
    }
}