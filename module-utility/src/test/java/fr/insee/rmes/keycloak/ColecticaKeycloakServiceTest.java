package fr.insee.rmes.keycloak;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;

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
        colecticaKeycloakService = new ColecticaKeycloakService(KeycloakTestFixtures.properties());
        colecticaKeycloakService.keycloakClient = testRestClient;

        KeycloakTestFixtures.stubTokenRequest(testRestClient, requestBodyUriSpec, requestBodySpec, responseSpec);
        when(responseSpec.body(Token.class)).thenReturn(KeycloakTestFixtures.token("colectica-token"));
    }

    @Test
    void getAccessToken_shouldCallKeycloakServerWithColecticaRealm() {
        colecticaKeycloakService.getAccessToken();
        verify(requestBodyUriSpec).uri("keycloak.test/realms/colectica-realm/protocol/openid-connect/token");
    }

    @Test
    void getRealmConfig_shouldReturnColecticaRealm() {
        var realmConfig = colecticaKeycloakService.getRealmConfig();
        assertEquals("colectica-realm", realmConfig.name());
        assertEquals("colectica-client", realmConfig.clientid());
    }

    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        ColecticaKeycloakService serviceWithNullServer =
                new ColecticaKeycloakService(KeycloakTestFixtures.propertiesWithoutServer());

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }
}
