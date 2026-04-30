package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordColecticaAuthenticatorTest {

    @Mock
    private RestClient restClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodyUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    private PasswordColecticaAuthenticator authenticator;

    private static final String BASE_SERVER_URL = "http://localhost:8082";
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final String TOKEN_URL = BASE_SERVER_URL + "/token/createtoken";

    @BeforeEach
    void setUp() {
        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.baseServerUrl()).thenReturn(BASE_SERVER_URL);
        when(instanceConfiguration.username()).thenReturn(USERNAME);
        when(instanceConfiguration.password()).thenReturn(PASSWORD);

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);

        authenticator = new PasswordColecticaAuthenticator(restClient, colecticaConfiguration);
    }

    @Test
    void shouldAuthenticateAndExecuteApiCall() {
        String accessToken = "test-token-123";
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);
        when(responseSpec.body(eq(AuthenticationResponse.class))).thenReturn(authResponse);

        String result = authenticator.executeWithAuth(token -> {
            assertEquals(accessToken, token);
            return "success";
        });

        assertEquals("success", result);
        verify(requestSpec).uri(eq(TOKEN_URL));
    }

    @Test
    void shouldAuthenticateWithCorrectCredentials() {
        AuthenticationResponse authResponse = new AuthenticationResponse("test-token-123");
        when(responseSpec.body(eq(AuthenticationResponse.class))).thenReturn(authResponse);

        authenticator.executeWithAuth(token -> "result");

        verify(requestSpec).contentType(MediaType.APPLICATION_JSON);
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(requestSpec).body(bodyCaptor.capture());
        AuthenticationRequest capturedRequest = (AuthenticationRequest) bodyCaptor.getValue();
        assertNotNull(capturedRequest);
        assertEquals(USERNAME, capturedRequest.username());
        assertEquals(PASSWORD, capturedRequest.password());
    }

    @Test
    void shouldCacheAuthenticationToken() {
        AuthenticationResponse authResponse = new AuthenticationResponse("test-token-123");
        when(responseSpec.body(eq(AuthenticationResponse.class))).thenReturn(authResponse);

        authenticator.executeWithAuth(token -> "result1");
        authenticator.executeWithAuth(token -> "result2");

        verify(restClient, times(1)).post();
    }

    @Test
    void shouldRetryWithNewTokenOn401Error() {
        String firstToken = "expired-token";
        String newToken = "new-token-123";
        when(responseSpec.body(eq(AuthenticationResponse.class)))
                .thenReturn(new AuthenticationResponse(firstToken))
                .thenReturn(new AuthenticationResponse(newToken));

        AtomicInteger callCount = new AtomicInteger(0);

        String result = authenticator.executeWithAuth(token -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            }
            return "success with " + token;
        });

        assertEquals("success with " + newToken, result);
        verify(restClient, times(2)).post();
    }

    @Test
    void shouldRetryWithNewTokenOn403Error() {
        String firstToken = "forbidden-token";
        String newToken = "new-token-456";
        when(responseSpec.body(eq(AuthenticationResponse.class)))
                .thenReturn(new AuthenticationResponse(firstToken))
                .thenReturn(new AuthenticationResponse(newToken));

        AtomicInteger callCount = new AtomicInteger(0);

        String result = authenticator.executeWithAuth(token -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
            }
            return "success with " + token;
        });

        assertEquals("success with " + newToken, result);
        verify(restClient, times(2)).post();
    }

    @Test
    void shouldNotRetryOnNon401Or403Error() {
        AuthenticationResponse authResponse = new AuthenticationResponse("valid-token");
        when(responseSpec.body(eq(AuthenticationResponse.class))).thenReturn(authResponse);

        assertThrows(HttpClientErrorException.class, () -> {
            authenticator.executeWithAuth(token -> {
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            });
        });

        verify(restClient, times(1)).post();
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationReturnsNull() {
        when(responseSpec.body(eq(AuthenticationResponse.class))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticator.executeWithAuth(token -> "result");
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccessTokenIsNull() {
        when(responseSpec.body(eq(AuthenticationResponse.class))).thenReturn(new AuthenticationResponse(null));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticator.executeWithAuth(token -> "result");
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldInvalidateCacheAfter401AndUseNewToken() {
        String expiredToken = "expired-token";
        String newToken = "new-token";
        when(responseSpec.body(eq(AuthenticationResponse.class)))
                .thenReturn(new AuthenticationResponse(expiredToken))
                .thenReturn(new AuthenticationResponse(newToken));

        AtomicInteger callCount = new AtomicInteger(0);

        authenticator.executeWithAuth(token -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            }
            return "success";
        });

        callCount.set(0);

        String result = authenticator.executeWithAuth(token -> {
            assertEquals(newToken, token);
            return "success with new token";
        });

        assertEquals("success with new token", result);
        verify(restClient, times(2)).post();
    }
}
