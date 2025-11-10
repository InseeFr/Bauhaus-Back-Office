package fr.insee.rmes.colectica;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record ColecticaConfiguration(
        boolean mockServerEnabled,
        String baseUrl,
        String apiPath,
        List<String> itemTypes,
        String versionResponsibility,
        String itemFormat,
        String username,
        String password
) {
    public ColecticaConfiguration {
        // Set default apiPath if not provided
        if (apiPath == null || apiPath.isBlank()) {
            apiPath = "/api/v1/";
        }
    }

    public String baseServerUrl() {
        return baseUrl;
    }

    public String baseApiUrl() {
        return baseUrl + apiPath;
    }
}
