package fr.insee.rmes.modules.users.infrastructure;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Utilisateur fictif qui porte les requêtes hors PROD (voir {@code DevAuthenticationFilter}). */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.dev-user")
public record DevUserProperties(String id, List<String> roles, List<String> stamps, String source) {}
