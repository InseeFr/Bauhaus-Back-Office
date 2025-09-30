package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakServerTest {

    @Test
    void shouldCreateKeycloakServerWithAllParameters() {
        String secret = "test-secret";
        String clientId = "test-client-id";
        String server = "http://keycloak.test.com";

        KeycloakServer keycloakServer = new KeycloakServer(secret, clientId, server);

        assertEquals(secret, keycloakServer.secret());
        assertEquals(clientId, keycloakServer.clientId());
        assertEquals(server, keycloakServer.server());
    }

    @Test
    void shouldHandleNullValues() {
        KeycloakServer keycloakServer = new KeycloakServer(null, null, null);

        assertNull(keycloakServer.secret());
        assertNull(keycloakServer.clientId());
        assertNull(keycloakServer.server());
    }

    @Test
    void shouldHandleEmptyValues() {
        String secret = "";
        String clientId = "";
        String server = "";

        KeycloakServer keycloakServer = new KeycloakServer(secret, clientId, server);

        assertEquals(secret, keycloakServer.secret());
        assertEquals(clientId, keycloakServer.clientId());
        assertEquals(server, keycloakServer.server());
    }

    @Test
    void shouldReturnCorrectSecretValue() {
        String expectedSecret = "my-super-secret-key";
        KeycloakServer keycloakServer = new KeycloakServer(expectedSecret, "client", "server");

        assertEquals(expectedSecret, keycloakServer.secret());
    }

    @Test
    void shouldReturnCorrectClientIdValue() {
        String expectedClientId = "bauhaus-client";
        KeycloakServer keycloakServer = new KeycloakServer("secret", expectedClientId, "server");

        assertEquals(expectedClientId, keycloakServer.clientId());
    }

    @Test
    void shouldReturnCorrectServerValue() {
        String expectedServer = "https://keycloak.insee.fr/auth";
        KeycloakServer keycloakServer = new KeycloakServer("secret", "client", expectedServer);

        assertEquals(expectedServer, keycloakServer.server());
    }

    @Test
    void shouldHandleLongValues() {
        String longSecret = "a".repeat(1000);
        String longClientId = "b".repeat(500);
        String longServer = "c".repeat(200);

        KeycloakServer keycloakServer = new KeycloakServer(longSecret, longClientId, longServer);

        assertEquals(longSecret, keycloakServer.secret());
        assertEquals(longClientId, keycloakServer.clientId());
        assertEquals(longServer, keycloakServer.server());
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String secret = "secret@#$%^&*()";
        String clientId = "client-id_123";
        String server = "https://keycloak.test.com:8080/auth/realms/test";

        KeycloakServer keycloakServer = new KeycloakServer(secret, clientId, server);

        assertEquals(secret, keycloakServer.secret());
        assertEquals(clientId, keycloakServer.clientId());
        assertEquals(server, keycloakServer.server());
    }
}