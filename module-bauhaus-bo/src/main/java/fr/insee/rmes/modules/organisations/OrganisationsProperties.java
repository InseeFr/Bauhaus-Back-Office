package fr.insee.rmes.modules.organisations;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.organisations")
public record OrganisationsProperties(String graph) {}
