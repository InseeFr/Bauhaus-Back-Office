package fr.insee.rmes.modules.commons.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public record StorageProperties(String directoryGestion, String directoryPublication) {


}
