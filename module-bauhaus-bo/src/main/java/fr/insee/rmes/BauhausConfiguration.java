package fr.insee.rmes;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import fr.insee.rmes.modules.clientconfig.domain.model.ModuleSettings;
import fr.insee.rmes.modules.shared_kernel.domain.model.AuthenticationMode;
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
        AuthenticationMode.fromEnv(env);
    }

    public AuthenticationMode authenticationMode() {
        return AuthenticationMode.fromEnv(env);
    }

    /**
     * L'API n'exige un jeton qu'en mode {@link AuthenticationMode#OIDC} : en
     * {@link AuthenticationMode#DEV}, le {@code DevAuthenticationFilter} authentifie chaque requête sans
     * jeton. Seule source de ce critère, partagée par la chaîne de sécurité et par la documentation
     * OpenAPI.
     */
    public static boolean isAuthenticated(String env) {
        return AuthenticationMode.fromEnv(env) == AuthenticationMode.OIDC;
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
