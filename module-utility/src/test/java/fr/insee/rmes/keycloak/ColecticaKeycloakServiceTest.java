package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ColecticaKeycloakServiceTest {

    @Mock
    private RestClient testRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ColecticaKeycloakService colecticaKeycloakService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var properties = new KeycloakProperties(
                new KeycloakProperties.Server("keycloak.test"),
                new KeycloakProperties.RealmConfig("default-realm", "default-client", "default-secret"),
                new KeycloakProperties.RealmConfig("colectica-realm", "colectica-client", "colectica-secret")
        );
        colecticaKeycloakService = new ColecticaKeycloakService(properties);
        colecticaKeycloakService.keycloakClient = testRestClient;

        when(testRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Token token = new Token() {
            @Override
            public String getAccessToken() {
                return "colectica-token";
            }
        };
        when(responseSpec.body(eq(Token.class))).thenReturn(token);
    }

    @Test
    void getAccessToken_shouldCallKeycloakServerWithColecticaRealm() {
        colecticaKeycloakService.getAccessToken();
        verify(requestBodyUriSpec).uri(
                eq("keycloak.test/realms/colectica-realm/protocol/openid-connect/token")
        );
    }

    @Test
    void getRealmConfig_shouldReturnColecticaRealm() {
        var realmConfig = colecticaKeycloakService.getRealmConfig();
        assertEquals("colectica-realm", realmConfig.name());
        assertEquals("colectica-client", realmConfig.clientid());
    }

    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        var propertiesWithNullServer = new KeycloakProperties(
                null,
                new KeycloakProperties.RealmConfig("default-realm", "default-client", "default-secret"),
                new KeycloakProperties.RealmConfig("colectica-realm", "colectica-client", "colectica-secret")
        );
        ColecticaKeycloakService serviceWithNullServer = new ColecticaKeycloakService(propertiesWithNullServer);

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }
}
