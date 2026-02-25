package fr.insee.rmes.modules.operations.msd;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.documentation")
public record DocumentationConfiguration(
	Geographie geographie,
	String titlePrefixLg1,
	String titlePrefixLg2
) {
	public record Geographie(
		String graph,
		String baseUri
	) {}
}
