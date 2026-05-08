package fr.insee.rmes;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record PaginationProperties(int perPage) {
}
