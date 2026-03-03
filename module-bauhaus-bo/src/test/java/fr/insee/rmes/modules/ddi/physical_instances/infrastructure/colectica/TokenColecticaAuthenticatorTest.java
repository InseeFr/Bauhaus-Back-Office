package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.keycloak.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

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

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authenticator.executeWithAuth(_ -> {
            throw new RuntimeException("API call failed");
        }));

        assertEquals("API call failed", exception.getMessage());
    }

    record TestObject(String name, int value) {}

    static Stream<Arguments> returnTypeTestCases() {
        return Stream.of(
            Arguments.of("Integer return", (Function<String, Object>) _ -> 42, 42),
            Arguments.of("String return", (Function<String, Object>) _ -> "string result", "string result"),
            Arguments.of("Null return", (Function<String, Object>) _ -> null, null),
            Arguments.of("Boolean return", (Function<String, Object>) _ -> true, true),
            Arguments.of("Object return", (Function<String, Object>) _ -> new TestObject("test", 42), new TestObject("test", 42))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("returnTypeTestCases")
    void shouldHandleDifferentReturnTypes(String testName, Function<String, Object> apiCall, Object expectedResult) {
        when(tokenService.getAccessToken()).thenReturn(KEYCLOAK_TOKEN);

        Object result = authenticator.executeWithAuth(apiCall);

        assertEquals(expectedResult, result);
    }
}
