package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.colectica.client.dto.*;
import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DDIRepositoryImplTest {

    @Mock
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    @Mock
    private DDI3toDDI4ConverterService ddi3ToDdi4Converter;

    @Mock
    private DDI4toDDI3ConverterService ddi4ToDdi3Converter;

    @Mock
    private ColecticaConfiguration colecticaConfiguration;

    @Mock
    private ColecticaClient colecticaClient;

    private DDIRepositoryImpl ddiRepository;


    @BeforeEach
    void setUp() {
        lenient().when(colecticaConfiguration.langs()).thenReturn(List.of("fr-FR"));

        // Plain (non-proxied) provider: exercises the package-tree walk logic. The @Cacheable
        // interception is a Spring concern, covered by ColecticaCacheIntegrationTest.
        MutualizedCodeListRefsProvider refsProvider =
            new MutualizedCodeListRefsProvider(instanceConfiguration, colecticaConfiguration, colecticaClient);

        ddiRepository = new DDIRepositoryImpl(instanceConfiguration, ddi3ToDdi4Converter, ddi4ToDdi3Converter, colecticaConfiguration, colecticaClient, refsProvider);
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
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);

        // Mock query call
        when(colecticaClient.query(anyList()))
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


        // Verify query was issued for the PhysicalInstance item type
        verify(colecticaClient).query(eq(List.of("a51e85bb-6259-4488-8df2-f08cb43485f8")));
    }

    @Test
    void shouldGetLogicalProducts() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";
        Map<String, String> itemTypes = Map.of("LogicalProduct", "965c8d28-7d48-4950-bea7-04b27e52bb9b");

        ColecticaItem item1 = new ColecticaItem(
            null, // summary
            Map.of("fr-FR", "Produit Logique 1", "en", "Logical Product 1"), // itemName
            Map.of("fr-FR", "Label 1", "en", "Label 1"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "LogicalProduct", // itemType
            "agency1", // agencyId
            1, // version
            "lp-1", // identifier
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
            Map.of("fr-FR", "Produit Logique 2", "en", "Logical Product 2"), // itemName
            Map.of("fr-FR", "Label 2", "en", "Label 2"), // value
            null, // description
            null, // versionRationale
            0, // metadataRank
            "test-repo", // repositoryName
            true, // isAuthoritative
            List.of(), // tags
            "LogicalProduct", // itemType
            "agency2", // agencyId
            1, // version
            "lp-2", // identifier
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

        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
        when(colecticaClient.query(anyList()))
                .thenReturn(mockResponse);

        // When
        List<PartialLogicalProduct> result = ddiRepository.getLogicalProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lp-1", result.get(0).id());
        assertEquals("Produit Logique 1", result.get(0).label());
        assertEquals("agency1", result.get(0).agency());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals("2025-01-01 00:00:00", sdf.format(result.get(0).versionDate()));
        assertEquals("lp-2", result.get(1).id());
        assertEquals("Produit Logique 2", result.get(1).label());
        assertEquals("agency2", result.get(1).agency());
        assertNull(result.get(1).versionDate());

        verify(colecticaClient).query(eq(List.of("965c8d28-7d48-4950-bea7-04b27e52bb9b")));
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        // Given
        String instanceId = "2514afe4-7b08-4500-be25-7a852a10fd8c";
        String agencyId = "fr.inserm.constances";
        int version = 1;

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(instanceId, version, agencyId),
            new ColecticaSetItem("var-1", 1, agencyId),
            new ColecticaSetItem("dr-123", 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, version, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("683889c6-f74b-4d5e-92ed-908c0a42bb2d", agencyId, 1, "var-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Variable/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("f39ff278-8500-45fe-a850-3906da2d242b", agencyId, 1, "dr-123",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><DataRelationship/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(itemResponses);

        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-10-23T12:28:43.615773Z"),
                "urn:ddi:fr.inserm.constances:2514afe4-7b08-4500-be25-7a852a10fd8c:1",
                agencyId, instanceId, "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Radon et gamma")),
                null
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
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
        assertEquals("Radon et gamma", result.physicalInstance().get(0).citation().title().get(0).value());

        verify(colecticaClient).getSet(eq(agencyId), eq(instanceId), any());
        verify(colecticaClient).getDescriptions(anyList());
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
    void shouldCreatePhysicalInstance() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";
        String studyUnitId = "su-uuid-1";
        String studyUnitAgency = "fr.insee";

        String physicalInstanceLabel = "Test Physical Instance";
        String dataRelationshipLabel = "Test Data Relationship Label";
        String logicalRecordLabel = "Test LogicalRecord Label";
        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
                physicalInstanceLabel,
                dataRelationshipLabel,
                logicalRecordLabel,
                studyUnitId,
                studyUnitAgency,
                null,
                null
        );

        // Mock configuration
        when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
                "StudyUnit", "30ea0200-7121-4f01-8d21-a931a182b86d"
        ));
        when(instanceConfiguration.itemFormat()).thenReturn("dc337820-af3a-4c0b-82f9-cf02535cde83");

        // Mock StudyUnit fetch
        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\" isUniversallyUnique=\"true\" versionDate=\"2025-01-01T00:00:00\"/>"
                + "</Fragment>";
        ColecticaItemResponse studyUnitResponse = new ColecticaItemResponse(
                "30ea0200-7121-4f01-8d21-a931a182b86d", studyUnitAgency, 2, studyUnitId,
                studyUnitXml, "2025-01-01T00:00:00", null, false, false, false, null);
        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(studyUnitResponse);

        // Mock item creation
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");

        // Mock set and _getList responses for getPhysicalInstance call after creation
        ColecticaSetItem[] setItems = {
            new ColecticaSetItem("test-id", 1, "fr.insee")
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] getListResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", "fr.insee", 1, "test-id",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(getListResponses);
        // Mock converter
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:test-id:1",
                "fr.insee", "test-id", "1",
                null,
                new Citation(LangStrings.of("fr-FR", physicalInstanceLabel)),
                null
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of("fr.insee", "test-id", "1", "PhysicalInstance")),
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
        assertEquals(physicalInstanceLabel, result.physicalInstance().get(0).citation().title().get(0).value());

        // Verify item creation was sent through the SDK
        ArgumentCaptor<ColecticaCreateItemRequest> bodyCaptor = ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
        verify(colecticaClient).createOrUpdateItems(bodyCaptor.capture());
        ColecticaCreateItemRequest createRequest = bodyCaptor.getValue();
        assertNotNull(createRequest);
        assertEquals(3, createRequest.items().size()); // PhysicalInstance + DataRelationship + StudyUnit

        // Verify first item is PhysicalInstance
        ColecticaItemResponse piItem = createRequest.items().get(0);
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", piItem.itemType()); // PhysicalInstance UUID
        assertTrue(piItem.item().contains(physicalInstanceLabel));

        // Verify second item is DataRelationship
        ColecticaItemResponse drItem = createRequest.items().get(1);
        assertEquals("f39ff278-8500-45fe-a850-3906da2d242b", drItem.itemType()); // DataRelationship UUID
        assertTrue(drItem.item().contains(dataRelationshipLabel));

        // Verify third item is the updated StudyUnit with the PI reference injected
        ColecticaItemResponse suItem = createRequest.items().get(2);
        assertEquals("30ea0200-7121-4f01-8d21-a931a182b86d", suItem.itemType()); // StudyUnit UUID
        assertEquals(2, suItem.version()); // version preserved, not incremented
        assertTrue(suItem.item().contains("PhysicalInstanceReference"));
        assertTrue(suItem.item().contains("PhysicalInstance")); // TypeOfObject
    }

    @Test
    void shouldPreserveExistingPhysicalInstanceReferencesInStudyUnit() {
        // Given: a StudyUnit that already contains one PhysicalInstanceReference
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String itemUrl = baseApiUrl + "item";
        String studyUnitId = "su-uuid-existing";
        String studyUnitAgency = "fr.insee";
        String existingPiId = "existing-pi-uuid";

        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
                "New PI", "New DR", "New LR",
                studyUnitId, studyUnitAgency, null, null
        );

        when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
                "StudyUnit", "30ea0200-7121-4f01-8d21-a931a182b86d"
        ));
        when(instanceConfiguration.itemFormat()).thenReturn("dc337820-af3a-4c0b-82f9-cf02535cde83");

        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\" isUniversallyUnique=\"true\">"
                + "<r:PhysicalInstanceReference xmlns:r=\"ddi:reusable:3_3\">"
                + "<r:Agency>fr.insee</r:Agency>"
                + "<r:ID>" + existingPiId + "</r:ID>"
                + "<r:Version>1</r:Version>"
                + "<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>"
                + "</r:PhysicalInstanceReference>"
                + "</StudyUnit>"
                + "</Fragment>";
        ColecticaItemResponse studyUnitResponse = new ColecticaItemResponse(
                "30ea0200-7121-4f01-8d21-a931a182b86d", studyUnitAgency, 3, studyUnitId,
                studyUnitXml, "2025-01-01T00:00:00", null, false, false, false, null);
        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(studyUnitResponse);

        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(new ColecticaSetItem[0]);

        // When
        ddiRepository.createPhysicalInstance(request);

        // Then: the StudyUnit item sent contains both the existing and the new PhysicalInstanceReference
        ArgumentCaptor<ColecticaCreateItemRequest> bodyCaptor3 = ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
        verify(colecticaClient).createOrUpdateItems(bodyCaptor3.capture());
        ColecticaCreateItemRequest createRequest = bodyCaptor3.getValue();

        ColecticaItemResponse suItem = createRequest.items().get(2);
        // Each PhysicalInstanceReference element contributes one open + one close tag = 2 occurrences per reference
        assertEquals(4, suItem.item().split("PhysicalInstanceReference").length - 1); // two references × 2 tags each
        assertTrue(suItem.item().contains(existingPiId)); // existing reference preserved
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

        // Mock set and _getList responses for getPhysicalInstance call
        ColecticaSetItem[] existingSetItems = {
            new ColecticaSetItem(instanceId, 1, agencyId),
            new ColecticaSetItem("dr-123", 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(existingSetItems);

        ColecticaItemResponse[] existingItemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("f39ff278-8500-45fe-a850-3906da2d242b", agencyId, 1, "dr-123",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><DataRelationship/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(existingItemResponses);
        // Mock converter
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Old Label")),
                List.of(Reference.of(agencyId, "dr-123", "1", "DataRelationship"))
        );

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                null,
                List.of(new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        null, null))
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // Mock DDI4 to DDI3 conversion for updateFullPhysicalInstance
        Ddi3Response.Ddi3Item mockPiDdi3Item = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "1", instanceId,
                "<PhysicalInstance>" + newLabel + "</PhysicalInstance>",
                "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        Ddi3Response.Ddi3Item mockDrDdi3Item = new Ddi3Response.Ddi3Item(
                "f39ff278-8500-45fe-a850-3906da2d242b", agencyId, "1", "dr-123",
                "<DataRelationship>" + newDataRelationshipLabel + "</DataRelationship>",
                "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        Ddi3Response mockDdi3Response = new Ddi3Response(null, List.of(mockPiDdi3Item, mockDrDdi3Item));
        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(mockDdi3Response);

        // Mock item update
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then
        // Version of the objects sent to Colectica must NOT be incremented
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertEquals("1", capturedDdi4.physicalInstance().get(0).version()); // version preserved, not incremented
        assertEquals("1", capturedDdi4.dataRelationship().get(0).version()); // version preserved, not incremented

        ArgumentCaptor<ColecticaCreateItemRequest> bodyCaptor2 = ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
        verify(colecticaClient).createOrUpdateItems(bodyCaptor2.capture());
        ColecticaCreateItemRequest createRequest = bodyCaptor2.getValue();
        assertNotNull(createRequest);
        assertEquals(2, createRequest.items().size()); // PhysicalInstance + DataRelationship

        // Verify first item is PhysicalInstance with updated label
        ColecticaItemResponse piItem = createRequest.items().get(0);
        assertEquals("a51e85bb-6259-4488-8df2-f08cb43485f8", piItem.itemType()); // PhysicalInstance UUID
        assertEquals(1, piItem.version()); // version preserved, not incremented
        assertTrue(piItem.item().contains(newLabel));

        // Verify second item is DataRelationship with updated name
        ColecticaItemResponse drItem = createRequest.items().get(1);
        assertEquals("f39ff278-8500-45fe-a850-3906da2d242b", drItem.itemType()); // DataRelationship UUID
        assertEquals(1, drItem.version()); // version preserved, not incremented
        assertTrue(drItem.item().contains(newDataRelationshipLabel));
    }

    @Test
    void getPhysicalInstance_skipsCodeListAndCategoryItemTypes() {
        // Les CodeList et Category sont volontairement omises de la réponse GET PI
        // (chargées paresseusement au clic d'une variable) pour réduire le payload
        // et le coût de conversion DDI3 -> DDI4 sur ce endpoint.
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
        String categoryType = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
                "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
                "CodeList", codeListType,
                "Category", categoryType
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(instanceId, 1, agencyId),
            new ColecticaSetItem("2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", 1, agencyId),
            new ColecticaSetItem("795aa4b8-acec-4ef8-8f08-3a200c7bdb10", 1, agencyId),
            new ColecticaSetItem("2f70f505-4a9e-4abe-82d4-c4ddfed25d52", 1, agencyId),
            new ColecticaSetItem("d363a730-14d4-4c54-9464-982312cf9330", 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("683889c6-f74b-4d5e-92ed-908c0a42bb2d", agencyId, 1, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Variable/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("f39ff278-8500-45fe-a850-3906da2d242b", agencyId, 1, "795aa4b8-acec-4ef8-8f08-3a200c7bdb10",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><DataRelationship/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(codeListType, agencyId, 1, "2f70f505-4a9e-4abe-82d4-c4ddfed25d52",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(categoryType, agencyId, 1, "d363a730-14d4-4c54-9464-982312cf9330",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(itemResponses);

        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:14.251595Z"),
                "urn:ddi:fr.insee:32799021-0663-41cd-aca6-3ad8dbdae3e3:1",
                agencyId, instanceId, "1",
                null,
                new Citation(LangStrings.of("fr-FR", "test")),
                List.of(Reference.of(agencyId, "795aa4b8-acec-4ef8-8f08-3a200c7bdb10", "1", "DataRelationship"))
        );

        Ddi4Variable mockVariable = new Ddi4Variable(Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:33.138Z"),
                "urn:ddi:fr.insee:2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d:1",
                agencyId,
                "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d",
                "1",
                null,
                LangStrings.of("fr-FR", "name"),
                LangStrings.of("fr-FR", "Test Label"),
                null,
                new VariableRepresentation(null,
                    new CodeRepresentation(CodeRepresentation.TYPE,false,
                        Reference.of(agencyId, "2f70f505-4a9e-4abe-82d4-c4ddfed25d52", "1", "CodeList")),
                    null, null, null),
                null
        );

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:14.251595Z"),
                "urn:ddi:fr.insee:795aa4b8-acec-4ef8-8f08-3a200c7bdb10:1",
                agencyId, "795aa4b8-acec-4ef8-8f08-3a200c7bdb10", "1",
                null,
                null,
                List.of(new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:8585972f-2dc2-4125-87b2-60fd3f243cf3:1",
                        agencyId, "8585972f-2dc2-4125-87b2-60fd3f243cf3", "1",
                        null,
                        new VariablesInRecord(List.of(
                                Reference.of(agencyId, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", "1", "Variable")
                        ))))
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship),
                List.of(mockVariable),
                null,
                null
        );

        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // When
        Ddi4Response result = ddiRepository.getPhysicalInstance(agencyId, instanceId);

        // Then : la réponse contient PI / DR / Variable mais ni CodeList ni Category
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());
        assertEquals(instanceId, result.physicalInstance().get(0).id());

        assertNotNull(result.variable());
        assertEquals(1, result.variable().size());

        assertNull(result.codeList(), "CodeList ne doit pas être renvoyée par GET PI");
        assertNull(result.category(), "Category ne doit pas être renvoyée par GET PI");

        verify(colecticaClient).getSet(eq(agencyId), eq(instanceId), any());
        verify(colecticaClient).getDescriptions(anyList());

        // Et surtout : les items CodeList/Category ne sont pas passés au converter
        // (économie du coût de conversion DDI3 -> DDI4 sur ces items souvent volumineux)
        ArgumentCaptor<Ddi3Response> ddi3Captor = ArgumentCaptor.forClass(Ddi3Response.class);
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(ddi3Captor.capture(), eq("ddi:4.0"));

        Ddi3Response capturedDdi3 = ddi3Captor.getValue();
        assertNotNull(capturedDdi3.items());
        assertEquals(3, capturedDdi3.items().size(), "Seuls PI, Variable et DR sont convertis");
        assertTrue(capturedDdi3.items().stream().noneMatch(i -> codeListType.equals(i.itemType())));
        assertTrue(capturedDdi3.items().stream().noneMatch(i -> categoryType.equals(i.itemType())));
    }

    @Test
    void getPhysicalInstanceCodeLists_keepsOnlyCodeListAndCategoryItems() {
        // L'endpoint /codeslists doit fonctionner sans repasser par getPhysicalInstance
        // (qui n'inclut plus les CodeList). On filtre côté repo pour ne convertir que
        // les CodeList et Category, et on renvoie les CodeLists résultantes.
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";
        String codeListId = "2f70f505-4a9e-4abe-82d4-c4ddfed25d52";
        String categoryId = "d363a730-14d4-4c54-9464-982312cf9330";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
        String categoryType = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
                "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
                "CodeList", codeListType,
                "Category", categoryType
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(instanceId, 1, agencyId),
            new ColecticaSetItem("2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", 1, agencyId),
            new ColecticaSetItem(codeListId, 1, agencyId),
            new ColecticaSetItem(categoryId, 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("683889c6-f74b-4d5e-92ed-908c0a42bb2d", agencyId, 1, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Variable/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(codeListType, agencyId, 1, codeListId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(categoryType, agencyId, 1, categoryId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(itemResponses);

        Ddi4CodeList mockCodeList = new Ddi4CodeList(Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:28.140Z"),
                "urn:ddi:fr.insee:" + codeListId + ":1",
                agencyId, codeListId, "1",
                LangStrings.of("fr-FR", "ma code list"),
                List.of(new Code(Code.TYPE,
                        "urn:ddi:fr.insee:6a290143-b9f6-43d3-92ac-70c3b2f516c1:1",
                        agencyId, "6a290143-b9f6-43d3-92ac-70c3b2f516c1", "1",
                        Reference.of(agencyId, categoryId, "1", "Category"),
                        ValueType.of("a")))
        );
        Ddi4Category mockCategory = new Ddi4Category(Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:28.140Z"),
                "urn:ddi:fr.insee:" + categoryId + ":1",
                agencyId, categoryId, "1",
                LangStrings.of("fr-FR", "cat")
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0", null, null, null, null,
                List.of(mockCodeList), List.of(mockCategory)
        );
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // When
        List<Ddi4CodeList> result = ddiRepository.getPhysicalInstanceCodeLists(agencyId, instanceId);

        // Then : les CodeLists sont renvoyées
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(codeListId, result.get(0).id());
        assertEquals("ma code list", result.get(0).label().get(0).value());

        // Et le converter n'a reçu que les items CodeList + Category (pas la PI ni les Variables)
        ArgumentCaptor<Ddi3Response> ddi3Captor = ArgumentCaptor.forClass(Ddi3Response.class);
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(ddi3Captor.capture(), eq("ddi:4.0"));

        Ddi3Response capturedDdi3 = ddi3Captor.getValue();
        assertEquals(2, capturedDdi3.items().size(), "Seules CodeList et Category sont converties");
        assertTrue(capturedDdi3.items().stream().anyMatch(i -> codeListType.equals(i.itemType())));
        assertTrue(capturedDdi3.items().stream().anyMatch(i -> categoryType.equals(i.itemType())));
    }

    @Test
    void getPhysicalInstanceCodeLists_returnsEmptyWhenNoCodeListInSet() {
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(instanceId, 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        List<Ddi4CodeList> result = ddiRepository.getPhysicalInstanceCodeLists(agencyId, instanceId);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Aucune CodeList dans le set -> liste vide, pas d'appel au converter");
        verifyNoInteractions(ddi3ToDdi4Converter);
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

        // Mock query call - should query for Group itemType (4bd6eef6-99df-40e6-9b11-5b8f64e5cb23)
        when(colecticaClient.query(anyList()))
                .thenReturn(mockResponse);

        // Mock second call to _getList for extracting UserID (seriesIris) from full XML
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(new ColecticaItemResponse[0]);

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

        // Verify query was issued for the Group item type
        ArgumentCaptor<List<String>> itemTypesCaptor = ArgumentCaptor.forClass(List.class);
        verify(colecticaClient).query(itemTypesCaptor.capture());
        assertEquals(1, itemTypesCaptor.getValue().size());
        assertEquals("4bd6eef6-99df-40e6-9b11-5b8f64e5cb23", itemTypesCaptor.getValue().get(0)); // Group UUID
    }

    @Test
    void shouldGetGroupById() {
        // Given
        String groupId = "10a689ce-7006-429b-8e84-036b7787b422";
        String agencyId = "fr.insee";

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
        when(colecticaClient.getDdiSet(anyString(), anyString()))
                .thenReturn(ddisetXml.getBytes(StandardCharsets.UTF_8));

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
        assertEquals("Base permanente des équipements", result.group().get(0).citation().title().get(0).value());
        assertEquals(2, result.group().get(0).studyUnitReference().size());

        // Verify StudyUnits
        assertNotNull(result.studyUnit());
        assertEquals(2, result.studyUnit().size());
        assertEquals("89f5e04d-da22-485f-9c08-5fbe452b6c90", result.studyUnit().get(0).id());
        assertEquals("BPE 2021", result.studyUnit().get(0).citation().title().get(0).value());
        assertEquals("820a7c14-0ac4-42bc-a8c1-d39f60e304ee", result.studyUnit().get(1).id());
        assertEquals("BPE 2022", result.studyUnit().get(1).citation().title().get(0).value());

        // Verify TopLevelReference
        assertNotNull(result.topLevelReference());
        assertEquals(1, result.topLevelReference().size());
        assertEquals(groupId, result.topLevelReference().get(0).id());
        assertEquals("Group", result.topLevelReference().get(0).type());

        // Verify ddiset endpoint was called
        verify(colecticaClient).getDdiSet(eq(agencyId), eq(groupId));
    }

    @Test
    void shouldPreserveUtf8AccentsWhenGroupResponseHasNoCharset() {
        // Reproduces the mojibake bug: Colectica returns UTF-8 bytes but with a Content-Type
        // that omits charset, so Spring's StringHttpMessageConverter falls back to ISO-8859-1.
        // The repository must read raw bytes and decode as UTF-8 itself.
        String groupId = "4ae1ad6e-bd5a-3ae7-ab21-57efc2f5e279";
        String agencyId = "fr.insee";

        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n" +
                "    <ddi:TopLevelReference>\n" +
                "        <r:Agency>fr.insee</r:Agency>\n" +
                "        <r:ID>" + groupId + "</r:ID>\n" +
                "        <r:Version>1</r:Version>\n" +
                "        <r:TypeOfObject>Group</r:TypeOfObject>\n" +
                "    </ddi:TopLevelReference>\n" +
                "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n" +
                "        <Group isUniversallyUnique=\"true\" versionDate=\"2026-05-11T10:05:24Z\" xmlns=\"ddi:group:3_3\">\n" +
                "            <r:URN>urn:ddi:fr.insee:" + groupId + ":1</r:URN>\n" +
                "            <r:Agency>fr.insee</r:Agency>\n" +
                "            <r:ID>" + groupId + "</r:ID>\n" +
                "            <r:Version>1</r:Version>\n" +
                "            <r:Citation>\n" +
                "                <r:Title>\n" +
                "                    <r:String xml:lang=\"fr-FR\">Enquête capacité à innover et stratégie</r:String>\n" +
                "                </r:Title>\n" +
                "            </r:Citation>\n" +
                "        </Group>\n" +
                "    </Fragment>\n" +
                "</ddi:FragmentInstance>";
        byte[] ddisetBytes = ddisetXml.getBytes(StandardCharsets.UTF_8);

        // Simulate Colectica delivering raw bytes (what a real server returns), not a pre-decoded String.
        when(colecticaClient.getDdiSet(anyString(), anyString())).thenReturn(ddisetBytes);

        Ddi4GroupResponse result = ddiRepository.getGroup(agencyId, groupId);

        assertNotNull(result);
        assertNotNull(result.group());
        assertEquals(1, result.group().size());
        assertEquals(
                "Enquête capacité à innover et stratégie",
                result.group().get(0).citation().title().get(0).value()
        );
    }

    private static final String CODE_LIST_TYPE = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
    private static final String CODE_LIST_SCHEME_TYPE = "4193d389-b5ae-4368-b399-cd5a7ee3653c";
    private static final String CODE_LIST_GROUP_TYPE = "394b9ff3-7248-4ede-b945-9bebdbf56bed";
    private static final String CATEGORY_TYPE = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";
    private static final String VARIABLE_TYPE = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
    private static final String DATA_RELATIONSHIP_TYPE = "f39ff278-8500-45fe-a850-3906da2d242b";
    private static final String PHYSICAL_INSTANCE_TYPE = "a51e85bb-6259-4488-8df2-f08cb43485f8";
    private static final String STUDY_UNIT_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";

    private static Map<String, String> standardItemTypes() {
        return Map.of(
            "CodeList", CODE_LIST_TYPE,
            "CodeListScheme", CODE_LIST_SCHEME_TYPE,
            "CodeListGroup", CODE_LIST_GROUP_TYPE,
            "Category", CATEGORY_TYPE,
            "Variable", VARIABLE_TYPE,
            "DataRelationship", DATA_RELATIONSHIP_TYPE,
            "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
            "StudyUnit", STUDY_UNIT_TYPE
        );
    }

    private static ColecticaItem codeListItem(String identifier, String labelFr, String versionDate) {
        return itemOfType(identifier, CODE_LIST_TYPE, labelFr, versionDate);
    }

    private static ColecticaItem itemOfType(String identifier, String itemType, String labelFr, String versionDate) {
        return new ColecticaItem(
            null,
            labelFr == null ? null : Map.of("fr-FR", labelFr),
            null,
            null, null, 0, "test-repo", true, List.of(),
            itemType,
            "fr.insee", 1, identifier, null, null,
            versionDate,
            null, false, false, false, "DDI", 1L, 0
        );
    }

    @Test
    void noPackageConfigured_returnsEmptyListWithoutHttpCall() {
        // Given - no mutualized codes package configured
        when(colecticaConfiguration.mutualizedCodesPackage()).thenReturn(null);

        // When
        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // No call of any kind
        verifyNoInteractions(colecticaClient);
    }

    /** package → CodeListScheme → CodeListGroup → CodeList */
    private void stubChildren(String agencyId, String parentId, String childType, ItemReference... children) {
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, parentId)),
                eq(List.of(childType))))
            .thenReturn(List.of(children));
    }

    @Test
    void walksPackageTreeTopDownToCollectCodeLists() {
        // package → scheme → group → [cl1, cl2], walked top-down via bysubject relationships.
        String agencyId = "fr.insee";
        String packageId = "pkg-1";
        String schemeId = "scheme-1";
        String groupId = "group-1";
        String cl1Id = "cl-1";
        String cl2Id = "cl-2";

        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef(agencyId, packageId, 1));
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE, new ItemReference(agencyId, schemeId));
        stubChildren(agencyId, schemeId, CODE_LIST_GROUP_TYPE, new ItemReference(agencyId, groupId));
        stubChildren(agencyId, groupId, CODE_LIST_TYPE,
            new ItemReference(agencyId, cl1Id), new ItemReference(agencyId, cl2Id));

        // Labels are resolved through a single repository-wide CodeList query.
        when(colecticaClient.query(List.of(CODE_LIST_TYPE))).thenReturn(new ColecticaResponse(
            List.of(
                codeListItem(cl1Id, "Niveau 1", "2024-10-31T10:43:38"),
                codeListItem(cl2Id, "Niveau 2", "2024-10-31T10:43:38")
            ),
            2, 2, null, null, null
        ));

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(2, result.size());
        assertEquals(cl1Id, result.get(0).id());
        assertEquals("Niveau 1", result.get(0).label());
        assertEquals(agencyId, result.get(0).agency());
        assertEquals(cl2Id, result.get(1).id());

        // Never walks up the parent chain anymore.
        verify(colecticaClient, never())
            .findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT), any(), anyList());
    }

    @Test
    void mutualizedCodeList_prefersLabelOverItemName() {
        // Une liste de codes mutualisée porte à la fois un nom technique (itemName) et un libellé
        // lisible (label). Le sélecteur doit afficher le libellé, pas le nom.
        String agencyId = "fr.insee";
        String packageId = "pkg-1";
        String schemeId = "scheme-1";
        String groupId = "group-1";
        String clId = "cl-1";

        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef(agencyId, packageId, 1));
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE, new ItemReference(agencyId, schemeId));
        stubChildren(agencyId, schemeId, CODE_LIST_GROUP_TYPE, new ItemReference(agencyId, groupId));
        stubChildren(agencyId, groupId, CODE_LIST_TYPE, new ItemReference(agencyId, clId));

        ColecticaItem withNameAndLabel = new ColecticaItem(
            null,
            Map.of("fr-FR", "CL_NOM_TECHNIQUE"), // itemName
            Map.of("fr-FR", "Libellé lisible"),  // label
            null, null, 0, "test-repo", true, List.of(),
            CODE_LIST_TYPE,
            agencyId, 1, clId, null, null, "2024-10-31T10:43:38",
            null, false, false, false, "DDI", 1L, 0
        );
        when(colecticaClient.query(List.of(CODE_LIST_TYPE))).thenReturn(new ColecticaResponse(
            List.of(withNameAndLabel), 1, 1, null, null, null
        ));

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(1, result.size());
        assertEquals("Libellé lisible", result.get(0).label());
        // Le nom technique (itemName) reste disponible séparément pour la recherche dans le sélecteur.
        assertEquals("CL_NOM_TECHNIQUE", result.get(0).name());
    }

    @Test
    void packageWithNoCodeListScheme_returnsEmptyWithoutResolvingLabels() {
        String agencyId = "fr.insee";
        String packageId = "pkg-1";

        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef(agencyId, packageId, 1));
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE /* no children */);

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertTrue(result.isEmpty());
        // No CodeList found in the tree → no point querying for labels.
        verify(colecticaClient, never()).query(anyList());
    }

    @Test
    void codeListWithoutStrictLabel_isFilteredOut() {
        String agencyId = "fr.insee";
        String packageId = "pkg-1";
        String schemeId = "scheme-1";
        String groupId = "group-1";
        String labelledId = "cl-labelled";
        String blankId = "cl-blank";

        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef(agencyId, packageId, 1));
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE, new ItemReference(agencyId, schemeId));
        stubChildren(agencyId, schemeId, CODE_LIST_GROUP_TYPE, new ItemReference(agencyId, groupId));
        stubChildren(agencyId, groupId, CODE_LIST_TYPE,
            new ItemReference(agencyId, labelledId), new ItemReference(agencyId, blankId));

        ColecticaItem blank = new ColecticaItem(
            null,
            Map.of("fr-FR", "   "),
            Map.of("fr-FR", ""),
            null, null, 0, "test-repo", true, List.of(),
            CODE_LIST_TYPE,
            agencyId, 1, blankId, null, null, "2024-10-31T10:43:38",
            null, false, false, false, "DDI", 1L, 0
        );
        when(colecticaClient.query(List.of(CODE_LIST_TYPE))).thenReturn(new ColecticaResponse(
            List.of(codeListItem(labelledId, "Has label", "2024-10-31T10:43:38"), blank),
            2, 2, null, null, null
        ));

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(1, result.size());
        assertEquals(labelledId, result.get(0).id());
    }


    @Test
    void sameCodeListReachableViaTwoGroups_appearsOnce() {
        String agencyId = "fr.insee";
        String packageId = "pkg-1";
        String schemeId = "scheme-1";
        String groupAId = "group-a";
        String groupBId = "group-b";
        String sharedClId = "cl-shared";

        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef(agencyId, packageId, 1));
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE, new ItemReference(agencyId, schemeId));
        stubChildren(agencyId, schemeId, CODE_LIST_GROUP_TYPE,
            new ItemReference(agencyId, groupAId), new ItemReference(agencyId, groupBId));
        stubChildren(agencyId, groupAId, CODE_LIST_TYPE, new ItemReference(agencyId, sharedClId));
        stubChildren(agencyId, groupBId, CODE_LIST_TYPE, new ItemReference(agencyId, sharedClId));
        when(colecticaClient.query(List.of(CODE_LIST_TYPE))).thenReturn(new ColecticaResponse(
            List.of(codeListItem(sharedClId, "Shared label", "2024-10-31T10:43:38")),
            1, 1, null, null, null
        ));

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(1, result.size());
        assertEquals(sharedClId, result.get(0).id());
    }

    @Test
    void shouldGetMutualizedCodesListWithCodesAndCategories() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.insee";
        String codeListId = "fc65a527-a04b-4505-85de-0a181e54dbad";
        String categoryId = "cat-1";
        int version = 1;

        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(codeListId, version, agencyId),
            new ColecticaSetItem(categoryId, version, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse(
                    "8b108ef8-b642-4484-9c49-f88e4bf7cf1d", // CodeList type
                    agencyId, version, codeListId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(
                    "fa1d4dca-f6dc-4d80-8b94-de1063a64d6d", // Category type
                    agencyId, version, categoryId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        Ddi4CodeList mockCodeList = new Ddi4CodeList(Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2024-10-31T10:43:38"),
                "urn:ddi:fr.insee:" + codeListId + ":1",
                agencyId, codeListId, "1",
                LangStrings.of("fr-FR", "NAF rév. 2"),
                List.of()
        );
        Ddi4Category mockCategory = new Ddi4Category(Ddi4Category.TYPE,
                CogsDate.ofDateTime("2024-10-31T10:43:38"),
                "urn:ddi:fr.insee:" + categoryId + ":1",
                agencyId, categoryId, "1",
                LangStrings.of("fr-FR", "Agriculture")
        );
        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, codeListId, "1", "CodeList")),
                List.of(), List.of(), List.of(),
                List.of(mockCodeList),
                List.of(mockCategory)
        );
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        // When
        Ddi4Response result = ddiRepository.getMutualizedCodesList(agencyId, codeListId);

        // Then
        assertNotNull(result);
        assertNotNull(result.codeList());
        assertEquals(1, result.codeList().size());
        assertEquals(codeListId, result.codeList().get(0).id());
        assertNotNull(result.category());
        assertEquals(1, result.category().size());
        assertEquals(categoryId, result.category().get(0).id());

        verify(colecticaClient).getSet(eq(agencyId), eq(codeListId), any());
        verify(colecticaClient).getDescriptions(anyList());
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0"));
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


        // Mock existing instance with a Label on DataRelationship
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Old Label")),
                List.of(Reference.of(agencyId, "dr-123", "1", "DataRelationship"))
        );

        // Existing DataRelationship has a Label with "en-US" language
        List<LangString> existingDrLabel = LangStrings.of("en-US", "Existing DR Label");
        List<LangString> existingLrLabel = LangStrings.of("de-DE", "Existing LR Label");

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                existingDrLabel,
                List.of(new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        existingLrLabel, null))
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        ColecticaSetItem[] updateSetItems = { new ColecticaSetItem(instanceId, 1, agencyId) };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(updateSetItems);
        ColecticaItemResponse[] updateItemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(updateItemResponses);
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
        when(colecticaClient.createOrUpdateItems(any()))
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
        assertEquals("en-US", updatedDr.label().get(0).language());
        assertEquals("Existing DR Label", updatedDr.label().get(0).value());
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


        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Old Label")),
                List.of(Reference.of(agencyId, "dr-123", "1", "DataRelationship"))
        );

        // DataRelationship has NO existing Label (null)
        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                null, // No existing label
                List.of(new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        null, null)) // No existing label
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        ColecticaSetItem[] updateSetItems = { new ColecticaSetItem(instanceId, 1, agencyId) };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(updateSetItems);
        ColecticaItemResponse[] updateItemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(updateItemResponses);
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        Ddi3Response.Ddi3Item mockItem = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "2", instanceId,
                "<PhysicalInstance></PhysicalInstance>", "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(new Ddi3Response(null, List.of(mockItem)));
        when(colecticaClient.createOrUpdateItems(any()))
                .thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then - verify that DEFAULT_LANG (fr-FR) is used when existing label is null
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertNotNull(capturedDdi4);
        assertNotNull(capturedDdi4.dataRelationship());

        Ddi4DataRelationship updatedDr = capturedDdi4.dataRelationship().get(0);
        assertNotNull(updatedDr.label());
        assertEquals("fr-FR", updatedDr.label().get(0).language()); // Should use DEFAULT_LANG
        assertEquals("New DR Label", updatedDr.label().get(0).value());

        // Also verify LogicalRecord label
        assertNotNull(updatedDr.logicalRecord());
        assertEquals(1, updatedDr.logicalRecord().size());
        assertNotNull(updatedDr.logicalRecord().get(0).label());
        assertEquals("fr-FR", updatedDr.logicalRecord().get(0).label().get(0).language()); // Should use DEFAULT_LANG
        assertEquals("New LR Label", updatedDr.logicalRecord().get(0).label().get(0).value());
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


        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId, instanceId, "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Old Label")),
                List.of(Reference.of(agencyId, "dr-123", "1", "DataRelationship"))
        );

        // Existing labels with specific languages (not fr-FR)
        List<LangString> existingDrLabel = LangStrings.of("en-GB", "Old DR Label");
        List<LangString> existingLrLabel = LangStrings.of("es-ES", "Old LR Label");

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:dr-123:1",
                agencyId, "dr-123", "1",
                null,
                existingDrLabel,
                List.of(new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:lr-123:1", agencyId, "lr-123", "1",
                        existingLrLabel, null))
        );

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship), List.of(), List.of(), List.of()
        );

        ColecticaSetItem[] updateSetItems = { new ColecticaSetItem(instanceId, 1, agencyId) };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(updateSetItems);
        ColecticaItemResponse[] updateItemResponses = {
            new ColecticaItemResponse("a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, 1, instanceId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(updateItemResponses);
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        Ddi3Response.Ddi3Item mockItem = new Ddi3Response.Ddi3Item(
                "a51e85bb-6259-4488-8df2-f08cb43485f8", agencyId, "2", instanceId,
                "<PhysicalInstance></PhysicalInstance>", "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(new Ddi3Response(null, List.of(mockItem)));
        when(colecticaClient.createOrUpdateItems(any()))
                .thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then - verify that existing language is preserved with new text
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertNotNull(capturedDdi4);

        Ddi4DataRelationship updatedDr = capturedDdi4.dataRelationship().get(0);
        assertNotNull(updatedDr.label());
        assertEquals("en-GB", updatedDr.label().get(0).language()); // Should preserve existing lang
        assertEquals("Updated DR Label", updatedDr.label().get(0).value()); // But update text

        assertNotNull(updatedDr.logicalRecord().get(0).label());
        assertEquals("es-ES", updatedDr.logicalRecord().get(0).label().get(0).language()); // Should preserve existing lang
        assertEquals("Updated LR Label", updatedDr.logicalRecord().get(0).label().get(0).value()); // But update text
    }

    @Test
    void shouldGetItemXmlWithVersion() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agency = "fr.insee";
        String id = "3b317f4c-79ae-422c-8cd4-04ba9d2e4be4";
        String version = "1";
        String expectedXml = "<CodeList><URN>urn:ddi:fr.insee:3b317f4c-79ae-422c-8cd4-04ba9d2e4be4:1</URN></CodeList>";


        ColecticaItemResponse itemResponse = new ColecticaItemResponse(
                "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                agency, 1, id, expectedXml,
                "2025-01-01T00:00:00", null, true, false, false, "DDI"
        );

        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(itemResponse);

        // When
        String result = ddiRepository.getItemXml(agency, id, version);

        // Then
        assertEquals(expectedXml, result);
    }

    @Test
    void shouldGetItemXmlLatestVersion() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agency = "fr.insee";
        String id = "3b317f4c-79ae-422c-8cd4-04ba9d2e4be4";
        String expectedXml = "<CodeList><URN>urn:ddi:fr.insee:3b317f4c-79ae-422c-8cd4-04ba9d2e4be4:2</URN></CodeList>";


        ColecticaItemResponse itemResponse = new ColecticaItemResponse(
                "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                agency, 2, id, expectedXml,
                "2025-06-01T00:00:00", null, true, false, false, "DDI"
        );

        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(itemResponse);

        // When
        String result = ddiRepository.getItemXml(agency, id);

        // Then
        assertEquals(expectedXml, result);
    }

    @Test
    void shouldReturnNullWhenItemXmlResponseIsNull() {
        // Given
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agency = "fr.insee";
        String id = "3b317f4c-79ae-422c-8cd4-04ba9d2e4be4";
        String version = "1";

        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(null);

        // When
        String result = ddiRepository.getItemXml(agency, id, version);

        // Then
        assertNull(result);
    }

    @Test
    void shouldFindStudyUnitXmlByOperationIri_returnsXmlWhenMatching() {
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String suId = "su-abc";
        String suAgency = "fr.insee";


        ColecticaItem suItem = new ColecticaItem(
            null, Map.of("fr-FR", "BPE 2021"), Map.of(), null, null, 0, "repo", true, List.of(),
            "30ea0200-7121-4f01-8d21-a931a182b86d", suAgency, 1, suId,
            null, null, "2025-01-01T00:00:00", null, false, false, false, "DDI", 1L, 0
        );
        ColecticaResponse queryResponse = new ColecticaResponse(List.of(suItem), 1, 1, null, null, null);
        when(colecticaClient.query(anyList()))
                .thenReturn(queryResponse);

        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\">"
                + "<r:UserID>" + operationIri + "</r:UserID>"
                + "</StudyUnit></Fragment>";
        ColecticaItemResponse itemResponse = new ColecticaItemResponse(
                "30ea0200-7121-4f01-8d21-a931a182b86d", suAgency, 1, suId,
                studyUnitXml, "2025-01-01T00:00:00", null, false, false, false, "DDI"
        );
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(new ColecticaItemResponse[]{itemResponse});

        Optional<String> result = ddiRepository.findStudyUnitXmlByOperationIri(operationIri);

        assertTrue(result.isPresent());
        assertEquals(studyUnitXml, result.get());
    }

    @Test
    void shouldFindStudyUnitXmlByOperationIri_returnsEmptyWhenNoMatch() {
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String queryUrl = baseApiUrl + "_query";
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        String suId = "su-xyz";
        String suAgency = "fr.insee";


        ColecticaItem suItem = new ColecticaItem(
            null, Map.of("fr-FR", "BPE 2021"), Map.of(), null, null, 0, "repo", true, List.of(),
            "30ea0200-7121-4f01-8d21-a931a182b86d", suAgency, 1, suId,
            null, null, "2025-01-01T00:00:00", null, false, false, false, "DDI", 1L, 0
        );
        ColecticaResponse queryResponse = new ColecticaResponse(List.of(suItem), 1, 1, null, null, null);
        when(colecticaClient.query(anyList()))
                .thenReturn(queryResponse);

        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\">"
                + "<r:UserID>http://id.insee.fr/operations/operation/other</r:UserID>"
                + "</StudyUnit></Fragment>";
        ColecticaItemResponse itemResponse = new ColecticaItemResponse(
                "30ea0200-7121-4f01-8d21-a931a182b86d", suAgency, 1, suId,
                studyUnitXml, "2025-01-01T00:00:00", null, false, false, false, "DDI"
        );
        when(colecticaClient.getDescriptions(anyList()))
                .thenReturn(new ColecticaItemResponse[]{itemResponse});

        Optional<String> result = ddiRepository.findStudyUnitXmlByOperationIri(operationIri);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldGetPhysicalInstanceParents() {
        // Given
        String studyUnitType = "30ea0200-7121-4f01-8d21-a931a182b86d";
        String groupType = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";
        String agencyId = "fr.insee";
        String piId = "pi-111";

        // PI → StudyUnit
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference(agencyId, piId)),
                eq(List.of(studyUnitType))))
            .thenReturn(List.of(new ItemReference("fr.insee", "su-222")));
        // StudyUnit → Group
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference("fr.insee", "su-222")),
                eq(List.of(groupType))))
            .thenReturn(List.of(new ItemReference("fr.insee", "grp-333")));

        // When
        PhysicalInstanceParents result = ddiRepository.getPhysicalInstanceParents(agencyId, piId);

        // Then
        assertNotNull(result);
        assertEquals("su-222", result.studyUnitId());
        assertEquals("fr.insee", result.studyUnitAgency());
        assertEquals("grp-333", result.groupId());
        assertEquals("fr.insee", result.groupAgency());
        verify(colecticaClient, times(2))
            .findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT), any(), anyList());
    }

    // ---- #485 : getCodeList / getCodeListXml (CodeList + Categories, versioned) ----

    @Test
    void getCodeList_withVersion_usesVersionedSetUrlAndConvertsToDdi4() {
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.insee";
        String codeListId = "fc65a527-a04b-4505-85de-0a181e54dbad";
        String categoryId = "cat-1";
        String version = "2";


        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(codeListId, 2, agencyId),
            new ColecticaSetItem(categoryId, 2, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse("8b108ef8-b642-4484-9c49-f88e4bf7cf1d", agencyId, 2, codeListId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("fa1d4dca-f6dc-4d80-8b94-de1063a64d6d", agencyId, 2, categoryId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        Ddi4CodeList mockCodeList = new Ddi4CodeList(Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2024-10-31T10:43:38"),
                "urn:ddi:fr.insee:" + codeListId + ":2",
                agencyId, codeListId, "2",
                LangStrings.of("fr-FR", "NAF rév. 2"), List.of());
        Ddi4Category mockCategory = new Ddi4Category(Ddi4Category.TYPE,
                CogsDate.ofDateTime("2024-10-31T10:43:38"),
                "urn:ddi:fr.insee:" + categoryId + ":2",
                agencyId, categoryId, "2",
                LangStrings.of("fr-FR", "Agriculture"));
        Ddi4Response mockDdi4Response = new Ddi4Response("ddi:4.0",
                List.of(Reference.of(agencyId, codeListId, "2", "CodeList")),
                List.of(), List.of(), List.of(),
                List.of(mockCodeList), List.of(mockCategory));
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(mockDdi4Response);

        Ddi4Response result = ddiRepository.getCodeList(agencyId, codeListId, version);

        assertNotNull(result);
        assertEquals(1, result.codeList().size());
        assertEquals(codeListId, result.codeList().get(0).id());
        assertEquals(1, result.category().size());
        verify(colecticaClient).getSet(eq(agencyId), eq(codeListId), eq(version));
        verify(colecticaClient).getDescriptions(anyList());
    }

    @Test
    void getCodeListXml_returnsMultiFragmentFragmentInstance() {
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.insee";
        String codeListId = "cl-1";
        String categoryId = "cat-1";


        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(codeListId, 1, agencyId),
            new ColecticaSetItem(categoryId, 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse("8b108ef8-b642-4484-9c49-f88e4bf7cf1d", agencyId, 1, codeListId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList xmlns=\"ddi:logicalproduct:3_3\"/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse("fa1d4dca-f6dc-4d80-8b94-de1063a64d6d", agencyId, 1, categoryId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category xmlns=\"ddi:logicalproduct:3_3\"/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        String xml = ddiRepository.getCodeListXml(agencyId, codeListId, null);

        assertNotNull(xml);
        assertTrue(xml.contains("FragmentInstance"));
        assertTrue(xml.contains("<CodeList"));
        assertTrue(xml.contains("<Category"));
        verify(colecticaClient).getSet(eq(agencyId), eq(codeListId), any());
    }

    @Test
    void getCodeListXml_returnsNullWhenSetEmpty() {
        String baseApiUrl = "http://localhost:8082/api/v1/";
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(new ColecticaSetItem[0]);

        assertNull(ddiRepository.getCodeListXml("fr.insee", "unknown", null));
    }

    @Test
    void getCodeList_returnsNull_whenRootIsNotCodeList() {
        // #493 : un {id} pointant vers un autre type (ici PhysicalInstance) ne renvoie aucun contenu.
        String agencyId = "fr.insee";
        String id = "not-a-codelist";
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        when(colecticaClient.getSet(anyString(), anyString(), any()))
            .thenReturn(new ColecticaSetItem[] { new ColecticaSetItem(id, 1, agencyId) });
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(PHYSICAL_INSTANCE_TYPE, agencyId, 1, id,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        });

        assertNull(ddiRepository.getCodeList(agencyId, id, null));
        verify(ddi3ToDdi4Converter, never()).convertDdi3ToDdi4(any(), any());
    }

    @Test
    void getCodeListXml_returnsNull_whenRootIsNotCodeList() {
        // #493 : même garde de type sur la variante XML.
        String agencyId = "fr.insee";
        String id = "not-a-codelist";
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        when(colecticaClient.getSet(anyString(), anyString(), any()))
            .thenReturn(new ColecticaSetItem[] { new ColecticaSetItem(id, 1, agencyId) });
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(PHYSICAL_INSTANCE_TYPE, agencyId, 1, id,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null)
        });

        assertNull(ddiRepository.getCodeListXml(agencyId, id, null));
    }

    // ---- #447 : getDataRelationships / getDataRelationshipsXml (PhysicalInstance) ----

    @Test
    void getDataRelationships_keepsDataRelationshipAndReferencedVariables() {
        // #447 : la réponse /variables porte les DataRelationship ET les Variable référencées
        // (VariableUsedReference des VariablesInRecord), mais pas les CodeList/Category référencées.
        String agencyId = "fr.insee";
        String piId = "pi-1";
        String piType = "a51e85bb-6259-4488-8df2-f08cb43485f8";
        String drType = "f39ff278-8500-45fe-a850-3906da2d242b";
        String variableType = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
        String categoryType = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", piType,
                "DataRelationship", drType,
                "Variable", variableType,
                "CodeList", codeListType,
                "Category", categoryType
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(piId, 1, agencyId),
            new ColecticaSetItem("dr-1", 1, agencyId),
            new ColecticaSetItem("var-1", 1, agencyId),
            new ColecticaSetItem("cl-1", 1, agencyId),
            new ColecticaSetItem("cat-1", 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse(piType, agencyId, 1, piId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(drType, agencyId, 1, "dr-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><DataRelationship/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(variableType, agencyId, 1, "var-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Variable/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(codeListType, agencyId, 1, "cl-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(categoryType, agencyId, 1, "cat-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        Ddi4Response mockDdi4 = new Ddi4Response("ddi:4.0", null, List.of(),
                List.of(), List.of(), List.of(), List.of());

        ArgumentCaptor<Ddi3Response> captor = ArgumentCaptor.forClass(Ddi3Response.class);
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(captor.capture(), eq("ddi:4.0"))).thenReturn(mockDdi4);

        ddiRepository.getDataRelationships(agencyId, piId, null);

        // DataRelationship + Variable sont convertis ; PI, CodeList et Category sont écartés.
        List<Ddi3Response.Ddi3Item> converted = captor.getValue().items();
        assertEquals(2, converted.size());
        assertTrue(converted.stream().anyMatch(i -> drType.equals(i.itemType())));
        assertTrue(converted.stream().anyMatch(i -> variableType.equals(i.itemType())));
        assertTrue(converted.stream().noneMatch(i -> piType.equals(i.itemType())));
        assertTrue(converted.stream().noneMatch(i -> codeListType.equals(i.itemType())));
        assertTrue(converted.stream().noneMatch(i -> categoryType.equals(i.itemType())));
    }

    @Test
    void getDataRelationships_setsPhysicalInstanceTopLevelReference() {
        // #494 : le TopLevelReference de /variables doit pointer la PhysicalInstance interrogée,
        // pas rester null (la PI est écartée de la sortie mais reste la racine du FragmentInstance).
        String agencyId = "fr.insee";
        String piId = "pi-1";
        String piType = "a51e85bb-6259-4488-8df2-f08cb43485f8";
        String drType = "f39ff278-8500-45fe-a850-3906da2d242b";
        String variableType = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", piType,
                "DataRelationship", drType,
                "Variable", variableType
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(piId, 3, agencyId),
            new ColecticaSetItem("dr-1", 3, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse(piType, agencyId, 3, piId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(drType, agencyId, 3, "dr-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><DataRelationship/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        // Le convertisseur renvoie un TopLevelReference null (comportement réel : il ne le dérive
        // que pour les items PhysicalInstance, absents de la sortie /variables).
        Ddi4Response mockDdi4 = new Ddi4Response("ddi:4.0", null, null, List.of(), List.of(), null, null);
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0"))).thenReturn(mockDdi4);

        Ddi4Response result = ddiRepository.getDataRelationships(agencyId, piId, null);

        assertNotNull(result.topLevelReference());
        assertEquals(1, result.topLevelReference().size());
        Reference tlr = result.topLevelReference().get(0);
        assertEquals("PhysicalInstance", tlr.type());
        assertEquals(agencyId, tlr.agency());
        assertEquals(piId, tlr.id());
        assertEquals("3", tlr.version());
    }

    @Test
    void getCodeList_setsCodeListTopLevelReference() {
        // #494 : le TopLevelReference de /codelist doit pointer la CodeList interrogée, pas null.
        String agencyId = "fr.insee";
        String codeListId = "cl-1";
        String categoryId = "cat-1";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
        String categoryType = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "CodeList", codeListType,
                "Category", categoryType
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(codeListId, 5, agencyId),
            new ColecticaSetItem(categoryId, 5, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse(codeListType, agencyId, 5, codeListId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(categoryType, agencyId, 5, categoryId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Category/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        Ddi4Response mockDdi4 = new Ddi4Response("ddi:4.0", null, null, null, null, List.of(), List.of());
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0"))).thenReturn(mockDdi4);

        Ddi4Response result = ddiRepository.getCodeList(agencyId, codeListId, null);

        assertNotNull(result.topLevelReference());
        assertEquals(1, result.topLevelReference().size());
        Reference tlr = result.topLevelReference().get(0);
        assertEquals("CodeList", tlr.type());
        assertEquals(agencyId, tlr.agency());
        assertEquals(codeListId, tlr.id());
        assertEquals("5", tlr.version());
    }

    @Test
    void getDataRelationshipsXml_includesDataRelationshipAndVariableFragmentsOnly() {
        String agencyId = "fr.insee";
        String piId = "pi-1";
        String piType = "a51e85bb-6259-4488-8df2-f08cb43485f8";
        String drType = "f39ff278-8500-45fe-a850-3906da2d242b";
        String variableType = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "PhysicalInstance", piType,
                "DataRelationship", drType,
                "Variable", variableType,
                "CodeList", codeListType
        ));

        ColecticaSetItem[] setItems = {
            new ColecticaSetItem(piId, 1, agencyId),
            new ColecticaSetItem("dr-1", 1, agencyId),
            new ColecticaSetItem("var-1", 1, agencyId),
            new ColecticaSetItem("cl-1", 1, agencyId)
        };
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(setItems);

        ColecticaItemResponse[] itemResponses = {
            new ColecticaItemResponse(piType, agencyId, 1, piId,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><PhysicalInstance/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(drType, agencyId, 1, "dr-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><DataRelationship xmlns=\"ddi:logicalproduct:3_3\"/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(variableType, agencyId, 1, "var-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><Variable/></Fragment>",
                    null, null, false, false, false, null),
            new ColecticaItemResponse(codeListType, agencyId, 1, "cl-1",
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null)
        };
        when(colecticaClient.getDescriptions(anyList())).thenReturn(itemResponses);

        String xml = ddiRepository.getDataRelationshipsXml(agencyId, piId, null);

        assertNotNull(xml);
        assertTrue(xml.contains("FragmentInstance"));
        assertTrue(xml.contains("<DataRelationship"));
        assertTrue(xml.contains("<Variable"));
        assertFalse(xml.contains("<PhysicalInstance"));
        assertFalse(xml.contains("<CodeList"));
    }

    @Test
    void getDataRelationships_returnsNull_whenRootIsNotPhysicalInstance() {
        // #493 : /variables sur un {id} qui n'est pas une PhysicalInstance (ici CodeList) ne renvoie rien.
        String agencyId = "fr.insee";
        String id = "not-a-pi";
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        when(colecticaClient.getSet(anyString(), anyString(), any()))
            .thenReturn(new ColecticaSetItem[] { new ColecticaSetItem(id, 1, agencyId) });
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(CODE_LIST_TYPE, agencyId, 1, id,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null)
        });

        assertNull(ddiRepository.getDataRelationships(agencyId, id, null));
        verify(ddi3ToDdi4Converter, never()).convertDdi3ToDdi4(any(), any());
    }

    @Test
    void getDataRelationshipsXml_returnsNull_whenRootIsNotPhysicalInstance() {
        // #493 : même garde de type sur la variante XML.
        String agencyId = "fr.insee";
        String id = "not-a-pi";
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());

        when(colecticaClient.getSet(anyString(), anyString(), any()))
            .thenReturn(new ColecticaSetItem[] { new ColecticaSetItem(id, 1, agencyId) });
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(CODE_LIST_TYPE, agencyId, 1, id,
                    "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList/></Fragment>",
                    null, null, false, false, false, null)
        });

        assertNull(ddiRepository.getDataRelationshipsXml(agencyId, id, null));
    }

    @Test
    void getLogicalProductsByGroup_keepsOnlyLogicalProductsReferencedByGroup() {
        String agencyId = "fr.insee";
        String groupId = "group-1";
        String lpType = "965c8d28-7d48-4950-bea7-04b27e52bb9b";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("LogicalProduct", lpType));

        // Colectica returns only the LogicalProduct directly referenced by the group, filtered
        // server-side by item type.
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, groupId)),
                eq(List.of(lpType))))
            .thenReturn(List.of(new ItemReference(agencyId, "lp-1")));

        // Repository-wide LogicalProduct query carries the labels. lp-2 exists globally but is not
        // referenced by the group, so it must be filtered out.
        ColecticaItem lp1 = new ColecticaItem(
            null, Map.of("fr-FR", "Produit Logique 1"), Map.of("fr-FR", "Produit Logique 1"),
            null, null, 0, "test-repo", true, List.of(), "LogicalProduct", agencyId, 1, "lp-1",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 1L, 0);
        ColecticaItem lp2 = new ColecticaItem(
            null, Map.of("fr-FR", "Produit hors groupe"), Map.of("fr-FR", "Produit hors groupe"),
            null, null, 0, "test-repo", true, List.of(), "LogicalProduct", agencyId, 1, "lp-2",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 2L, 0);
        when(colecticaClient.query(anyList()))
                .thenReturn(new ColecticaResponse(List.of(lp1, lp2), 2, 2, null, null, null));

        List<PartialLogicalProduct> result = ddiRepository.getLogicalProductsByGroup(agencyId, groupId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("lp-1", result.get(0).id());
        assertEquals("Produit Logique 1", result.get(0).label());
    }

    @Test
    void getLogicalProductsByGroup_returnsEmptyWhenGroupHasNoLogicalProduct() {
        String agencyId = "fr.insee";
        String groupId = "group-empty";
        String lpType = "965c8d28-7d48-4950-bea7-04b27e52bb9b";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("LogicalProduct", lpType));
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, groupId)),
                eq(List.of(lpType))))
            .thenReturn(List.of());

        List<PartialLogicalProduct> result = ddiRepository.getLogicalProductsByGroup(agencyId, groupId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCodeListSchemesByLogicalProduct_keepsOnlySchemesReferencedByLogicalProduct() {
        String baseApiUrl = "http://localhost:8082/api/v1/";
        String agencyId = "fr.insee";
        String logicalProductId = "lp-1";
        String clsType = "4193d389-b5ae-4368-b399-cd5a7ee3653c";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeListScheme", clsType));

        // Colectica returns only the CodeListScheme directly referenced by the logical product,
        // filtered server-side by item type.
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, logicalProductId)),
                eq(List.of(clsType))))
            .thenReturn(List.of(new ItemReference(agencyId, "cls-1")));

        // Repository-wide CodeListScheme query carries the labels. cls-2 exists globally but is not
        // referenced by the logical product, so it must be filtered out.
        ColecticaItem cls1 = new ColecticaItem(
            null, Map.of("fr-FR", "Schéma 1"), Map.of("fr-FR", "Schéma 1"),
            null, null, 0, "test-repo", true, List.of(), "CodeListScheme", agencyId, 1, "cls-1",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 1L, 0);
        ColecticaItem cls2 = new ColecticaItem(
            null, Map.of("fr-FR", "Schéma hors LP"), Map.of("fr-FR", "Schéma hors LP"),
            null, null, 0, "test-repo", true, List.of(), "CodeListScheme", agencyId, 1, "cls-2",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 2L, 0);
        when(colecticaClient.query(anyList()))
                .thenReturn(new ColecticaResponse(List.of(cls1, cls2), 2, 2, null, null, null));

        List<PartialCodeListScheme> result = ddiRepository.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("cls-1", result.get(0).id());
        assertEquals("Schéma 1", result.get(0).label());
    }

    @Test
    void getCodeListSchemesByLogicalProduct_returnsEmptyWhenLogicalProductHasNoCodeListScheme() {
        String agencyId = "fr.insee";
        String logicalProductId = "lp-empty";
        String clsType = "4193d389-b5ae-4368-b399-cd5a7ee3653c";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeListScheme", clsType));
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, logicalProductId)),
                eq(List.of(clsType))))
            .thenReturn(List.of());

        List<PartialCodeListScheme> result = ddiRepository.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCodeListSchemes_returnsAllSchemesWithLabels() {
        String agencyId = "fr.insee";
        String clsType = "4193d389-b5ae-4368-b399-cd5a7ee3653c";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeListScheme", clsType));

        ColecticaItem cls1 = new ColecticaItem(
            null, Map.of("fr-FR", "Schéma 1"), Map.of("fr-FR", "Schéma 1"),
            null, null, 0, "test-repo", true, List.of(), "CodeListScheme", agencyId, 1, "cls-1",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 1L, 0);
        ColecticaItem cls2 = new ColecticaItem(
            null, Map.of("fr-FR", "Schéma 2"), Map.of("fr-FR", "Schéma 2"),
            null, null, 0, "test-repo", true, List.of(), "CodeListScheme", agencyId, 1, "cls-2",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 2L, 0);
        when(colecticaClient.query(eq(List.of(clsType))))
                .thenReturn(new ColecticaResponse(List.of(cls1, cls2), 2, 2, null, null, null));

        List<PartialCodeListScheme> result = ddiRepository.getCodeListSchemes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("cls-1", result.get(0).id());
        assertEquals("Schéma 1", result.get(0).label());
        assertEquals("cls-2", result.get(1).id());
    }

    @Test
    void getCodeListsByCodeListScheme_keepsOnlyCodeListsReferencedByScheme() {
        String agencyId = "fr.insee";
        String codeListSchemeId = "cls-1";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeList", codeListType));

        // Colectica returns only the CodeList directly referenced by the scheme, filtered server-side.
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, codeListSchemeId)),
                eq(List.of(codeListType))))
            .thenReturn(List.of(new ItemReference(agencyId, "code-list-1")));

        // Repository-wide CodeList query carries the labels. code-list-2 exists globally but is not
        // referenced by the scheme, so it must be filtered out.
        ColecticaItem codeList1 = new ColecticaItem(
            null, Map.of("fr-FR", "Liste 1"), Map.of("fr-FR", "Liste 1"),
            null, null, 0, "test-repo", true, List.of(), "CodeList", agencyId, 1, "code-list-1",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 1L, 0);
        ColecticaItem codeList2 = new ColecticaItem(
            null, Map.of("fr-FR", "Liste hors scheme"), Map.of("fr-FR", "Liste hors scheme"),
            null, null, 0, "test-repo", true, List.of(), "CodeList", agencyId, 1, "code-list-2",
            null, null, "2025-01-01T00:00:00", null, true, false, false, "DDI", 2L, 0);
        when(colecticaClient.query(anyList()))
                .thenReturn(new ColecticaResponse(List.of(codeList1, codeList2), 2, 2, null, null, null));

        List<PartialCodesList> result = ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("code-list-1", result.get(0).id());
        assertEquals("Liste 1", result.get(0).label());
    }

    @Test
    void getCodeListsByCodeListScheme_returnsEmptyWhenSchemeHasNoCodeList() {
        String agencyId = "fr.insee";
        String codeListSchemeId = "cls-empty";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeList", codeListType));
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference(agencyId, codeListSchemeId)),
                eq(List.of(codeListType))))
            .thenReturn(List.of());

        List<PartialCodesList> result = ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getVariablesUsingCodeList_returnsStudyUnitPhysicalInstanceVariableWithLabels() {
        String agencyId = "fr.insee";
        String codeListId = "cl-1";
        String variableType = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";
        String dataRelationshipType = "f39ff278-8500-45fe-a850-3906da2d242b";
        String physicalInstanceType = "a51e85bb-6259-4488-8df2-f08cb43485f8";
        String studyUnitType = STUDY_UNIT_ITEM_TYPE;

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "Variable", variableType,
                "DataRelationship", dataRelationshipType,
                "PhysicalInstance", physicalInstanceType,
                "StudyUnit", studyUnitType));

        // CodeList ← Variable ← DataRelationship ← PhysicalInstance ← StudyUnit.
        // Labels come from the /descriptions endpoint directly (findRelatedItems → ColecticaItem),
        // so no separate label query is made. DataRelationships are only intermediate (bare refs).
        when(colecticaClient.findRelatedItems(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference(agencyId, codeListId)),
                eq(List.of(variableType))))
            .thenReturn(List.of(labelItem(variableType, agencyId, "var-1", "Sexe")));
        when(colecticaClient.findRelatedDescriptions(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference(agencyId, "var-1")),
                eq(List.of(dataRelationshipType))))
            .thenReturn(List.of(new ItemReference(agencyId, "dr-1")));
        when(colecticaClient.findRelatedItems(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference(agencyId, "dr-1")),
                eq(List.of(physicalInstanceType))))
            .thenReturn(List.of(labelItem(physicalInstanceType, agencyId, "pi-1", "Fichier détail")));
        when(colecticaClient.findRelatedItems(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference(agencyId, "pi-1")),
                eq(List.of(studyUnitType))))
            .thenReturn(List.of(labelItem(studyUnitType, agencyId, "su-1", "Recensement 2024")));

        List<CodeListVariableUsage> result = ddiRepository.getVariablesUsingCodeList(agencyId, codeListId);

        assertNotNull(result);
        assertEquals(1, result.size());
        CodeListVariableUsage usage = result.get(0);
        assertEquals(agencyId, usage.studyUnitAgencyId());
        assertEquals("su-1", usage.studyUnitId());
        assertEquals("Recensement 2024", usage.studyUnitLabel());
        assertEquals(agencyId, usage.physicalInstanceAgencyId());
        assertEquals("pi-1", usage.physicalInstanceId());
        assertEquals("Fichier détail", usage.physicalInstanceLabel());
        assertEquals(agencyId, usage.variableAgencyId());
        assertEquals("var-1", usage.variableId());
        assertEquals("Sexe", usage.variableLabel());
    }

    @Test
    void getVariablesUsingCodeList_returnsEmptyWhenNoVariableUsesIt() {
        String agencyId = "fr.insee";
        String codeListId = "cl-unused";
        String variableType = "683889c6-f74b-4d5e-92ed-908c0a42bb2d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
                "Variable", variableType,
                "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
                "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "StudyUnit", STUDY_UNIT_ITEM_TYPE));
        when(colecticaClient.findRelatedItems(
                eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference(agencyId, codeListId)),
                eq(List.of(variableType))))
            .thenReturn(List.of());

        List<CodeListVariableUsage> result = ddiRepository.getVariablesUsingCodeList(agencyId, codeListId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private static ColecticaItem labelItem(String itemType, String agency, String id, String label) {
        return new ColecticaItem(
                null,                       // summary
                Map.of("fr-FR", label),     // itemName
                null,                       // label
                null,                       // description
                null,                       // versionRationale
                0,                          // metadataRank
                "test-repo",                // repositoryName
                true,                       // isAuthoritative
                List.of(),                  // tags
                itemType,                   // itemType
                agency,                     // agencyId
                1,                          // version
                id,                         // identifier
                null,                       // item
                null,                       // notes
                null,                       // versionDate
                null,                       // versionResponsibility
                true,                       // isPublished
                false,                      // isDeprecated
                false,                      // isProvisional
                "DDI",                      // itemFormat
                1L,                         // transactionId
                0                           // versionCreationType
        );
    }

    private static final String STUDY_UNIT_ITEM_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";
    private static final String GROUP_ITEM_TYPE = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

    @Test
    void updateFullPhysicalInstance_attachesNonMutualizedCodeListsToGroupCodeListScheme() {
        // The converted PhysicalInstance produces at least one item so the save proceeds.
        Ddi3Response.Ddi3Item piItem = new Ddi3Response.Ddi3Item(
            "pi-type", "fr.insee", "1", "pi-1", "<pi/>", "2026-01-01T00:00:00",
            "resp", false, false, false, "fmt");
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(any()))
            .thenReturn(new Ddi3Response(new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")), List.of(piItem)));

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
            "LogicalProduct", "lp-type", "CodeListScheme", CODE_LIST_SCHEME_TYPE,
            "CodeListGroup", CODE_LIST_GROUP_TYPE, "CodeList", CODE_LIST_TYPE));

        // Mutualized package: walking it top-down (scheme → group → code list) reaches CL_MUT only;
        // CL_NEW is not part of the package tree.
        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef("fr.insee", "PKG", 1));
        stubChildren("fr.insee", "PKG", CODE_LIST_SCHEME_TYPE, new ItemReference("fr.insee", "SCHEME_M"));
        stubChildren("fr.insee", "SCHEME_M", CODE_LIST_GROUP_TYPE, new ItemReference("fr.insee", "GROUP_M"));
        stubChildren("fr.insee", "GROUP_M", CODE_LIST_TYPE, new ItemReference("fr.insee", "CL_MUT"));

        // Parents: PI -> StudyUnit -> Group
        when(colecticaClient.findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference("fr.insee", "pi-1")), eq(List.of(STUDY_UNIT_ITEM_TYPE))))
            .thenReturn(List.of(new ItemReference("fr.insee", "su-1")));
        when(colecticaClient.findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT),
                eq(new ItemReference("fr.insee", "su-1")), eq(List.of(GROUP_ITEM_TYPE))))
            .thenReturn(List.of(new ItemReference("fr.insee", "group-1")));

        // Scheme resolution: Group -> LogicalProduct -> CodeListScheme
        when(colecticaClient.findRelatedDescriptions(eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference("fr.insee", "group-1")), eq(List.of("lp-type"))))
            .thenReturn(List.of(new ItemReference("fr.insee", "lp-1")));
        when(colecticaClient.findRelatedDescriptions(eq(RelationshipDirection.BY_SUBJECT),
                eq(new ItemReference("fr.insee", "lp-1")), eq(List.of(CODE_LIST_SCHEME_TYPE))))
            .thenReturn(List.of(new ItemReference("fr.insee", "CLS_1")));

        // Existing scheme already references CL_EXISTING.
        ColecticaItemResponse existingScheme = new ColecticaItemResponse(
            "cls-type", "fr.insee", 1, "CLS_1", "<scheme/>", "2026-01-01T00:00:00",
            "resp", false, false, false, "fmt");
        when(colecticaClient.getItem("fr.insee", "CLS_1", null)).thenReturn(existingScheme);
        Ddi4CodeListScheme parsedScheme = new Ddi4CodeListScheme(Ddi4CodeListScheme.TYPE,
            CogsDate.ofDateTime("2026-01-01T00:00:00"), "urn:ddi:fr.insee:CLS_1:1",
            "fr.insee", "CLS_1", "1", LangStrings.of("fr-FR", "Scheme"),
            new java.util.ArrayList<>(List.of(Reference.of("fr.insee", "CL_EXISTING", "1", "CodeList"))));
        when(ddi3ToDdi4Converter.toCodeListScheme("<scheme/>")).thenReturn(parsedScheme);

        Ddi3Response.Ddi3Item schemeItem = new Ddi3Response.Ddi3Item(
            "cls-type", "fr.insee", "1", "CLS_1", "<scheme-updated/>", "2026-01-01T00:00:00",
            "resp", false, false, false, "fmt");
        ArgumentCaptor<Ddi4CodeListScheme> schemeCaptor = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        when(ddi4ToDdi3Converter.toCodeListSchemeItem(schemeCaptor.capture())).thenReturn(schemeItem);

        Ddi4CodeList clMut = new Ddi4CodeList(Ddi4CodeList.TYPE, CogsDate.ofDateTime("2026-01-01T00:00:00"),
            "urn:ddi:fr.insee:CL_MUT:1", "fr.insee", "CL_MUT", "1", LangStrings.of("fr-FR", "mut"), null);
        Ddi4CodeList clNew = new Ddi4CodeList(Ddi4CodeList.TYPE, CogsDate.ofDateTime("2026-01-01T00:00:00"),
            "urn:ddi:fr.insee:CL_NEW:1", "fr.insee", "CL_NEW", "1", LangStrings.of("fr-FR", "new"), null);
        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, null, List.of(clMut, clNew), null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // The merged scheme keeps CL_EXISTING and adds the non-mutualized CL_NEW, but not CL_MUT.
        assertThat(schemeCaptor.getValue().codeListReference())
            .extracting(Reference::id)
            .containsExactlyInAnyOrder("CL_EXISTING", "CL_NEW");

        ArgumentCaptor<ColecticaCreateItemRequest> reqCaptor =
            ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
        verify(colecticaClient).createOrUpdateItems(reqCaptor.capture());
        assertThat(reqCaptor.getValue().items())
            .extracting(ColecticaItemResponse::identifier)
            .contains("CLS_1");
    }

    @Test
    void updateFullPhysicalInstance_doesNotTouchSchemeWhenAllCodeListsAreMutualized() {
        Ddi3Response.Ddi3Item piItem = new Ddi3Response.Ddi3Item(
            "pi-type", "fr.insee", "1", "pi-1", "<pi/>", "2026-01-01T00:00:00",
            "resp", false, false, false, "fmt");
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(any()))
            .thenReturn(new Ddi3Response(new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")), List.of(piItem)));

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of(
            "CodeListScheme", CODE_LIST_SCHEME_TYPE,
            "CodeListGroup", CODE_LIST_GROUP_TYPE, "CodeList", CODE_LIST_TYPE));

        // CL_MUT belongs to the package tree (package → scheme → group → CL_MUT).
        when(colecticaConfiguration.mutualizedCodesPackage())
            .thenReturn(new ColecticaConfiguration.PackageRef("fr.insee", "PKG", 1));
        stubChildren("fr.insee", "PKG", CODE_LIST_SCHEME_TYPE, new ItemReference("fr.insee", "SCHEME_M"));
        stubChildren("fr.insee", "SCHEME_M", CODE_LIST_GROUP_TYPE, new ItemReference("fr.insee", "GROUP_M"));
        stubChildren("fr.insee", "GROUP_M", CODE_LIST_TYPE, new ItemReference("fr.insee", "CL_MUT"));

        Ddi4CodeList clMut = new Ddi4CodeList(Ddi4CodeList.TYPE, CogsDate.ofDateTime("2026-01-01T00:00:00"),
            "urn:ddi:fr.insee:CL_MUT:1", "fr.insee", "CL_MUT", "1", LangStrings.of("fr-FR", "mut"), null);
        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, null, List.of(clMut), null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // No scheme resolution, no scheme item: only the converted items are sent.
        verify(colecticaClient, never()).getItem(anyString(), anyString(), any());
        verify(ddi4ToDdi3Converter, never()).toCodeListSchemeItem(any());
        ArgumentCaptor<ColecticaCreateItemRequest> reqCaptor =
            ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
        verify(colecticaClient).createOrUpdateItems(reqCaptor.capture());
        assertThat(reqCaptor.getValue().items())
            .extracting(ColecticaItemResponse::identifier)
            .containsExactly("pi-1");
    }

}