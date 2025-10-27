package fr.insee.rmes.colectica;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record ColecticaConfiguration(
        boolean mockServerEnabled,
        String baseURI,
        List<String> itemTypes
) {
}
