package fr.insee.rmes.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.colectica.dto.AuthenticationRequest;
import fr.insee.rmes.colectica.dto.AuthenticationResponse;
import fr.insee.rmes.colectica.dto.ColecticaItem;
import fr.insee.rmes.colectica.dto.ColecticaResponse;
import fr.insee.rmes.colectica.dto.QueryRequest;
import fr.insee.rmes.domain.model.ddi.Ddi4Response;
import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDIRepositoryImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    @Mock
    private ObjectMapper objectMapper;

    private DDIRepositoryImpl ddiRepository;

    @BeforeEach
    void setUp() {
        ddiRepository = new DDIRepositoryImpl(restTemplate, colecticaConfiguration, objectMapper);
    }

    @Test
    void shouldGetPhysicalInstancesWithAuthentication() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String accessToken = "test-token-123";
        String tokenUrl = baseServerUrl + "/token/createtoken";
        String queryUrl = baseApiUrl + "_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");

        // Mock authentication response
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);

        ColecticaItem item1 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Instance Physique 1", "en", "Physical Instance 1"), // itemName
            Map.of("fr-FR", "Label 1", "en", "Label 1"), // label
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "PhysicalInstance", // itemType
            "agency1", // agencyId
            1, // version
            "pi-1", // identifier
            null, // item
            null, // notes
            "2025-01-01T00:00:00", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            1L, // transactionId
            0 // versionCreationType
        );

        ColecticaItem item2 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Instance Physique 2", "en", "Physical Instance 2"), // itemName
            Map.of("fr-FR", "Label 2", "en", "Label 2"), // label
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "PhysicalInstance", // itemType
            "agency2", // agencyId
            1, // version
            "pi-2", // identifier
            null, // item
            null, // notes
            null, // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            2L, // transactionId
            0 // versionCreationType
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(item1, item2));

        // Mock configuration
        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);

        // Mock authentication call
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // Mock query call
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstances();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Instance Physique 1", result.get(0).label());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2025-01-01 00:00:00", sdf.format(result.get(0).versionDate()));
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Instance Physique 2", result.get(1).label());
        assertNull(result.get(1).versionDate());

        // Verify authentication was called
        verify(restTemplate).postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class));

        // Verify query was called with Bearer token
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(queryUrl), entityCaptor.capture(), eq(ColecticaResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertTrue(headers.getFirst(HttpHeaders.AUTHORIZATION).startsWith("Bearer "));
    }

    @Test
    void shouldAuthenticateWithCorrectHeaders() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String accessToken = "test-token-123";
        String tokenUrl = baseServerUrl + "/token/createtoken";
        String queryUrl = baseApiUrl + "_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");

        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);
        ColecticaResponse mockResponse = new ColecticaResponse(List.of());

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        ddiRepository.getPhysicalInstances();

        // Then - Verify authentication request has correct headers and body
        ArgumentCaptor<HttpEntity> authEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(tokenUrl), authEntityCaptor.capture(), eq(AuthenticationResponse.class));

        HttpEntity<?> authEntity = authEntityCaptor.getValue();
        HttpHeaders authHeaders = authEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, authHeaders.getContentType());

        // Verify the body contains correct credentials
        AuthenticationRequest authRequest = (AuthenticationRequest) authEntity.getBody();
        assertNotNull(authRequest);
        assertEquals(username, authRequest.username());
        assertEquals(password, authRequest.password());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFails() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String username = "test-user";
        String password = "test-password";
        String tokenUrl = baseServerUrl + "/token/createtoken";

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ddiRepository.getPhysicalInstances();
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccessTokenIsNull() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String username = "test-user";
        String password = "test-password";
        String tokenUrl = baseServerUrl + "/token/createtoken";

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(new AuthenticationResponse(null));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ddiRepository.getPhysicalInstances();
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldGetPhysicalInstanceById() throws Exception {
        // Given
        String instanceId = "pi-123";

        // Mock the ObjectMapper to return a simple Ddi4Response without loading the actual file
        Ddi4Response mockResponse = new Ddi4Response(
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );

        when(objectMapper.readValue(any(String.class), eq(Ddi4Response.class)))
                .thenReturn(mockResponse);

        // When
        Ddi4Response result = ddiRepository.getPhysicalInstance(instanceId);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertNotNull(result.dataRelationship());

        // Verify ObjectMapper was called
        verify(objectMapper).readValue(any(String.class), eq(Ddi4Response.class));
    }

    @Test
    void shouldCreateAuthenticationRequestWithUsernameAndPassword() {
        // Given
        String username = "test-user";
        String password = "test-password";

        // When
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        // Then
        assertEquals(username, request.username());
        assertEquals(password, request.password());
    }

    @Test
    void shouldCreateAuthenticationResponseWithAccessToken() {
        // Given
        String accessToken = "test-token-123";

        // When
        AuthenticationResponse response = new AuthenticationResponse(accessToken);

        // Then
        assertEquals(accessToken, response.accessToken());
    }

    @Test
    void shouldCacheAuthenticationToken() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String accessToken = "test-token-123";
        String tokenUrl = baseServerUrl + "/token/createtoken";
        String queryUrl = baseApiUrl + "_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");

        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);
        ColecticaResponse mockResponse = new ColecticaResponse(List.of());

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When - Call twice
        ddiRepository.getPhysicalInstances();
        ddiRepository.getPhysicalInstances();

        // Then - Authentication should only be called once (token is cached)
        verify(restTemplate, times(1)).postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class));
        // Query should be called twice
        verify(restTemplate, times(2)).postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class));
    }

    @Test
    void shouldRetryWithNewTokenOn401Error() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String firstToken = "expired-token";
        String newToken = "new-token-123";
        String tokenUrl = baseServerUrl + "/token/createtoken";
        String queryUrl = baseApiUrl + "_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");

        AuthenticationResponse firstAuthResponse = new AuthenticationResponse(firstToken);
        AuthenticationResponse newAuthResponse = new AuthenticationResponse(newToken);
        ColecticaResponse mockResponse = new ColecticaResponse(List.of());

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);

        // First auth returns first token, second auth returns new token
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(firstAuthResponse)
                .thenReturn(newAuthResponse);

        // First query call throws 401, second succeeds
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED))
                .thenReturn(mockResponse);

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstances();

        // Then
        assertNotNull(result);
        // Authentication should be called twice (once initially, once after 401)
        verify(restTemplate, times(2)).postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class));
        // Query should be called twice (once with expired token, once with new token)
        verify(restTemplate, times(2)).postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class));
    }

    @Test
    void shouldRetryWithNewTokenOn403Error() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String firstToken = "forbidden-token";
        String newToken = "new-token-456";
        String tokenUrl = baseServerUrl + "/token/createtoken";
        String queryUrl = baseApiUrl + "_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");

        AuthenticationResponse firstAuthResponse = new AuthenticationResponse(firstToken);
        AuthenticationResponse newAuthResponse = new AuthenticationResponse(newToken);
        ColecticaResponse mockResponse = new ColecticaResponse(List.of());

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);

        // First auth returns first token, second auth returns new token
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(firstAuthResponse)
                .thenReturn(newAuthResponse);

        // First query call throws 403, second succeeds
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
                .thenReturn(mockResponse);

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstances();

        // Then
        assertNotNull(result);
        // Authentication should be called twice (once initially, once after 403)
        verify(restTemplate, times(2)).postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class));
        // Query should be called twice (once with forbidden token, once with new token)
        verify(restTemplate, times(2)).postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class));
    }

    @Test
    void shouldNotRetryOnNon401Or403Error() {
        // Given
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String accessToken = "valid-token";
        String tokenUrl = baseServerUrl + "/token/createtoken";
        String queryUrl = baseApiUrl + "_query";
        List<String> itemTypes = List.of("a51e85bb-6259-4488-8df2-f08cb43485f8");

        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);

        when(colecticaConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(colecticaConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(colecticaConfiguration.username()).thenReturn(username);
        when(colecticaConfiguration.password()).thenReturn(password);
        when(colecticaConfiguration.itemTypes()).thenReturn(itemTypes);
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // Query throws 500 Internal Server Error
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> {
            ddiRepository.getPhysicalInstances();
        });

        // Authentication should only be called once (no retry for 500 errors)
        verify(restTemplate, times(1)).postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class));
        // Query should only be called once (no retry)
        verify(restTemplate, times(1)).postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class));
    }
}