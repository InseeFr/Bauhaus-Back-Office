package fr.insee.rmes.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.keycloak")
public record KeycloakProperties(
        Client client,
        Server server,
        Realm realm
) {
    public record Client(String id, String secret) {}
    public record Server(String url) {}
    public record Realm(String defaultValue, String colectica) {}

    public String tokenUrl() {
        return tokenUrl(realm.defaultValue());
    }

    public String tokenUrl(String realmName) {
        return server.url() + "/realms/" + realmName + "/protocol/openid-connect/token";
    }
}
