package fr.insee.rmes.modules.commons.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.theme")
public record ThemeProperties(String conceptSchemeFilter) {}
