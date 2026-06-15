package fr.insee.rmes.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KeycloakServiceTest {

    @Mock
    private RestClient testRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        var properties = new KeycloakProperties(
                new KeycloakProperties.Server("keycloak.test"),
                new KeycloakProperties.RealmConfig("default-realm", "default-client", "default-secret"),
                new KeycloakProperties.RealmConfig("colectica-realm", "colectica-client", "colectica-secret")
        );
        keycloakService = new KeycloakService(properties);
        keycloakService.keycloakClient = testRestClient;

        when(testRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        Token token = new Token() {
            @Override
            public String getAccessToken() {
                return "token";
            }
        };
        when(responseSpec.body(eq(Token.class))).thenReturn(token);
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
    void getAccessToken_shouldCallKeycloakServerWithDefaultRealm() {
        keycloakService.getAccessToken();
        verify(requestBodyUriSpec).uri(
                eq("keycloak.test/realms/default-realm/protocol/openid-connect/token")
        );
    }

    @Test
    void getRealmConfig_shouldReturnDefaultRealm() {
        var realmConfig = keycloakService.getRealmConfig();
        assertEquals("default-realm", realmConfig.name());
        assertEquals("default-client", realmConfig.clientid());
    }

    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        var propertiesWithNullServer = new KeycloakProperties(
                null,
                new KeycloakProperties.RealmConfig("default-realm", "default-client", "default-secret"),
                new KeycloakProperties.RealmConfig("colectica-realm", "colectica-client", "colectica-secret")
        );
        KeycloakService serviceWithNullServer = new KeycloakService(propertiesWithNullServer);

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsNull() {
        assertFalse(keycloakService.isTokenValid(null));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsNotAJwt() {
        assertFalse(keycloakService.isTokenValid("not-a-jwt"));
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenHasFutureExpiry() {
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
                .sign(Algorithm.HMAC256("secret"));
        assertTrue(keycloakService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().minusSeconds(300)))
                .sign(Algorithm.HMAC256("secret"));
        assertFalse(keycloakService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenHasNoExpiryClaim() {
        String token = JWT.create().sign(Algorithm.HMAC256("secret"));
        assertFalse(keycloakService.isTokenValid(token));
    }

    private void stubKeycloakReturns(String accessToken) {
        when(responseSpec.body(eq(Token.class))).thenReturn(new Token() {
            @Override
            public String getAccessToken() {
                return accessToken;
            }
        });
    }

    private static String jwtExpiringIn(long seconds) {
        return JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(seconds)))
                .sign(Algorithm.HMAC256("secret"));
    }

    @Test
    void getAccessToken_cachesTokenAndDoesNotCallKeycloakAgainWhileValid() {
        String jwt = jwtExpiringIn(300);
        stubKeycloakReturns(jwt);

        String first = keycloakService.getAccessToken();
        String second = keycloakService.getAccessToken();

        assertEquals(jwt, first);
        assertEquals(jwt, second);
        verify(testRestClient, times(1)).post();
    }

    @Test
    void getAccessToken_refetchesWhenCachedTokenIsExpired() {
        stubKeycloakReturns(jwtExpiringIn(-10));

        keycloakService.getAccessToken();
        keycloakService.getAccessToken();

        verify(testRestClient, times(2)).post();
    }

    @Test
    void getAccessToken_refetchesWhenTokenExpiresWithinSafetyMargin() {
        // exp in 10s while the refresh margin is larger → treated as stale to avoid mid-call expiry.
        stubKeycloakReturns(jwtExpiringIn(10));

        keycloakService.getAccessToken();
        keycloakService.getAccessToken();

        verify(testRestClient, times(2)).post();
    }

    @Test
    void invalidate_forcesRefetchOnNextCall() {
        stubKeycloakReturns(jwtExpiringIn(300));

        keycloakService.getAccessToken();
        keycloakService.invalidate();
        keycloakService.getAccessToken();

        verify(testRestClient, times(2)).post();
    }
}
