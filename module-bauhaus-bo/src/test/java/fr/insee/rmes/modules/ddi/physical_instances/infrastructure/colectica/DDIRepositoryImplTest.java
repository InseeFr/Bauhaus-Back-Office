package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
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
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DDI3toDDI4ConverterService ddi3ToDdi4Converter;

    private DDIRepositoryImpl ddiRepository;

    @BeforeEach
    void setUp() {
        ddiRepository = new DDIRepositoryImpl(restTemplate, instanceConfiguration, objectMapper, ddi3ToDdi4Converter);
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
            Map.of("fr-FR", "Label 1", "en", "Label 1"), // value
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
            Map.of("fr-FR", "Label 2", "en", "Label 2"), // value
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
        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);

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
        assertEquals("agency1", result.get(0).agency());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2025-01-01 00:00:00", sdf.format(result.get(0).versionDate()));
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Instance Physique 2", result.get(1).label());
        assertEquals("agency2", result.get(1).agency());
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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(restTemplate.postForObject(eq(tokenUrl), any(HttpEntity.class), eq(AuthenticationResponse.class)))
                .thenReturn(new AuthenticationResponse(null));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ddiRepository.getPhysicalInstances();
        });

        assertEquals("Authentication failed: unable to retrieve access token", exception.getMessage());
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        // Given
        String instanceId = "2514afe4-7b08-4500-be25-7a852a10fd8c";
        String baseServerUrl = "http://localhost:8082";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String username = "test-user";
        String password = "test-password";
        String accessToken = "test-token-123";
        String agencyId = "fr.inserm.constances";
        int version = 1;

        // Mock configuration
        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);

        // Mock authentication
        AuthenticationResponse authResponse = new AuthenticationResponse(accessToken);
        when(restTemplate.postForObject(
                eq(baseServerUrl + "/token/createtoken"),
                any(HttpEntity.class),
                eq(AuthenticationResponse.class)))
                .thenReturn(authResponse);

        // Mock DDI set response with complete FragmentInstance XML (including Variables)
        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n" +
                "    <ddi:TopLevelReference>\n" +
                "        <r:Agency>fr.inserm.constances</r:Agency>\n" +
                "        <r:ID>2514afe4-7b08-4500-be25-7a852a10fd8c</r:ID>\n" +
                "        <r:Version>1</r:Version>\n" +
                "        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>\n" +
                "    </ddi:TopLevelReference>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <PhysicalInstance isUniversallyUnique=\"true\" versionDate=\"2025-10-23T12:28:43.615878Z\" xmlns=\"ddi:physicalinstance:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.inserm.constances:2514afe4-7b08-4500-be25-7a852a10fd8c:1</r:URN>\n" +
                "            <r:Agency>fr.inserm.constances</r:Agency>\n" +
                "            <r:ID>2514afe4-7b08-4500-be25-7a852a10fd8c</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">Radon et gamma</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "            <r:DataRelationshipReference>\n" +
                "                <r:Agency>fr.inserm.constances</r:Agency>\n" +
                "                <r:ID>dr-123</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <r:TypeOfObject>DataRelationship</r:TypeOfObject>\n" +
                "            </r:DataRelationshipReference>\n" +
                "        </PhysicalInstance>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <Variable isUniversallyUnique=\"true\" versionDate=\"2025-10-23T12:28:43.608412Z\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.inserm.constances:var-1:1</r:URN>\n" +
                "            <r:Agency>fr.inserm.constances</r:Agency>\n" +
                "            <r:ID>var-1</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <VariableName>\n" +
                "                <r:String xml:lang=\"fr\">TEST_VAR</r:String>\n" +
                "            </VariableName>\n" +
                "            <VariableRepresentation>\n" +
                "                <r:TextRepresentation blankIsMissingValue=\"false\" />\n" +
                "            </VariableRepresentation>\n" +
                "        </Variable>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-10-23T12:28:43.608394Z\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.inserm.constances:dr-123:1</r:URN>\n" +
                "            <r:Agency>fr.inserm.constances</r:Agency>\n" +
                "            <r:ID>dr-123</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <DataRelationshipName>\n" +
                "                <r:String xml:lang=\"en-US\">DataRelationshipName</r:String>\n" +
                "            </DataRelationshipName>\n" +
                "        </DataRelationship>\n" +
                "    </Fragment>\n" +
                "</ddi:FragmentInstance>";

        // Mock the direct call to /ddiset/{agencyId}/{identifier}
        when(restTemplate.exchange(
                eq(baseApiUrl + "ddiset/" + agencyId + "/" + instanceId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));

        // Mock DDI3 to DDI4 conversion
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-10-23T12:28:43.615773Z",
                "urn:ddi:fr.inserm.constances:2514afe4-7b08-4500-be25-7a852a10fd8c:1",
                agencyId, instanceId, "1",
                new Citation(new Title(new StringValue("fr-FR", "Radon et gamma"))),
                null
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(), List.of(), List.of(), List.of()
        );

        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // When
        Ddi4Response result = ddiRepository.getPhysicalInstance(agencyId, instanceId);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());
        assertEquals(instanceId, result.physicalInstance().get(0).id());
        assertEquals(agencyId, result.physicalInstance().get(0).agency());
        assertEquals("Radon et gamma", result.physicalInstance().get(0).citation().title().string().text());

        // Verify authentication was called
        verify(restTemplate).postForObject(
                eq(baseServerUrl + "/token/createtoken"),
                any(HttpEntity.class),
                eq(AuthenticationResponse.class));

        // Verify ddiset endpoint was called
        verify(restTemplate).exchange(
                eq(baseApiUrl + "ddiset/" + agencyId + "/" + instanceId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class));

        // Verify converter was called
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0"));
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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);

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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);

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

        when(instanceConfiguration.baseServerUrl()).thenReturn(baseServerUrl);
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.username()).thenReturn(username);
        when(instanceConfiguration.password()).thenReturn(password);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
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