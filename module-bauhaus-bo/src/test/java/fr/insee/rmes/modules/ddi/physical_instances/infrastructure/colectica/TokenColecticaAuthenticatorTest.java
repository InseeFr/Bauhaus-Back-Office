package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.keycloak.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenColecticaAuthenticatorTest {

    @Mock
    private TokenService tokenService;

    private TokenColecticaAuthenticator authenticator;

    private static final String KEYCLOAK_TOKEN = "keycloak-access-token";

    @BeforeEach
    void setUp() {
        authenticator = new TokenColecticaAuthenticator(tokenService);
    }

    @Test
    void shouldExecuteApiCallWithKeycloakToken() {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        String result = authenticator.executeWithAuth(token -> {
            assertEquals(KEYCLOAK_TOKEN, token);
            return "success";
        });

        assertEquals("success", result);
    }

    @Test
    void shouldCallTokenServiceForEachRequest() {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        AtomicInteger callCount = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            authenticator.executeWithAuth(token -> {
                callCount.incrementAndGet();
                assertEquals(KEYCLOAK_TOKEN, token);
                return "result";
            });
        }

        assertEquals(5, callCount.get());
    }

    @Test
    void shouldPropagateExceptionFromApiCall() {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticator.executeWithAuth(token -> {
                throw new RuntimeException("API call failed");
            });
        });

        assertEquals("API call failed", exception.getMessage());
    }

    @Test
    void shouldReturnResultFromApiCall() {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        Integer result = authenticator.executeWithAuth(token -> 42);

        assertEquals(42, result);
    }

    @Test
    void shouldHandleNullReturnFromApiCall() {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        String result = authenticator.executeWithAuth(token -> null);

        assertNull(result);
    }

    @Test
    void shouldWorkWithDifferentReturnTypes() {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        String stringResult = authenticator.executeWithAuth(token -> "string result");
        assertEquals("string result", stringResult);

        Integer intResult = authenticator.executeWithAuth(token -> 123);
        assertEquals(123, intResult);

        Boolean boolResult = authenticator.executeWithAuth(token -> true);
        assertTrue(boolResult);

        record TestObject(String name, int value) {}
        TestObject objectResult = authenticator.executeWithAuth(token -> new TestObject("test", 42));
        assertEquals("test", objectResult.name());
        assertEquals(42, objectResult.value());
    }
}
