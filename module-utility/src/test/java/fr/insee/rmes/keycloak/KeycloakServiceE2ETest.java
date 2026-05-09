package fr.insee.rmes.keycloak;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Testcontainers()
class KeycloakServiceE2ETest {

    private static final String REALM_NAME = "bauhaus";
    private static final String CLIENT_ID = "bauhaus-client";
    private static final String CLIENT_SECRET = "test-secret";

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer();

    private static KeycloakService keycloakService;

    @BeforeAll
    static void setUp() {
        createRealmWithClient();
        var properties = new KeycloakProperties(
                new KeycloakProperties.Server(keycloak.getAuthServerUrl()),
                new KeycloakProperties.RealmConfig(REALM_NAME, CLIENT_ID, CLIENT_SECRET),
                new KeycloakProperties.RealmConfig("colectica", "colectica-client", "colectica-secret")
        );
        keycloakService = new KeycloakService(properties);
    }

    private static void createRealmWithClient() {
        try (Keycloak adminClient = keycloak.getKeycloakAdminClient()) {
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(REALM_NAME);
            realm.setEnabled(true);
            adminClient.realms().create(realm);

            ClientRepresentation client = new ClientRepresentation();
            client.setClientId(CLIENT_ID);
            client.setSecret(CLIENT_SECRET);
            client.setServiceAccountsEnabled(true);
            client.setDirectAccessGrantsEnabled(false);
            client.setPublicClient(false);
            client.setProtocol("openid-connect");
            client.setEnabled(true);

            adminClient.realm(REALM_NAME).clients().create(client);
        }
    }

    @Test
    void shouldGetAccessTokenFromKeycloak() {
        String token = keycloakService.getAccessToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldReturnValidToken() {
        String token = keycloakService.getAccessToken();

        assertTrue(keycloakService.isTokenValid(token));
    }

    @Test
    void shouldThrowExceptionWithInvalidCredentials() {
        var invalidProperties = new KeycloakProperties(
                new KeycloakProperties.Server(keycloak.getAuthServerUrl()),
                new KeycloakProperties.RealmConfig(REALM_NAME, CLIENT_ID, "wrong-secret"),
                new KeycloakProperties.RealmConfig("colectica", "colectica-client", "colectica-secret")
        );
        KeycloakService invalidService = new KeycloakService(invalidProperties);

        assertThrows(UnreachableKeycloakException.class, invalidService::getAccessToken);
    }
}
