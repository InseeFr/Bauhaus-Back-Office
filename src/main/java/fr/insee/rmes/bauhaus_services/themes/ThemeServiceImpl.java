package fr.insee.rmes.bauhaus_services.themes;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ThemeServiceImpl extends RdfService implements  ThemeService {
    @Value("${fr.insee.rmes.bauhaus.theme.graph}")
    private String datasetsThemeGraph;

    @Value("${fr.insee.rmes.bauhaus.theme.conceptSchemeFilter}")
    private String themesConceptSchemeFilter;

    @Override
    public JSONArray getThemes(String schemeFilter) throws RmesException {
        return this.repoGestion.getResponseAsArray(ThemeQueries.getThemes(schemeFilter, datasetsThemeGraph, themesConceptSchemeFilter));
    }
}