package fr.insee.rmes.keycloak;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.keycloak")
public record KeycloakProperties(Server server, RealmConfig defaultrealm, RealmConfig colecticarealm) {
    /**
     * @param connectTimeout time allowed to open the connection to Keycloak; 5 seconds when absent
     * @param readTimeout time allowed to wait for Keycloak's answer; 10 seconds when absent
     */
    public record Server(String url, Duration connectTimeout, Duration readTimeout) {
        public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
        public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);

        public Server {
            if (connectTimeout == null) {
                connectTimeout = DEFAULT_CONNECT_TIMEOUT;
            }
            if (readTimeout == null) {
                readTimeout = DEFAULT_READ_TIMEOUT;
            }
        }
    }

    public record RealmConfig(String name, String clientid, String clientsecret) {}

    public String tokenUrl(RealmConfig realmConfig) {
        return server.url() + "/realms/" + realmConfig.name() + "/protocol/openid-connect/token";
    }
}
