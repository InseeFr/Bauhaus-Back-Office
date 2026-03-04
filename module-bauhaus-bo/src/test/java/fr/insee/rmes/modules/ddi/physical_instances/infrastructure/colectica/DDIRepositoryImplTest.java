package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDIRepositoryImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    @Mock
    private DDI3toDDI4ConverterService ddi3ToDdi4Converter;

    @Mock
    private DDI4toDDI3ConverterService ddi4ToDdi3Converter;

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    @Mock
    private ColecticaAuthenticator authenticator;

    private DDIRepositoryImpl ddiRepository;

    private static final String TEST_TOKEN = "test-token-123";

    @BeforeEach
    void setUp() {
        // By default, mock returns null for codeListDenyList (no filtering)
        // Use lenient() since not all tests use this stubbing
        lenient().when(colecticaConfiguration.codeListDenyList()).thenReturn(null);
        lenient().when(colecticaConfiguration.langs()).thenReturn(List.of("fr-FR"));

        // Configure authenticator to execute the function with a test token
        lenient().when(authenticator.executeWithAuth(any())).thenAnswer(invocation -> {
            java.util.function.Function<String, ?> function = invocation.getArgument(0);
            return function.apply(TEST_TOKEN);
        });

        ddiRepository = new DDIRepositoryImpl(restTemplate, instanceConfiguration, ddi3ToDdi4Converter, ddi4ToDdi3Converter, colecticaConfiguration, authenticator);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";
        Map<String, String> itemTypes = Map.of("PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8");

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

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(item1, item2), 2, 2, null, null, null);

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);

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

        // Verify authenticator was called
        verify(authenticator).executeWithAuth(any());

        // Verify query was called with Bearer token
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(queryUrl), entityCaptor.capture(), eq(ColecticaResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertTrue(headers.getFirst(HttpHeaders.AUTHORIZATION).startsWith("Bearer "));
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        // Given
        String instanceId = "2514afe4-7b08-4500-be25-7a852a10fd8c";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.inserm.constances";
        int version = 1;

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

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
                null,
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
    void shouldGetCodesLists() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";

        ColecticaItem codeList1 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Liste de codes 1", "en", "Code List 1"), // itemName
            Map.of("fr-FR", "LC1", "en", "LC1"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "CodeList", // itemType
            "agency1", // agencyId
            1, // version
            "cl-1", // identifier
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

        ColecticaItem codeList2 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Liste de codes 2", "en", "Code List 2"), // itemName
            Map.of("fr-FR", "LC2", "en", "LC2"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "CodeList", // itemType
            "agency2", // agencyId
            1, // version
            "cl-2", // identifier
            null, // item
            null, // notes
            "2025-01-02T00:00:00", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            2L, // transactionId
            0 // versionCreationType
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(codeList1, codeList2), 2, 2, null, null, null);

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock query call - should query for CodeList itemType (8b108ef8-b642-4484-9c49-f88e4bf7cf1d)
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialCodesList> result = ddiRepository.getCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("cl-1", result.get(0).id());
        assertEquals("Liste de codes 1", result.get(0).label());
        assertEquals("agency1", result.get(0).agency());
        assertNotNull(result.get(0).versionDate());
        assertEquals("cl-2", result.get(1).id());
        assertEquals("Liste de codes 2", result.get(1).label());
        assertEquals("agency2", result.get(1).agency());

        // Verify query was called with CodeList itemType
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(queryUrl), entityCaptor.capture(), eq(ColecticaResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        QueryRequest queryRequest = (QueryRequest) capturedEntity.getBody();
        assertNotNull(queryRequest);
        assertEquals(1, queryRequest.itemTypes().size());
        assertEquals("8b108ef8-b642-4484-9c49-f88e4bf7cf1d", queryRequest.itemTypes().get(0)); // CodeList UUID
    }

    @Test
    void shouldCreatePhysicalInstance() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";

        String physicalInstanceLabel = "Test Physical Instance";
        String dataRelationshipLabel = "Test Data Relationship Label";
        String logicalRecordLabel = "Test LogicalRecord Label";
        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
                physicalInstanceLabel,
                dataRelationshipLabel,
                logicalRecordLabel
        );

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b"
        ));

        // Mock item creation (POST /item)
        when(restTemplate.postForObject(eq(itemUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{}");

        // Mock ddiset response for getPhysicalInstance call after creation
        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <PhysicalInstance isUniversallyUnique=\"true\" xmlns=\"ddi:physicalinstance:3_3\">\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">" + physicalInstanceLabel + "</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "        </PhysicalInstance>\n" +
                "    </Fragment>\n" +
                "</ddi:FragmentInstance>";

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));

        // Mock converter
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:test-id:1",
                "fr.insee", "test-id", "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", physicalInstanceLabel))),
                null
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference("fr.insee", "test-id", "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(), List.of(), List.of(), List.of()
        );

        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // When
        Ddi4Response result = ddiRepository.createPhysicalInstance(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());
        assertEquals(physicalInstanceLabel, result.physicalInstance().get(0).citation().title().string().text());

        // Verify item creation endpoint was called
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(itemUrl), entityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        ColecticaCreateItemRequest createRequest = (ColecticaCreateItemRequest) capturedEntity.getBody();
        assertNotNull(createRequest);
        assertEquals(2, createRequest.items().size()); // PhysicalInstance + DataRelationship

        // Verify first item is PhysicalInstance
        ColecticaItemResponse piItem = createRequest.items().get(0);
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", piItem.itemType()); // PhysicalInstance UUID
        assertTrue(piItem.item().contains(physicalInstanceLabel));

        // Verify second item is DataRelationship
        ColecticaItemResponse drItem = createRequest.items().get(1);
        assertEquals("f39ff278-8500-45fe-a850-3906da2d242b", drItem.itemType()); // DataRelationship UUID
        assertTrue(drItem.item().contains(dataRelationshipLabel));
    }

    @Test
    void shouldUpdatePhysicalInstance() {
        // Given
        String instanceId = "test-pi-id";
        String agencyId = "fr.insee";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";

        String newLabel = "Updated Physical Instance Label";
        String newDataRelationshipLabel = "Updated Data Relationship Label";
        String newLogicalRecordLabel = "Updated LogicalRecord Label";
        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                newLabel,
                newDataRelationshipLabel,
                newLogicalRecordLabel
        );

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock existing instance (for getPhysicalInstance call)
        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <PhysicalInstance isUniversallyUnique=\"true\" xmlns=\"ddi:physicalinstance:3_3\">\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>" + instanceId + "</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">Old Label</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "            <r:DataRelationshipReference>\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>dr-123</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "            </r:DataRelationshipReference>\n" +
                "        </PhysicalInstance>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <DataRelationship isUniversallyUnique=\"true\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>dr-123</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <DataRelationshipName>\n" +
                "                <r:String xml:lang=\"en-US\">Old DR Name</r:String>\n" +
                "            </DataRelationshipName>\n" +
                "            <LogicalRecord isUniversallyUnique=\"true\">\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>lr-123</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <LogicalRecordName>\n" +
                "                    <r:String xml:lang=\"fr\">Old LR Name</r:String>\n" +
                "                </LogicalRecordName>\n" +
                "            </LogicalRecord>\n" +
                "        </DataRelationship>\n" +
                "    </Fragment>\n" +
                "</ddi:FragmentInstance>";

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));

        // Mock converter
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Old Label"))),
                new DataRelationshipReference(agencyId, "dr-123", "1", "DataRelationship")
        );

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                new DataRelationshipName(new StringValue("en-US", "Old DR Name")),
                null,
                new LogicalRecord("true", "urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        new LogicalRecordName(new StringValue("fr", "Old LR Name")), null, null)
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // Mock DDI4 to DDI3 conversion for updateFullPhysicalInstance
        Ddi3Response.Ddi3Item mockPiDdi3Item = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "2", instanceId,
                "<PhysicalInstance>" + newLabel + "</PhysicalInstance>",
                "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        Ddi3Response.Ddi3Item mockDrDdi3Item = new Ddi3Response.Ddi3Item(
                "f39ff278-8500-45fe-a850-3906da2d242b", agencyId, "2", "dr-123",
                "<DataRelationship>" + newDataRelationshipLabel + "</DataRelationship>",
                "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        Ddi3Response mockDdi3Response = new Ddi3Response(null, List.of(mockPiDdi3Item, mockDrDdi3Item));
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(any(Ddi4Response.class)))
                .thenReturn(mockDdi3Response);

        // Mock item update (POST /item)
        when(restTemplate.postForObject(eq(itemUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then
        // Verify item update endpoint was called
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(itemUrl), entityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        ColecticaCreateItemRequest createRequest = (ColecticaCreateItemRequest) capturedEntity.getBody();
        assertNotNull(createRequest);
        assertEquals(2, createRequest.items().size()); // PhysicalInstance + DataRelationship

        // Verify first item is PhysicalInstance with updated label
        ColecticaItemResponse piItem = createRequest.items().get(0);
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", piItem.itemType()); // PhysicalInstance UUID
        assertEquals(2, piItem.version()); // Version incremented
        assertTrue(piItem.item().contains(newLabel));

        // Verify second item is DataRelationship with updated name
        ColecticaItemResponse drItem = createRequest.items().get(1);
        assertEquals("f39ff278-8500-45fe-a850-3906da2d242b", drItem.itemType()); // DataRelationship UUID
        assertEquals(2, drItem.version()); // Version incremented
        assertTrue(drItem.item().contains(newDataRelationshipLabel));
    }

    @Test
    void shouldFilterCodeListsInDenyList() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";

        // Create code lists - one should be filtered, one should pass
        ColecticaItem codeListToFilter = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Liste des statuts professionnels", "en", "Professional Status List"), // itemName
            Map.of("fr-FR", "Statuts", "en", "Status"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "CodeList", // itemType
            "fr.insee", // agencyId
            1, // version
            "2a22ba00-a977-4a61-a582-99025c6b0582", // identifier - IN DENY LIST
            null, // item
            null, // notes
            "2023-07-04T08:19:29", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            1L, // transactionId
            0 // versionCreationType
        );

        ColecticaItem codeListToKeep = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Liste de codes à garder", "en", "Code List to Keep"), // itemName
            Map.of("fr-FR", "À garder", "en", "To Keep"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "CodeList", // itemType
            "fr.insee", // agencyId
            1, // version
            "other-id-to-keep", // identifier - NOT IN DENY LIST
            null, // item
            null, // notes
            "2023-07-04T08:19:29", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            2L, // transactionId
            0 // versionCreationType
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(codeListToFilter, codeListToKeep), 2, 2, null, null, null);

        // Configure deny list
        List<ColecticaConfiguration.CodeListDenyEntry> denyList = List.of(
            new ColecticaConfiguration.CodeListDenyEntry("fr.insee", "2a22ba00-a977-4a61-a582-99025c6b0582")
        );
        when(colecticaConfiguration.codeListDenyList()).thenReturn(denyList);

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock query call
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialCodesList> result = ddiRepository.getCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one code list should remain
        assertEquals("other-id-to-keep", result.get(0).id());
        assertEquals("Liste de codes à garder", result.get(0).label());
        assertEquals("fr.insee", result.get(0).agency());

        // Verify the filtered code list is not in the results
        assertFalse(result.stream().anyMatch(cl -> cl.id().equals("2a22ba00-a977-4a61-a582-99025c6b0582")));
    }

    @Test
    void shouldNotFilterWhenDenyListIsEmpty() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";

        ColecticaItem codeList = new ColecticaItem(
            null, Map.of("fr-FR", "Liste de codes"), Map.of("fr-FR", "LC"),
            null, null, 0, "test-repo", true, List.of(), "CodeList",
            "fr.insee", 1, "some-id", null, null, "2023-07-04T08:19:29",
            null, true, false, false, "DDI", 1L, 0
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(codeList), 1, 1, null, null, null);

        // Configure empty deny list
        when(colecticaConfiguration.codeListDenyList()).thenReturn(List.of());

        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialCodesList> result = ddiRepository.getCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Code list should not be filtered
        assertEquals("some-id", result.get(0).id());
    }

    @Test
    void shouldNotFilterWhenDenyListIsNull() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";

        ColecticaItem codeList = new ColecticaItem(
            null, Map.of("fr-FR", "Liste de codes"), Map.of("fr-FR", "LC"),
            null, null, 0, "test-repo", true, List.of(), "CodeList",
            "fr.insee", 1, "some-id", null, null, "2023-07-04T08:19:29",
            null, true, false, false, "DDI", 1L, 0
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(codeList), 1, 1, null, null, null);

        // Configure null deny list (default behavior)
        when(colecticaConfiguration.codeListDenyList()).thenReturn(null);

        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialCodesList> result = ddiRepository.getCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Code list should not be filtered
        assertEquals("some-id", result.get(0).id());
    }

    @Test
    void shouldFilterMultipleCodeListsInDenyList() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";

        ColecticaItem codeList1 = new ColecticaItem(
            null, Map.of("fr-FR", "Code List 1"), Map.of("fr-FR", "CL1"),
            null, null, 0, "test-repo", true, List.of(), "CodeList",
            "fr.insee", 1, "id-to-filter-1", null, null, "2023-07-04T08:19:29",
            null, true, false, false, "DDI", 1L, 0
        );

        ColecticaItem codeList2 = new ColecticaItem(
            null, Map.of("fr-FR", "Code List 2"), Map.of("fr-FR", "CL2"),
            null, null, 0, "test-repo", true, List.of(), "CodeList",
            "fr.insee", 1, "id-to-keep", null, null, "2023-07-04T08:19:29",
            null, true, false, false, "DDI", 2L, 0
        );

        ColecticaItem codeList3 = new ColecticaItem(
            null, Map.of("fr-FR", "Code List 3"), Map.of("fr-FR", "CL3"),
            null, null, 0, "test-repo", true, List.of(), "CodeList",
            "other.agency", 1, "id-to-filter-2", null, null, "2023-07-04T08:19:29",
            null, true, false, false, "DDI", 3L, 0
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(codeList1, codeList2, codeList3), 3, 3, null, null, null);

        // Configure deny list with multiple entries
        List<ColecticaConfiguration.CodeListDenyEntry> denyList = List.of(
            new ColecticaConfiguration.CodeListDenyEntry("fr.insee", "id-to-filter-1"),
            new ColecticaConfiguration.CodeListDenyEntry("other.agency", "id-to-filter-2")
        );
        when(colecticaConfiguration.codeListDenyList()).thenReturn(denyList);

        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialCodesList> result = ddiRepository.getCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one code list should remain
        assertEquals("id-to-keep", result.get(0).id());
        assertEquals("Code List 2", result.get(0).label());
    }

    @Test
    void shouldGetPhysicalInstanceWithCodeListsAndCategories() {
        // Given
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.insee";

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b"
        ));

        // Mock DDI set response with complete FragmentInstance XML (including Variables, CodeLists, Categories)
        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n" +
                "    <ddi:TopLevelReference>\n" +
                "        <r:Agency>fr.insee</r:Agency>\n" +
                "        <r:ID>32799021-0663-41cd-aca6-3ad8dbdae3e3</r:ID>\n" +
                "        <r:Version>1</r:Version>\n" +
                "        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>\n" +
                "    </ddi:TopLevelReference>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <PhysicalInstance isUniversallyUnique=\"true\" versionDate=\"2025-12-10T11:55:14.251595Z\" xmlns=\"ddi:physicalinstance:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:32799021-0663-41cd-aca6-3ad8dbdae3e3:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>32799021-0663-41cd-aca6-3ad8dbdae3e3</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">test</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "            <r:DataRelationshipReference>\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>795aa4b8-acec-4ef8-8f08-3a200c7bdb10</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <r:TypeOfObject>DataRelationship</r:TypeOfObject>\n" +
                "            </r:DataRelationshipReference>\n" +
                "        </PhysicalInstance>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <Variable isUniversallyUnique=\"true\" versionDate=\"2025-12-10T11:55:33.138Z\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <VariableName>\n" +
                "                <r:String xml:lang=\"fr-FR\">name</r:String>\n" +
                "            </VariableName>\n" +
                "            <r:Label>\n" +
                "                <r:Content xml:lang=\"fr-FR\">Test Label</r:Content>\n" +
                "            </r:Label>\n" +
                "            <VariableRepresentation>\n" +
                "                <r:CodeRepresentation blankIsMissingValue=\"false\">\n" +
                "                    <r:CodeListReference>\n" +
                "                        <r:Agency>fr.insee</r:Agency>\n" +
                "                        <r:ID>2f70f505-4a9e-4abe-82d4-c4ddfed25d52</r:ID>\n" +
                "                        <r:Version>1</r:Version>\n" +
                "                        <r:TypeOfObject>CodeList</r:TypeOfObject>\n" +
                "                    </r:CodeListReference>\n" +
                "                </r:CodeRepresentation>\n" +
                "            </VariableRepresentation>\n" +
                "        </Variable>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <DataRelationship isUniversallyUnique=\"true\" versionDate=\"2025-12-10T11:55:14.251595Z\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:795aa4b8-acec-4ef8-8f08-3a200c7bdb10:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>795aa4b8-acec-4ef8-8f08-3a200c7bdb10</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <DataRelationshipName>\n" +
                "                <r:String xml:lang=\"en-US\">DataRelationShip Name:test</r:String>\n" +
                "            </DataRelationshipName>\n" +
                "            <LogicalRecord isUniversallyUnique=\"true\">\n" +
                "                <r:URN>urn:ddi:fr.insee:8585972f-2dc2-4125-87b2-60fd3f243cf3:1</r:URN>\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>8585972f-2dc2-4125-87b2-60fd3f243cf3</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <LogicalRecordName>\n" +
                "                    <r:String xml:lang=\"fr\">test</r:String>\n" +
                "                </LogicalRecordName>\n" +
                "                <VariablesInRecord>\n" +
                "                    <VariableUsedReference>\n" +
                "                        <r:Agency>fr.insee</r:Agency>\n" +
                "                        <r:ID>2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d</r:ID>\n" +
                "                        <r:Version>1</r:Version>\n" +
                "                        <r:TypeOfObject>Variable</r:TypeOfObject>\n" +
                "                    </VariableUsedReference>\n" +
                "                </VariablesInRecord>\n" +
                "            </LogicalRecord>\n" +
                "        </DataRelationship>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <CodeList isUniversallyUnique=\"true\" versionDate=\"2025-12-10T11:55:28.140Z\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:82466a9c-5266-434b-9dd3-329993717ad4:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>2f70f505-4a9e-4abe-82d4-c4ddfed25d52</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Label>\n" +
                "                <r:Content xml:lang=\"fr-FR\">cl</r:Content>\n" +
                "            </r:Label>\n" +
                "            <Code isUniversallyUnique=\"true\">\n" +
                "                <r:URN>urn:ddi:fr.insee:6a290143-b9f6-43d3-92ac-70c3b2f516c1:1</r:URN>\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>6a290143-b9f6-43d3-92ac-70c3b2f516c1</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <r:CategoryReference>\n" +
                "                    <r:Agency>fr.insee</r:Agency>\n" +
                "                    <r:ID>d363a730-14d4-4c54-9464-982312cf9330</r:ID>\n" +
                "                    <r:Version>1</r:Version>\n" +
                "                    <r:TypeOfObject>Category</r:TypeOfObject>\n" +
                "                </r:CategoryReference>\n" +
                "                <r:Value>a</r:Value>\n" +
                "            </Code>\n" +
                "        </CodeList>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <Category isUniversallyUnique=\"true\" versionDate=\"2025-12-10T11:55:28.140Z\" isMissing=\"false\" xmlns=\"ddi:logicalproduct:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:d363a730-14d4-4c54-9464-982312cf9330:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>d363a730-14d4-4c54-9464-982312cf9330</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Label>\n" +
                "                <r:Content xml:lang=\"fr-FR\">aq</r:Content>\n" +
                "            </r:Label>\n" +
                "        </Category>\n" +
                "    </Fragment>\n" +
                "</ddi:FragmentInstance>";

        // Mock the direct call to /ddiset/{agencyId}/{identifier}
        when(restTemplate.exchange(
                eq(baseApiUrl + "ddiset/" + agencyId + "/" + instanceId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));

        // Mock DDI3 to DDI4 conversion with CodeLists and Categories
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-12-10T11:55:14.251595Z",
                "urn:ddi:fr.insee:32799021-0663-41cd-aca6-3ad8dbdae3e3:1",
                agencyId, instanceId, "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "test"))),
                new DataRelationshipReference(agencyId, "795aa4b8-acec-4ef8-8f08-3a200c7bdb10", "1", "DataRelationship")
        );

        Ddi4Variable mockVariable = new Ddi4Variable(
                "true",
                "2025-12-10T11:55:33.138Z",
                "urn:ddi:fr.insee:2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d:1",
                agencyId,
                "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d",
                "1",
                null,
                new VariableName(new StringValue("fr-FR", "name")),
                new Label(new Content("fr-FR", "Test Label")),
                null,
                new VariableRepresentation(null,
                    new CodeRepresentation("false",
                        new CodeListReference(agencyId, "2f70f505-4a9e-4abe-82d4-c4ddfed25d52", "1", "CodeList")),
                    null, null, null),
                ""
        );

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(
                "true", "2025-12-10T11:55:14.251595Z",
                "urn:ddi:fr.insee:795aa4b8-acec-4ef8-8f08-3a200c7bdb10:1",
                agencyId, "795aa4b8-acec-4ef8-8f08-3a200c7bdb10", "1",
                null,
                new DataRelationshipName(new StringValue("en-US", "DataRelationShip Name:test")),
                null,
                new LogicalRecord("true", "urn:ddi:fr.insee:8585972f-2dc2-4125-87b2-60fd3f243cf3:1",
                        agencyId, "8585972f-2dc2-4125-87b2-60fd3f243cf3", "1",
                        new LogicalRecordName(new StringValue("fr", "test")),
                        null,
                        new VariablesInRecord(List.of(
                                new VariableUsedReference(agencyId, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", "1", "Variable")
                        )))
        );

        Ddi4CodeList mockCodeList = new Ddi4CodeList(
                "true", "2025-12-10T11:55:28.140Z",
                "urn:ddi:fr.insee:82466a9c-5266-434b-9dd3-329993717ad4:1",
                agencyId, "2f70f505-4a9e-4abe-82d4-c4ddfed25d52", "1",
                new Label(new Content("fr-FR", "cl")),
                List.of(new Code("true",
                        "urn:ddi:fr.insee:6a290143-b9f6-43d3-92ac-70c3b2f516c1:1",
                        agencyId, "6a290143-b9f6-43d3-92ac-70c3b2f516c1", "1",
                        new CategoryReference(agencyId, "d363a730-14d4-4c54-9464-982312cf9330", "1", "Category"),
                        "a"))
        );

        Ddi4Category mockCategory = new Ddi4Category(
                "true", "2025-12-10T11:55:28.140Z",
                "urn:ddi:fr.insee:d363a730-14d4-4c54-9464-982312cf9330:1",
                agencyId, "d363a730-14d4-4c54-9464-982312cf9330", "1",
                new Label(new Content("fr-FR", "aq"))
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship),
                List.of(mockVariable),
                List.of(mockCodeList),
                List.of(mockCategory)
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
        assertEquals("test", result.physicalInstance().get(0).citation().title().string().text());

        // Verify Variables are present
        assertNotNull(result.variable());
        assertEquals(1, result.variable().size());
        assertEquals("2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", result.variable().get(0).id());
        assertEquals("name", result.variable().get(0).variableName().string().text());

        // Verify CodeLists are present
        assertNotNull(result.codeList());
        assertEquals(1, result.codeList().size());
        assertEquals("2f70f505-4a9e-4abe-82d4-c4ddfed25d52", result.codeList().get(0).id());
        assertEquals("cl", result.codeList().get(0).label().content().text());

        // Verify Categories are present
        assertNotNull(result.category());
        assertEquals(1, result.category().size());
        assertEquals("d363a730-14d4-4c54-9464-982312cf9330", result.category().get(0).id());
        assertEquals("aq", result.category().get(0).label().content().text());

        // Verify ddiset endpoint was called
        verify(restTemplate).exchange(
                eq(baseApiUrl + "ddiset/" + agencyId + "/" + instanceId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class));

        // Verify converter was called with all item types
        ArgumentCaptor<Ddi3Response> ddi3Captor = ArgumentCaptor.forClass(Ddi3Response.class);
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(ddi3Captor.capture(), eq("ddi:4.0"));

        Ddi3Response capturedDdi3 = ddi3Captor.getValue();
        assertNotNull(capturedDdi3.items());
        assertEquals(5, capturedDdi3.items().size()); // PI + Variable + DR + CodeList + Category
    }

    @Test
    void shouldGetGroups() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";

        ColecticaItem group1 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Base permanente des équipements", "en", "Permanent Database of Facilities"), // itemName
            Map.of("fr-FR", "BPE", "en", "BPE"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "Group", // itemType
            "fr.insee", // agencyId
            1, // version
            "group-1", // identifier
            null, // item
            null, // notes
            "2025-01-09T00:00:00", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            1L, // transactionId
            0 // versionCreationType
        );

        ColecticaItem group2 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Recensement de la population", "en", "Population Census"), // itemName
            Map.of("fr-FR", "RP", "en", "PC"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "Group", // itemType
            "fr.insee", // agencyId
            1, // version
            "group-2", // identifier
            null, // item
            null, // notes
            "2025-01-08T00:00:00", // versionDate
            null, // versionResponsibility
            true, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            2L, // transactionId
            0 // versionCreationType
        );

        ColecticaResponse mockResponse = new ColecticaResponse(List.of(group1, group2), 2, 2, null, null, null);

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock query call - should query for Group itemType (4bd6eef6-99df-40e6-9b11-5b8f64e5cb23)
        when(restTemplate.postForObject(eq(queryUrl), any(HttpEntity.class), eq(ColecticaResponse.class)))
                .thenReturn(mockResponse);

        // When
        List<PartialGroup> result = ddiRepository.getGroups();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("group-1", result.get(0).id());
        assertEquals("Base permanente des équipements", result.get(0).label());
        assertEquals("fr.insee", result.get(0).agency());
        assertNotNull(result.get(0).versionDate());
        assertEquals("group-2", result.get(1).id());
        assertEquals("Recensement de la population", result.get(1).label());
        assertEquals("fr.insee", result.get(1).agency());

        // Verify query was called with Group itemType
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(queryUrl), entityCaptor.capture(), eq(ColecticaResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        QueryRequest queryRequest = (QueryRequest) capturedEntity.getBody();
        assertNotNull(queryRequest);
        assertEquals(1, queryRequest.itemTypes().size());
        assertEquals("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23", queryRequest.itemTypes().get(0)); // Group UUID
    }

    @Test
    void shouldGetGroupById() {
        // Given
        String groupId = "10a689ce-7006-429b-8e84-036b7787b422";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.insee";

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock DDI set response with Group and StudyUnits
        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n" +
                "    <ddi:TopLevelReference>\n" +
                "        <r:Agency>fr.insee</r:Agency>\n" +
                "        <r:ID>10a689ce-7006-429b-8e84-036b7787b422</r:ID>\n" +
                "        <r:Version>1</r:Version>\n" +
                "        <r:TypeOfObject>Group</r:TypeOfObject>\n" +
                "    </ddi:TopLevelReference>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <Group isUniversallyUnique=\"true\" versionDate=\"2025-01-09T09:00:00Z\" xmlns=\"ddi:group:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:10a689ce-7006-429b-8e84-036b7787b422:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>10a689ce-7006-429b-8e84-036b7787b422</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:VersionResponsibility>bauhaus</r:VersionResponsibility>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">Base permanente des équipements</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "            <r:StudyUnitReference>\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>89f5e04d-da22-485f-9c08-5fbe452b6c90</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <r:TypeOfObject>StudyUnit</r:TypeOfObject>\n" +
                "            </r:StudyUnitReference>\n" +
                "            <r:StudyUnitReference>\n" +
                "                <r:Agency>fr.insee</r:Agency>\n" +
                "                <r:ID>820a7c14-0ac4-42bc-a8c1-d39f60e304ee</r:ID>\n" +
                "                <r:Version>1</r:Version>\n" +
                "                <r:TypeOfObject>StudyUnit</r:TypeOfObject>\n" +
                "            </r:StudyUnitReference>\n" +
                "        </Group>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <StudyUnit isUniversallyUnique=\"true\" versionDate=\"2025-01-09T09:00:00Z\" xmlns=\"ddi:studyunit:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:89f5e04d-da22-485f-9c08-5fbe452b6c90:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>89f5e04d-da22-485f-9c08-5fbe452b6c90</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">BPE 2021</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "        </StudyUnit>\n" +
                "    </Fragment>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <StudyUnit isUniversallyUnique=\"true\" versionDate=\"2025-01-09T09:00:00Z\" xmlns=\"ddi:studyunit:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:820a7c14-0ac4-42bc-a8c1-d39f60e304ee:1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>820a7c14-0ac4-42bc-a8c1-d39f60e304ee</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">BPE 2022</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "        </StudyUnit>\n" +
                "    </Fragment>\n" +
                "</ddi:FragmentInstance>";

        // Mock the direct call to /ddiset/{agencyId}/{identifier}
        when(restTemplate.exchange(
                eq(baseApiUrl + "ddiset/" + agencyId + "/" + groupId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));

        // When
        Ddi4GroupResponse result = ddiRepository.getGroup(agencyId, groupId);

        // Then
        assertNotNull(result);
        assertEquals("ddi:4.0", result.schema());

        // Verify Group
        assertNotNull(result.group());
        assertEquals(1, result.group().size());
        assertEquals(groupId, result.group().get(0).id());
        assertEquals(agencyId, result.group().get(0).agency());
        assertEquals("Base permanente des équipements", result.group().get(0).citation().title().string().text());
        assertEquals(2, result.group().get(0).studyUnitReference().size());

        // Verify StudyUnits
        assertNotNull(result.studyUnit());
        assertEquals(2, result.studyUnit().size());
        assertEquals("89f5e04d-da22-485f-9c08-5fbe452b6c90", result.studyUnit().get(0).id());
        assertEquals("BPE 2021", result.studyUnit().get(0).citation().title().string().text());
        assertEquals("820a7c14-0ac4-42bc-a8c1-d39f60e304ee", result.studyUnit().get(1).id());
        assertEquals("BPE 2022", result.studyUnit().get(1).citation().title().string().text());

        // Verify TopLevelReference
        assertNotNull(result.topLevelReference());
        assertEquals(1, result.topLevelReference().size());
        assertEquals(groupId, result.topLevelReference().get(0).id());
        assertEquals("Group", result.topLevelReference().get(0).typeOfObject());

        // Verify ddiset endpoint was called
        verify(restTemplate).exchange(
                eq(baseApiUrl + "ddiset/" + agencyId + "/" + groupId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void shouldGetMutualizedCodesLists() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String getDescriptionsUrl = baseApiUrl + "item/_getDescriptions";

        // Configure mutualized codes lists
        List<ColecticaConfiguration.MutualizedCodeListEntry> mutualizedEntries = List.of(
            new ColecticaConfiguration.MutualizedCodeListEntry("fr.insee", "fc65a527-a04b-4505-85de-0a181e54dbad", 1)
        );
        when(colecticaConfiguration.mutualizedCodesLists()).thenReturn(mutualizedEntries);

        // Mock response from _getDescriptions endpoint
        ColecticaItem codeList = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "CL-RMES-NAFR2-SOUS-CLASSE"), // itemName
            Map.of("fr-FR", "NAF rév. 2, 2008 - Niveau 5 - Sous-classes"), // label
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "8b108ef8-b642-4484-9c49-f88e4bf7cf1d", // itemType (CodeList)
            "fr.insee", // agencyId
            1, // version
            "fc65a527-a04b-4505-85de-0a181e54dbad", // identifier
            null, // item
            null, // notes
            "2024-10-31T10:43:38", // versionDate
            null, // versionResponsibility
            false, // isPublished
            false, // isDeprecated
            false, // isProvisional
            "DDI", // itemFormat
            1L, // transactionId
            0 // versionCreationType
        );

        ColecticaItem[] mockResponse = new ColecticaItem[] { codeList };

        // Mock configuration
        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock the POST call to _getDescriptions
        when(restTemplate.postForObject(eq(getDescriptionsUrl), any(HttpEntity.class), eq(ColecticaItem[].class)))
                .thenReturn(mockResponse);

        // When
        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("fc65a527-a04b-4505-85de-0a181e54dbad", result.get(0).id());
        assertEquals("CL-RMES-NAFR2-SOUS-CLASSE", result.get(0).label());
        assertEquals("fr.insee", result.get(0).agency());
        assertNotNull(result.get(0).versionDate());

        // Verify the request was made correctly
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(getDescriptionsUrl), entityCaptor.capture(), eq(ColecticaItem[].class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        GetDescriptionsRequest requestBody = (GetDescriptionsRequest) capturedEntity.getBody();
        assertNotNull(requestBody);
        assertEquals(1, requestBody.identifiers().size());
        assertEquals("fr.insee", requestBody.identifiers().get(0).agencyId());
        assertEquals("fc65a527-a04b-4505-85de-0a181e54dbad", requestBody.identifiers().get(0).identifier());
        assertEquals(1, requestBody.identifiers().get(0).version());
    }

    @Test
    void shouldReturnEmptyListWhenNoMutualizedCodesListsConfigured() {
        // Given - no mutualized codes lists configured
        when(colecticaConfiguration.mutualizedCodesLists()).thenReturn(null);

        // When
        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify no REST call was made
        verify(restTemplate, never()).postForObject(anyString(), any(HttpEntity.class), eq(ColecticaItem[].class));
    }

    @Test
    void shouldReturnEmptyListWhenMutualizedCodesListsIsEmpty() {
        // Given - empty mutualized codes lists
        when(colecticaConfiguration.mutualizedCodesLists()).thenReturn(List.of());

        // When
        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify no REST call was made
        verify(restTemplate, never()).postForObject(anyString(), any(HttpEntity.class), eq(ColecticaItem[].class));
    }

    @Test
    void shouldPreserveExistingLabelWhenNewTextIsNull() {
        // This tests the createLabelWithFallback behavior when newText is null
        // Given
        String instanceId = "test-pi-id";
        String agencyId = "fr.insee";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";

        // Request with null dataRelationshipLabel - should preserve existing
        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Updated PI Label",
                null, // dataRelationshipLabel is null
                null  // logicalRecordLabel is null
        );

        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        // Mock existing instance with a Label on DataRelationship
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Old Label"))),
                new DataRelationshipReference(agencyId, "dr-123", "1", "DataRelationship")
        );

        // Existing DataRelationship has a Label with "en-US" language
        Label existingDrLabel = new Label(new Content("en-US", "Existing DR Label"));
        Label existingLrLabel = new Label(new Content("de-DE", "Existing LR Label"));

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                new DataRelationshipName(new StringValue("en-US", "DR Name")),
                existingDrLabel,
                new LogicalRecord("true", "urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        new LogicalRecordName(new StringValue("fr", "LR Name")), existingLrLabel, null)
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n</ddi:FragmentInstance>";
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // Capture what's passed to the DDI4 to DDI3 converter
        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        Ddi3Response.Ddi3Item mockItem = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "2", instanceId,
                "<PhysicalInstance></PhysicalInstance>", "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(new Ddi3Response(null, List.of(mockItem)));
        when(restTemplate.postForObject(eq(itemUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then - verify that existing labels are preserved
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertNotNull(capturedDdi4);
        assertNotNull(capturedDdi4.dataRelationship());
        assertEquals(1, capturedDdi4.dataRelationship().size());

        Ddi4DataRelationship updatedDr = capturedDdi4.dataRelationship().get(0);
        // When newText is null, existing label should be preserved
        assertNotNull(updatedDr.label());
        assertEquals("en-US", updatedDr.label().content().xmlLang());
        assertEquals("Existing DR Label", updatedDr.label().content().text());
    }

    @Test
    void shouldUseDefaultLangWhenExistingLabelIsNull() {
        // This tests the createLabelWithFallback behavior when existingLabel is null
        // Given
        String instanceId = "test-pi-id";
        String agencyId = "fr.insee";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";

        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Updated PI Label",
                "New DR Label", // New label text
                "New LR Label"
        );

        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Old Label"))),
                new DataRelationshipReference(agencyId, "dr-123", "1", "DataRelationship")
        );

        // DataRelationship has NO existing Label (null)
        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                new DataRelationshipName(new StringValue("en-US", "DR Name")),
                null, // No existing label
                new LogicalRecord("true", "urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        new LogicalRecordName(new StringValue("fr", "LR Name")), null, null) // No existing label
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n</ddi:FragmentInstance>";
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        Ddi3Response.Ddi3Item mockItem = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "2", instanceId,
                "<PhysicalInstance></PhysicalInstance>", "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(new Ddi3Response(null, List.of(mockItem)));
        when(restTemplate.postForObject(eq(itemUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then - verify that DEFAULT_LANG (fr-FR) is used when existing label is null
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertNotNull(capturedDdi4);
        assertNotNull(capturedDdi4.dataRelationship());

        Ddi4DataRelationship updatedDr = capturedDdi4.dataRelationship().get(0);
        assertNotNull(updatedDr.label());
        assertEquals("fr-FR", updatedDr.label().content().xmlLang()); // Should use DEFAULT_LANG
        assertEquals("New DR Label", updatedDr.label().content().text());

        // Also verify LogicalRecord label
        assertNotNull(updatedDr.logicalRecord());
        assertNotNull(updatedDr.logicalRecord().label());
        assertEquals("fr-FR", updatedDr.logicalRecord().label().content().xmlLang()); // Should use DEFAULT_LANG
        assertEquals("New LR Label", updatedDr.logicalRecord().label().content().text());
    }

    @Test
    void shouldPreserveExistingLangWhenUpdatingLabelText() {
        // This tests the createLabelWithFallback behavior when both existingLabel and newText are provided
        // Given
        String instanceId = "test-pi-id";
        String agencyId = "fr.insee";
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";

        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Updated PI Label",
                "Updated DR Label", // New text for existing label
                "Updated LR Label"
        );

        when(instanceConfiguration.baseApiUrl()).thenReturn(baseApiUrl);

        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(new Title(new StringValue("fr-FR", "Old Label"))),
                new DataRelationshipReference(agencyId, "dr-123", "1", "DataRelationship")
        );

        // Existing labels with specific languages (not fr-FR)
        Label existingDrLabel = new Label(new Content("en-GB", "Old DR Label"));
        Label existingLrLabel = new Label(new Content("es-ES", "Old LR Label"));

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(
                "true", "2025-01-01T00:00:00",
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                new DataRelationshipName(new StringValue("en-US", "DR Name")),
                existingDrLabel,
                new LogicalRecord("true", "urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        new LogicalRecordName(new StringValue("fr", "LR Name")), existingLrLabel, null)
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(new TopLevelReference(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n</ddi:FragmentInstance>";
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(ddisetXml));
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        Ddi3Response.Ddi3Item mockItem = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "2", instanceId,
                "<PhysicalInstance></PhysicalInstance>", "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(new Ddi3Response(null, List.of(mockItem)));
        when(restTemplate.postForObject(eq(itemUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then - verify that existing language is preserved with new text
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertNotNull(capturedDdi4);

        Ddi4DataRelationship updatedDr = capturedDdi4.dataRelationship().get(0);
        assertNotNull(updatedDr.label());
        assertEquals("en-GB", updatedDr.label().content().xmlLang()); // Should preserve existing lang
        assertEquals("Updated DR Label", updatedDr.label().content().text()); // But update text

        assertNotNull(updatedDr.logicalRecord().label());
        assertEquals("es-ES", updatedDr.logicalRecord().label().content().xmlLang()); // Should preserve existing lang
        assertEquals("Updated LR Label", updatedDr.logicalRecord().label().content().text()); // But update text
    }
}