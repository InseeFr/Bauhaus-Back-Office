package fr.insee.rmes;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import fr.insee.rmes.modules.clientconfig.domain.model.ModuleSettings;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus")
public record BauhausConfiguration(
        String env,
        boolean enableDevTools,
        String appHost,
        Map<String, ModuleSettings> modules,
        String version,
        String baseGraph) {
    public BauhausConfiguration {
        modules = modules == null ? Map.of() : modules;
    }

    /**
     * L'API n'exige un jeton qu'en PROD : ailleurs, le {@code DevAuthenticationFilter} authentifie
     * chaque requête sans jeton. Seule source de ce critère, partagée par la chaîne de sécurité et
     * par la documentation OpenAPI.
     */
    public static boolean isAuthenticated(String env) {
        return "PROD".equalsIgnoreCase(env);
    }

    public boolean authenticated() {
        return isAuthenticated(env);
    }

    /**
     * Les modules actifs, dans leur ordre de déclaration — c'est aussi l'ordre des tuiles sur
     * la page d'accueil.
     */
    public List<ModuleConfig> enabledModules() {
        return modules.entrySet().stream()
                .filter(entry -> entry.getValue().enabled())
                .map(entry -> new ModuleConfig(
                        entry.getKey(),
                        entry.getValue().show(),
                        entry.getValue().directAccess()))
                .toList();
    }
}
