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
                new KeycloakProperties.Client("XXX", "XXX"),
                new KeycloakProperties.Server("keycloak.test"),
                new KeycloakProperties.Realm("default-realm", "colectica-realm")
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
    void getRealmName_shouldReturnColecticaRealm() {
        assertEquals("colectica-realm", colecticaKeycloakService.getRealmName());
    }

    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        var propertiesWithNullServer = new KeycloakProperties(
                new KeycloakProperties.Client("XXX", "XXX"),
                null,
                new KeycloakProperties.Realm("default-realm", "colectica-realm")
        );
        ColecticaKeycloakService serviceWithNullServer = new ColecticaKeycloakService(propertiesWithNullServer);

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }
}
