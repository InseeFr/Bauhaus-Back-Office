package fr.insee.rmes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetsConfig {

    @Autowired
    Config config;

    @Value("${fr.insee.rmes.bauhaus.datasets.graph}")
    private String datasetsGraph;

    @Value("${fr.insee.rmes.bauhaus.datasets.baseURI}")
    private String datasetsBaseUri;

    @Value("${fr.insee.rmes.bauhaus.theme.graph}")
    private String datasetsThemeGraph;

    @Value("${fr.insee.rmes.bauhaus.theme.conceptSchemeFilter}")
    private String datasetsConceptSchemeFilter;

    public String getDatasetsGraph() {
        return config.getBaseGraph() + datasetsGraph;
    }

    public String getDatasetsBaseUri() {
        return config.getBaseUriGestion() + datasetsBaseUri;
    }

    public String getDatasetsThemeGraph() {
        return datasetsThemeGraph;
    }

    public String getDatasetsConceptSchemeFilter() {
        return datasetsConceptSchemeFilter;
    }
}
