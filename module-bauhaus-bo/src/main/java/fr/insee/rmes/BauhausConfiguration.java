package fr.insee.rmes;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record BauhausConfiguration(
        String env,
        boolean enableDevTools,
        String appHost,
        List<ModuleConfig> modules,
        String version,
        String baseGraph
) {
}
