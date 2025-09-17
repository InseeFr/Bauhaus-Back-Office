package fr.insee.rmes.colectica;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record ColecticaConfiguration(
        boolean mockServerEnabled,
        String baseURI
) {
}
