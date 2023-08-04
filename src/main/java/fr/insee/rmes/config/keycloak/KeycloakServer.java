package fr.insee.rmes.config.keycloak;

import java.net.URL;

public class KeycloakServer {

    private String secret;
    private String clientId;
    private String server;

    public KeycloakServer(String secret, String clientId, String server) {
        this.secret = secret;
        this.clientId = clientId;
        this.server = server;
    }

    public String secret() {
        return secret;
    }

    public String clientId() {
        return clientId;
    }

    public String server() {
        return server;
    }
}
