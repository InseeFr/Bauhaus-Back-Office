package fr.insee.rmes.modules.operations.msd;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.documentation")
public record DocumentationConfiguration(
	String geographieGraph,
	String titlePrefixLg1,
	String titlePrefixLg2
) {}
