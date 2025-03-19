package fr.insee.rmes.config.keycloak;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakServerTest {


    @Test
    void secret() {
        KeycloakServer keycloakServer = new KeycloakServer("secret", "clientID", "server");
        assertEquals("secret",keycloakServer.secret());
    }

    @Test
    void clientId() {
        KeycloakServer keycloakServer = new KeycloakServer("secret", "clientID", "server");
        assertEquals("clientID",keycloakServer.clientId());
    }

    @Test
    void server() {
        KeycloakServer keycloakServer = new KeycloakServer("secret", "clientID", "server");
        assertEquals("server",keycloakServer.server());
    }

}