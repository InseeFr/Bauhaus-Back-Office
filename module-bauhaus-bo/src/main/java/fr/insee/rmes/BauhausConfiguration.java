package fr.insee.rmes;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import fr.insee.rmes.modules.clientconfig.domain.model.ModuleSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record BauhausConfiguration(
        String env,
        boolean enableDevTools,
        String appHost,
        Map<String, ModuleSettings> modules,
        String version,
        String baseGraph
) {
    public BauhausConfiguration {
        modules = modules == null ? Map.of() : modules;
    }

    /**
     * Les modules actifs, dans leur ordre de déclaration — c'est aussi l'ordre des tuiles sur
     * la page d'accueil.
     */
    public List<ModuleConfig> enabledModules() {
        return modules.entrySet().stream()
                .filter(entry -> entry.getValue().enabled())
                .map(entry -> new ModuleConfig(
                        entry.getKey(), entry.getValue().show(), entry.getValue().directAccess()))
                .toList();
    }
}
