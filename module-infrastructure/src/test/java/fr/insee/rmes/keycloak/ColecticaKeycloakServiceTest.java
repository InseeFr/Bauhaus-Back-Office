package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ColecticaKeycloakServiceTest {

    @Mock
    private RestTemplate testRestTemplate;

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
        colecticaKeycloakService.keycloakClient = testRestTemplate;

        Token token = new Token() {
            @Override
            public String getAccessToken() {
                return "colectica-token";
            }
        };
        when(testRestTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Token.class))).thenReturn(token);
    }

    @Test
    void getAccessToken_shouldCallKeycloakServerWithColecticaRealm() {
        colecticaKeycloakService.getAccessToken();
        Mockito.verify(testRestTemplate).postForObject(
                eq("keycloak.test/realms/colectica-realm/protocol/openid-connect/token"),
                any(HttpEntity.class),
                eq(Token.class)
        );
    }

    @Test
    void getRealmConfig_shouldReturnColecticaRealm() {
        var realmConfig = colecticaKeycloakService.getRealmConfig();
        assertEquals("colectica-realm", realmConfig.name());
        assertEquals("colectica-client", realmConfig.clientId());
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
