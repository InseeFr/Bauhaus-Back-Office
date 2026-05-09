package fr.insee.rmes.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.keycloak")
public record KeycloakProperties(
        Server server,
        RealmConfig defaultrealm,
        RealmConfig colecticarealm
) {
    public record Server(String url) {}
    public record RealmConfig(String name, String clientid, String clientsecret) {}

    public String tokenUrl(RealmConfig realmConfig) {
        return server.url() + "/realms/" + realmConfig.name() + "/protocol/openid-connect/token";
    }
}
