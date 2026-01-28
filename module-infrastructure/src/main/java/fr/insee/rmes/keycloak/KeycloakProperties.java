package fr.insee.rmes.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.keycloak")
public record KeycloakProperties(
        Server server,
        RealmConfig defaultRealm,
        RealmConfig colecticaRealm
) {
    public record Server(String url) {}
    public record RealmConfig(String name, String clientId, String clientSecret) {}

    public String tokenUrl(RealmConfig realmConfig) {
        return server.url() + "/realms/" + realmConfig.name() + "/protocol/openid-connect/token";
    }
}
