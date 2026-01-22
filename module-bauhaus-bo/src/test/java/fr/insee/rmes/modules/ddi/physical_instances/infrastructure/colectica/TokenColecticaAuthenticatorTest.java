package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenColecticaAuthenticatorTest {

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    private static final String STATIC_TOKEN = "bauhaus-mock-token";

    @Test
    void shouldExecuteApiCallWithStaticToken() {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(STATIC_TOKEN);

        TokenColecticaAuthenticator authenticator = new TokenColecticaAuthenticator(colecticaConfiguration);

        // When
        String result = authenticator.executeWithAuth(token -> {
            assertEquals(STATIC_TOKEN, token);
            return "success";
        });

        // Then
        assertEquals("success", result);
    }

    @Test
    void shouldAlwaysUseTheSameToken() {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(STATIC_TOKEN);

        TokenColecticaAuthenticator authenticator = new TokenColecticaAuthenticator(colecticaConfiguration);

        AtomicInteger callCount = new AtomicInteger(0);

        // When - Call multiple times
        for (int i = 0; i < 5; i++) {
            authenticator.executeWithAuth(token -> {
                callCount.incrementAndGet();
                assertEquals(STATIC_TOKEN, token);
                return "result";
            });
        }

        // Then - All calls should have received the same token
        assertEquals(5, callCount.get());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldThrowExceptionWhenTokenIsInvalid(String invalidToken) {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(invalidToken);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new TokenColecticaAuthenticator(colecticaConfiguration);
        });

        assertEquals("Token authentication mode requires a non-empty token configuration", exception.getMessage());
    }

    @Test
    void shouldPropagateExceptionFromApiCall() {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(STATIC_TOKEN);

        TokenColecticaAuthenticator authenticator = new TokenColecticaAuthenticator(colecticaConfiguration);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticator.executeWithAuth(token -> {
                throw new RuntimeException("API call failed");
            });
        });

        assertEquals("API call failed", exception.getMessage());
    }

    @Test
    void shouldReturnResultFromApiCall() {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(STATIC_TOKEN);

        TokenColecticaAuthenticator authenticator = new TokenColecticaAuthenticator(colecticaConfiguration);

        // When
        Integer result = authenticator.executeWithAuth(token -> 42);

        // Then
        assertEquals(42, result);
    }

    @Test
    void shouldHandleNullReturnFromApiCall() {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(STATIC_TOKEN);

        TokenColecticaAuthenticator authenticator = new TokenColecticaAuthenticator(colecticaConfiguration);

        // When
        String result = authenticator.executeWithAuth(token -> null);

        // Then
        assertNull(result);
    }

    @Test
    void shouldWorkWithDifferentReturnTypes() {
        // Given
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.token()).thenReturn(STATIC_TOKEN);

        TokenColecticaAuthenticator authenticator = new TokenColecticaAuthenticator(colecticaConfiguration);

        // When & Then - String
        String stringResult = authenticator.executeWithAuth(token -> "string result");
        assertEquals("string result", stringResult);

        // When & Then - Integer
        Integer intResult = authenticator.executeWithAuth(token -> 123);
        assertEquals(123, intResult);

        // When & Then - Boolean
        Boolean boolResult = authenticator.executeWithAuth(token -> true);
        assertTrue(boolResult);

        // When & Then - Custom object
        record TestObject(String name, int value) {}
        TestObject objectResult = authenticator.executeWithAuth(token -> new TestObject("test", 42));
        assertEquals("test", objectResult.name());
        assertEquals(42, objectResult.value());
    }
}