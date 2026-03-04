package fr.insee.rmes;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record BauhausConfiguration(
        String env,
        String lg1,
        String lg2,
        boolean enableDevTools,
        String appHost,
        List<String> activeModules,
        List<String> modules,
        String version
) {
}
