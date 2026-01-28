package fr.insee.rmes.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class KeycloakServiceTest {

    @Mock
    private RestTemplate testRestTemplate;

    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var properties = new KeycloakProperties(
                new KeycloakProperties.Client("XXX", "XXX"),
                new KeycloakProperties.Server("keycloak.test"),
                new KeycloakProperties.Realm("test-realm", "colectica-realm")
        );
        keycloakService = new KeycloakService(properties);
        keycloakService.keycloakClient = testRestTemplate;

        Token token = new Token() {
            @Override
            public String getAccessToken() {
                return "token";
            }
        };
        when(testRestTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Token.class))).thenReturn(token);
    }

    @Test
    void nowPlus1Second() {
        var start = new Date();
        var actual = keycloakService.nowPlus1Second();
        var nowPlus1 = Date.from(start.toInstant().plusSeconds(1));
        var nowPlus10 = Date.from(start.toInstant().plusSeconds(10));
        assertFalse(actual.before(nowPlus1));
        assertTrue(actual.before(nowPlus10));
    }

    @Test
    void getAccessToken_shouldCallKeycloakServer() {
        keycloakService.getAccessToken();
        Mockito.verify(testRestTemplate).postForObject(eq("keycloak.test/realms/test-realm/protocol/openid-connect/token"), any(HttpEntity.class), eq(Token.class));
    }



    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        var propertiesWithNullServer = new KeycloakProperties(
                new KeycloakProperties.Client("XXX", "XXX"),
                null,
                new KeycloakProperties.Realm("test-realm", "colectica-realm")
        );
        KeycloakService serviceWithNullServer = new KeycloakService(propertiesWithNullServer);

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }
}
