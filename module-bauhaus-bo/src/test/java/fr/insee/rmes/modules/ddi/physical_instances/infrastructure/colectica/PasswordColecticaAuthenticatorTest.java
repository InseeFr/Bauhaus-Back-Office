package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordColecticaAuthenticatorTest {

    @Mock
    private RestTemplate restTemplate;

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

        authenticator = new PasswordColecticaAuthenticator(restTemplate, colecticaConfiguration);
    }

    @Test
    void shouldAuthenticateAndExecuteApiCall() {
        // Given
        String accessToken = "test-token-123";
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // When
        String result = authenticator.executeWithAuth(token -> {
            assertEquals(accessToken, token);
            return "success";
        });

        // Then
        assertEquals("success", result);
        verify(restTemplate).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class));
    }

    @Test
    void shouldAuthenticateWithCorrectCredentials() {
        // Given
        String accessToken = "test-token-123";
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // When
        authenticator.executeWithAuth(token -> "result");

        // Then
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(TOKEN_URL), entityCaptor.capture(), eq(AuthenticationResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());

        AuthenticationRequest authRequest = (AuthenticationRequest) capturedEntity.getBody();
        assertNotNull(authRequest);
        assertEquals(USERNAME, authRequest.username());
        assertEquals(PASSWORD, authRequest.password());
    }

    @Test
    void shouldCacheAuthenticationToken() {
        // Given
        String accessToken = "test-token-123";
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // When - Call twice
        authenticator.executeWithAuth(token -> "result1");
        authenticator.executeWithAuth(token -> "result2");

        // Then - Authentication should only be called once (token is cached)
        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class));
    }

    @Test
    void shouldRetryWithNewTokenOn401Error() {
        // Given
        String firstToken = "expired-token";
        String newToken = "new-token-123";
        AuthenticationResponse firstAuthResponse = new AuthenticationResponse(firstToken);
        AuthenticationResponse newAuthResponse = new AuthenticationResponse(newToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(firstAuthResponse)
                .thenReturn(newAuthResponse);

        AtomicInteger callCount = new AtomicInteger(0);

        // When
        String result = authenticator.executeWithAuth(token -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            }
            return "success with " + token;
        });

        // Then
        assertEquals("success with " + newToken, result);
        verify(restTemplate, times(2)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class));
    }

    @Test
    void shouldRetryWithNewTokenOn403Error() {
        // Given
        String firstToken = "forbidden-token";
        String newToken = "new-token-456";
        AuthenticationResponse firstAuthResponse = new AuthenticationResponse(firstToken);
        AuthenticationResponse newAuthResponse = new AuthenticationResponse(newToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(firstAuthResponse)
                .thenReturn(newAuthResponse);

        AtomicInteger callCount = new AtomicInteger(0);

        // When
        String result = authenticator.executeWithAuth(token -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
            }
            return "success with " + token;
        });

        // Then
        assertEquals("success with " + newToken, result);
        verify(restTemplate, times(2)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class));
    }

    @Test
    void shouldNotRetryOnNon401Or403Error() {
        // Given
        String accessToken = "valid-token";
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> {
            authenticator.executeWithAuth(token -> {
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
            });
        });

        // Authentication should only be called once (no retry for 500 errors)
        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class));
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationReturnsNull() {
        // Given
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticator.executeWithAuth(token -> "result");
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccessTokenIsNull() {
        // Given
        AuthenticationResponse authResponse = new AuthenticationResponse(null);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticator.executeWithAuth(token -> "result");
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldInvalidateCacheAfter401AndUseNewToken() {
        // Given
        String expiredToken = "expired-token";
        String newToken = "new-token";
        AuthenticationResponse expiredAuthResponse = new AuthenticationResponse(expiredToken);
        AuthenticationResponse newAuthResponse = new AuthenticationResponse(newToken);

        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(expiredAuthResponse)
                .thenReturn(newAuthResponse);

        AtomicInteger callCount = new AtomicInteger(0);

        // When - First call fails with 401
        authenticator.executeWithAuth(token -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            }
            return "success";
        });

        // Reset call count for second test
        callCount.set(0);

        // Second call should use the new cached token
        String result = authenticator.executeWithAuth(token -> {
            assertEquals(newToken, token);
            return "success with new token";
        });

        // Then
        assertEquals("success with new token", result);
        // Authentication should have been called twice total (initial + after 401)
        verify(restTemplate, times(2)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(AuthenticationResponse.class));
    }
}