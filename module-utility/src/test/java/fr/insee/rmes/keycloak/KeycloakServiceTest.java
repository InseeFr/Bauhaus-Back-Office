package fr.insee.rmes.keycloak;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class KeycloakServiceTest extends AbstractTokenServiceTest<KeycloakService> {

    @Override
    KeycloakService createService(KeycloakProperties properties) {
        return new KeycloakService(properties);
    }

    @Test
    void nowPlus1Second() {
        var start = new Date();
        var actual = service.nowPlus1Second();
        var nowPlus1 = Date.from(start.toInstant().plusSeconds(1));
        var nowPlus10 = Date.from(start.toInstant().plusSeconds(10));
        assertFalse(actual.before(nowPlus1));
        assertTrue(actual.before(nowPlus10));
    }

    @Test
    void getAccessToken_shouldCallKeycloakServerWithDefaultRealm() {
        service.getAccessToken();
        verify(requestBodyUriSpec).uri("keycloak.test/realms/default-realm/protocol/openid-connect/token");
    }

    @Test
    void getRealmConfig_shouldReturnDefaultRealm() {
        var realmConfig = service.getRealmConfig();
        assertEquals("default-realm", realmConfig.name());
        assertEquals("default-client", realmConfig.clientid());
    }

    @Test
    void shouldThrowMissingKeycloakConfigurationException_whenServerUrlIsNull() {
        KeycloakService serviceWithNullServer = new KeycloakService(KeycloakTestFixtures.propertiesWithoutServer());

        assertThrows(MissingKeycloakConfigurationException.class, serviceWithNullServer::getAccessToken);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsNull() {
        assertFalse(service.isTokenValid(null));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsNotAJwt() {
        assertFalse(service.isTokenValid("not-a-jwt"));
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenHasFutureExpiry() {
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plusSeconds(300)))
                .sign(Algorithm.HMAC256("secret"));
        assertTrue(service.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        String token = JWT.create()
                .withExpiresAt(Date.from(Instant.now().minusSeconds(300)))
                .sign(Algorithm.HMAC256("secret"));
        assertFalse(service.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenHasNoExpiryClaim() {
        String token = JWT.create().sign(Algorithm.HMAC256("secret"));
        assertFalse(service.isTokenValid(token));
    }

    private void stubKeycloakReturns(String accessToken) {
        when(responseSpec.body(Token.class)).thenReturn(KeycloakTestFixtures.token(accessToken));
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

        String first = service.getAccessToken();
        String second = service.getAccessToken();

        assertEquals(jwt, first);
        assertEquals(jwt, second);
        verify(testRestClient, times(1)).post();
    }

    @Test
    void getAccessToken_refetchesWhenCachedTokenIsExpired() {
        stubKeycloakReturns(jwtExpiringIn(-10));

        service.getAccessToken();
        service.getAccessToken();

        verify(testRestClient, times(2)).post();
    }

    @Test
    void getAccessToken_refetchesWhenTokenExpiresWithinSafetyMargin() {
        // exp in 10s while the refresh margin is larger → treated as stale to avoid mid-call expiry.
        stubKeycloakReturns(jwtExpiringIn(10));

        service.getAccessToken();
        service.getAccessToken();

        verify(testRestClient, times(2)).post();
    }

    @Test
    void invalidate_forcesRefetchOnNextCall() {
        stubKeycloakReturns(jwtExpiringIn(300));

        service.getAccessToken();
        service.invalidate();
        service.getAccessToken();

        verify(testRestClient, times(2)).post();
    }
}
