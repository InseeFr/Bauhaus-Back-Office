package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.StudyUnitNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        ddiRepository = new DDIRepositoryImpl(
                instanceConfiguration,
                ddi3ToDdi4Converter,
                ddi4ToDdi3Converter,
                colecticaConfiguration,
                colecticaClient,
                refsProvider);
    }

    @Test
    void shouldGetPhysicalInstances() {
        // Given
        Map<String, String> itemTypes = Map.of("PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8");

        ColecticaItem item1 = colecticaItem(
                "PhysicalInstance",
                "agency1",
                1,
                "pi-1",
                Map.of("fr-FR", "Instance Physique 1", "en", "Physical Instance 1"),
                Map.of("fr-FR", "Label 1", "en", "Label 1"),
                "2025-01-01T00:00:00",
                true,
                1L);

        ColecticaItem item2 = colecticaItem(
                "PhysicalInstance",
                "agency2",
                1,
                "pi-2",
                Map.of("fr-FR", "Instance Physique 2", "en", "Physical Instance 2"),
                Map.of("fr-FR", "Label 2", "en", "Label 2"),
                null,
                true,
                2L);

        ColecticaResponse mockResponse = queryResponse(item1, item2);

        // Mock configuration
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);

        // Mock query call
        when(colecticaClient.query(anyList())).thenReturn(mockResponse);

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstances();

        // Then
        assertFirstOfTwoPhysicalInstances(result, "2025-01-01 00:00:00");
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Instance Physique 2", result.get(1).label());
        assertEquals("agency2", result.get(1).agency());
        assertNull(result.get(1).versionDate());

        // Verify query was issued for the PhysicalInstance item type
        verify(colecticaClient).query(List.of("a51e85bb-6259-4488-8df2-f08cb43485f8"));
    }

    @Test
    void shouldGetPhysicalInstancesViaAdvancedQuery() {
        // Given the _query/advanced payload shape: label/date live in typed property bags,
        // versionDate carries sub-second precision that we truncate to seconds (like the legacy path).
        Map<String, String> itemTypes = Map.of("PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8");

        ColecticaAdvancedItem item1 = new ColecticaAdvancedItem(
                "agency1",
                "pi-1",
                1,
                "a51e85bb-6259-4488-8df2-f08cb43485f8",
                false,
                Map.of(
                        "dcTitle", List.of(new LocalizedText("Titre 1", "fr-FR")),
                        "label", List.of(new LocalizedText("Instance Physique 1", "fr-FR"))),
                Map.of("versionDate", List.of("2026-06-29T14:26:32.961778")),
                Map.of("isPublished", false));

        // No "label" key and no versionDate: label falls back to dcTitle, date stays null.
        ColecticaAdvancedItem item2 = new ColecticaAdvancedItem(
                "agency2",
                "pi-2",
                1,
                "a51e85bb-6259-4488-8df2-f08cb43485f8",
                false,
                Map.of("dcTitle", List.of(new LocalizedText("Titre 2", "fr-FR"))),
                Map.of(),
                Map.of("isPublished", false));

        ColecticaAdvancedResponse mockResponse = new ColecticaAdvancedResponse(List.of(item1, item2), 2, null);

        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
        when(colecticaClient.queryAdvanced(anyList())).thenReturn(mockResponse);

        // When
        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstancesViaAdvancedQuery();

        // Then
        assertFirstOfTwoPhysicalInstances(result, "2026-06-29 14:26:32");
        assertEquals("pi-2", result.get(1).id());
        assertEquals("Titre 2", result.get(1).label());
        assertNull(result.get(1).versionDate());

        verify(colecticaClient).queryAdvanced(List.of("a51e85bb-6259-4488-8df2-f08cb43485f8"));
    }

    @Test
    void shouldGetLogicalProducts() {
        // Given
        Map<String, String> itemTypes = Map.of("LogicalProduct", "965c8d28-7d48-4950-bea7-04b27e52bb9b");

        ColecticaItem item1 = colecticaItem(
                "LogicalProduct",
                "agency1",
                1,
                "lp-1",
                Map.of("fr-FR", "Produit Logique 1", "en", "Logical Product 1"),
                Map.of("fr-FR", "Label 1", "en", "Label 1"),
                "2025-01-01T00:00:00",
                true,
                1L);

        ColecticaItem item2 = colecticaItem(
                "LogicalProduct",
                "agency2",
                1,
                "lp-2",
                Map.of("fr-FR", "Produit Logique 2", "en", "Logical Product 2"),
                Map.of("fr-FR", "Label 2", "en", "Label 2"),
                null,
                true,
                2L);

        ColecticaResponse mockResponse = queryResponse(item1, item2);

        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
        when(colecticaClient.query(anyList())).thenReturn(mockResponse);

        // When
        List<PartialLogicalProduct> result = ddiRepository.getLogicalProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("lp-1", result.get(0).id());
        assertEquals("Produit Logique 1", result.get(0).label());
        assertEquals("agency1", result.get(0).agency());
        assertEquals("2025-01-01 00:00:00", utcSeconds(result.get(0).versionDate()));
        assertEquals("lp-2", result.get(1).id());
        assertEquals("Produit Logique 2", result.get(1).label());
        assertEquals("agency2", result.get(1).agency());
        assertNull(result.get(1).versionDate());

        verify(colecticaClient).query(List.of("965c8d28-7d48-4950-bea7-04b27e52bb9b"));
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        // Given
        String instanceId = "2514afe4-7b08-4500-be25-7a852a10fd8c";
        String agencyId = "fr.inserm.constances";
        int version = 1;

        stubSet(
                new ColecticaSetItem(instanceId, version, agencyId),
                new ColecticaSetItem("var-1", 1, agencyId),
                new ColecticaSetItem("dr-123", 1, agencyId));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, version, instanceId, fragment("PhysicalInstance")),
                description(VARIABLE_TYPE, agencyId, 1, "var-1", fragment("Variable")),
                description(DATA_RELATIONSHIP_TYPE, agencyId, 1, "dr-123", fragment("DataRelationship")));

        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-10-23T12:28:43.615773Z"),
                "urn:ddi:fr.inserm.constances:2514afe4-7b08-4500-be25-7a852a10fd8c:1",
                agencyId,
                instanceId,
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Radon et gamma")),
                null);

        stubDdi3ToDdi4Conversion(physicalInstanceResponse(agencyId, instanceId, mockPhysicalInstance));

        // When
        Ddi4Response result = ddiRepository.getPhysicalInstance(agencyId, instanceId);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());
        assertEquals(instanceId, result.physicalInstance().get(0).id());
        assertEquals(agencyId, result.physicalInstance().get(0).agency());
        assertEquals(
                "Radon et gamma",
                result.physicalInstance().get(0).citation().title().get(0).value());

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
                null);

        // Mock configuration
        stubCreationConfiguration(creationItemTypesWithStudyUnit());

        // Mock StudyUnit fetch
        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\" isUniversallyUnique=\"true\" versionDate=\"2025-01-01T00:00:00\"/>"
                + "</Fragment>";
        when(colecticaClient.getItem(anyString(), anyString(), any()))
                .thenReturn(studyUnitToUpdate(studyUnitAgency, 2, studyUnitId, studyUnitXml));

        // Mock item creation
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");

        // Mock set and _getList responses for getPhysicalInstance call after creation
        stubSet(new ColecticaSetItem("test-id", 1, "fr.insee"));

        stubDescriptions(description(PHYSICAL_INSTANCE_TYPE, "fr.insee", 1, "test-id", fragment("PhysicalInstance")));
        // Mock converter
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:test-id:1",
                "fr.insee",
                "test-id",
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", physicalInstanceLabel)),
                null);

        stubDdi3ToDdi4Conversion(physicalInstanceResponse("fr.insee", "test-id", mockPhysicalInstance));

        // When
        Ddi4Response result = ddiRepository.createPhysicalInstance(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.physicalInstance());
        assertEquals(1, result.physicalInstance().size());
        assertEquals(
                physicalInstanceLabel,
                result.physicalInstance().get(0).citation().title().get(0).value());

        // Verify item creation was sent through the SDK
        ColecticaCreateItemRequest createRequest = sentRequest();
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
        String studyUnitId = "su-uuid-existing";
        String studyUnitAgency = "fr.insee";
        String existingPiId = "existing-pi-uuid";

        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
                "New PI", "New DR", "New LR", studyUnitId, studyUnitAgency, null, null);

        stubCreationConfiguration(creationItemTypesWithStudyUnit());

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
        when(colecticaClient.getItem(anyString(), anyString(), any()))
                .thenReturn(studyUnitToUpdate(studyUnitAgency, 3, studyUnitId, studyUnitXml));

        stubSaveWithEmptyReload();

        // When
        ddiRepository.createPhysicalInstance(request);

        // Then: the StudyUnit item sent contains both the existing and the new PhysicalInstanceReference
        ColecticaCreateItemRequest createRequest = sentRequest();

        ColecticaItemResponse suItem = createRequest.items().get(2);
        // Each PhysicalInstanceReference element contributes one open + one close tag = 2 occurrences per reference
        assertEquals(4, suItem.item().split("PhysicalInstanceReference").length - 1); // two references × 2 tags each
        assertTrue(suItem.item().contains(existingPiId)); // existing reference preserved
    }

    @Test
    void shouldStampConfiguredVersionResponsibilityOnCreatedItems() {
        assertThat(createPhysicalInstanceWithConfiguredVersionResponsibility())
                .isNotEmpty()
                .allSatisfy(item -> assertThat(item.versionResponsibility()).isEqualTo("responsable-configure"));
    }

    @Test
    void shouldWriteConfiguredVersionResponsibilityInTheXmlOfCreatedItems() {
        assertThat(createPhysicalInstanceWithConfiguredVersionResponsibility())
                .isNotEmpty()
                .allSatisfy(item -> assertThat(item.item())
                        .contains("<r:VersionResponsibility>responsable-configure</r:VersionResponsibility>"));
    }

    /** Creates a PhysicalInstance with a configured version responsibility and returns the items sent. */
    private List<ColecticaItemResponse> createPhysicalInstanceWithConfiguredVersionResponsibility() {
        CreatePhysicalInstanceRequest request =
                new CreatePhysicalInstanceRequest("New PI", "New DR", "New LR", null, null, null, null);

        stubCreationConfiguration(creationItemTypes());
        when(instanceConfiguration.versionResponsibility()).thenReturn("responsable-configure");
        stubSaveWithEmptyReload();

        ddiRepository.createPhysicalInstance(request);

        return sentItems();
    }

    @Test
    void shouldUseTheIdentifiersProvidedByTheCallerInsteadOfGeneratingThem() {
        CreatePhysicalInstanceRequest request =
                new CreatePhysicalInstanceRequest("New PI", "New DR", "New LR", null, null, null, null);
        PhysicalInstanceIds imposedIds = new PhysicalInstanceIds("pi-impose", "dr-impose", "lr-impose");

        stubCreationConfiguration(creationItemTypes());
        stubSaveWithEmptyReload();

        ddiRepository.createPhysicalInstance(request, imposedIds);

        List<ColecticaItemResponse> items = sentItems();

        ColecticaItemResponse physicalInstance = items.get(0);
        ColecticaItemResponse dataRelationship = items.get(1);
        assertThat(physicalInstance.identifier()).isEqualTo("pi-impose");
        assertThat(dataRelationship.identifier()).isEqualTo("dr-impose");
        // La PhysicalInstance référence la DataRelationship, qui porte elle-même le LogicalRecord.
        assertThat(physicalInstance.item()).contains("dr-impose");
        assertThat(dataRelationship.item()).contains("lr-impose");
    }

    @Test
    void shouldUpdatePhysicalInstance() {
        // Given
        String instanceId = "test-pi-id";
        String agencyId = "fr.insee";

        String newLabel = "Updated Physical Instance Label";
        String newDataRelationshipLabel = "Updated Data Relationship Label";
        String newLogicalRecordLabel = "Updated LogicalRecord Label";
        UpdatePhysicalInstanceRequest updateRequest =
                new UpdatePhysicalInstanceRequest(newLabel, newDataRelationshipLabel, newLogicalRecordLabel);

        // Mock set, _getList and converter responses for getPhysicalInstance call
        stubExistingPhysicalInstanceWithDataRelationship(agencyId, instanceId);

        // Mock DDI4 to DDI3 conversion for updateFullPhysicalInstance
        Ddi3Response.Ddi3Item mockPiDdi3Item = legacyDdi3Item(
                PHYSICAL_INSTANCE_TYPE,
                agencyId,
                "1",
                instanceId,
                "<PhysicalInstance>" + newLabel + "</PhysicalInstance>",
                true);
        Ddi3Response.Ddi3Item mockDrDdi3Item = legacyDdi3Item(
                DATA_RELATIONSHIP_TYPE,
                agencyId,
                "1",
                "dr-123",
                "<DataRelationship>" + newDataRelationshipLabel + "</DataRelationship>",
                true);
        Ddi3Response mockDdi3Response = new Ddi3Response(null, List.of(mockPiDdi3Item, mockDrDdi3Item));
        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture())).thenReturn(mockDdi3Response);

        // Mock item update
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");

        // When
        OffsetDateTime beforeUpdate = OffsetDateTime.now();
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then
        // Version of the objects sent to Colectica must NOT be incremented
        Ddi4Response capturedDdi4 = ddi4Captor.getValue();
        assertEquals("1", capturedDdi4.physicalInstance().get(0).version()); // version preserved, not incremented
        assertEquals("1", capturedDdi4.dataRelationship().get(0).version()); // version preserved, not incremented

        // versionDate must be restamped to now() on both the PI and the DR (Colectica never fills it)
        OffsetDateTime piVersionDate = OffsetDateTime.parse(
                capturedDdi4.physicalInstance().get(0).versionDate().dateTime());
        OffsetDateTime drVersionDate = OffsetDateTime.parse(
                capturedDdi4.dataRelationship().get(0).versionDate().dateTime());
        assertThat(piVersionDate).isBetween(beforeUpdate, OffsetDateTime.now());
        assertThat(drVersionDate).isBetween(beforeUpdate, OffsetDateTime.now());

        ColecticaCreateItemRequest createRequest = sentRequest();
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
    void shouldAttachPhysicalInstanceToStudyUnitWhenUpdateCarriesStudyUnit() {
        // Given a PATCH that carries a StudyUnit (duplication workflow, cf. #1555):
        // the PI must be attached to that StudyUnit so that GET .../parents can resolve
        // its Group & Study afterwards.
        String instanceId = "duplicated-pi-id";
        String agencyId = "fr.insee";
        String studyUnitId = "su-target";
        String studyUnitAgency = "fr.insee";

        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Dup PI (copy)", "Dup DR", "Dup LR", studyUnitId, studyUnitAgency, "group-1", "fr.insee");

        // Mock getPhysicalInstance (getSet + _getList + converter), mirroring shouldUpdatePhysicalInstance
        stubExistingPhysicalInstanceWithDataRelationship(agencyId, instanceId);

        Ddi3Response.Ddi3Item mockPiDdi3Item = legacyDdi3Item(
                PHYSICAL_INSTANCE_TYPE,
                agencyId,
                "1",
                instanceId,
                "<PhysicalInstance>Dup PI (copy)</PhysicalInstance>",
                false);
        Ddi3Response.Ddi3Item mockDrDdi3Item = legacyDdi3Item(
                DATA_RELATIONSHIP_TYPE, agencyId, "1", "dr-123", "<DataRelationship>Dup DR</DataRelationship>", false);
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(any()))
                .thenReturn(new Ddi3Response(null, List.of(mockPiDdi3Item, mockDrDdi3Item)));

        // StudyUnit fetched by addPhysicalInstanceReferenceToStudyUnit
        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\" isUniversallyUnique=\"true\"></StudyUnit>"
                + "</Fragment>";
        when(colecticaClient.getItem(anyString(), anyString(), any()))
                .thenReturn(studyUnitToUpdate(studyUnitAgency, 2, studyUnitId, studyUnitXml));
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("StudyUnit", "30ea0200-7121-4f01-8d21-a931a182b86d"));

        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");

        // When
        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // Then: the saved batch includes the StudyUnit carrying a PhysicalInstanceReference to the PI
        ColecticaItemResponse savedStudyUnit = sentItems().stream()
                .filter(item -> "30ea0200-7121-4f01-8d21-a931a182b86d".equals(item.itemType()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No StudyUnit item sent to Colectica during PATCH"));
        assertTrue(savedStudyUnit.item().contains("PhysicalInstanceReference"));
        assertTrue(savedStudyUnit.item().contains(instanceId));
    }

    @Test
    void getPhysicalInstance_skipsCodeListAndCategoryItemTypes() {
        // Les CodeList et Category sont volontairement omises de la réponse GET PI
        // (chargées paresseusement au clic d'une variable) pour réduire le payload
        // et le coût de conversion DDI3 -> DDI4 sur ce endpoint.
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";

        when(instanceConfiguration.itemTypes()).thenReturn(physicalInstanceItemTypesWithCodeLists());

        stubSet(
                new ColecticaSetItem(instanceId, 1, agencyId),
                new ColecticaSetItem("2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", 1, agencyId),
                new ColecticaSetItem("795aa4b8-acec-4ef8-8f08-3a200c7bdb10", 1, agencyId),
                new ColecticaSetItem("2f70f505-4a9e-4abe-82d4-c4ddfed25d52", 1, agencyId),
                new ColecticaSetItem("d363a730-14d4-4c54-9464-982312cf9330", 1, agencyId));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")),
                description(VARIABLE_TYPE, agencyId, 1, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", fragment("Variable")),
                description(
                        DATA_RELATIONSHIP_TYPE,
                        agencyId,
                        1,
                        "795aa4b8-acec-4ef8-8f08-3a200c7bdb10",
                        fragment("DataRelationship")),
                description(CODE_LIST_TYPE, agencyId, 1, "2f70f505-4a9e-4abe-82d4-c4ddfed25d52", fragment("CodeList")),
                description(CATEGORY_TYPE, agencyId, 1, "d363a730-14d4-4c54-9464-982312cf9330", fragment("Category")));

        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:14.251595Z"),
                "urn:ddi:fr.insee:32799021-0663-41cd-aca6-3ad8dbdae3e3:1",
                agencyId,
                instanceId,
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "test")),
                List.of(Reference.of(agencyId, "795aa4b8-acec-4ef8-8f08-3a200c7bdb10", "1", "DataRelationship")));

        Ddi4Variable mockVariable = new Ddi4Variable(
                Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:33.138Z"),
                "urn:ddi:fr.insee:2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d:1",
                agencyId,
                "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d",
                "1",
                null,
                LangStrings.of("fr-FR", "name"),
                LangStrings.of("fr-FR", "Test Label"),
                null,
                new VariableRepresentation(
                        null,
                        new CodeRepresentation(
                                CodeRepresentation.TYPE,
                                false,
                                Reference.of(agencyId, "2f70f505-4a9e-4abe-82d4-c4ddfed25d52", "1", "CodeList")),
                        null,
                        null,
                        null,
                        null),
                null);

        Ddi4DataRelationship mockDataRelationship = new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:14.251595Z"),
                "urn:ddi:fr.insee:795aa4b8-acec-4ef8-8f08-3a200c7bdb10:1",
                agencyId,
                "795aa4b8-acec-4ef8-8f08-3a200c7bdb10",
                "1",
                null,
                null,
                List.of(new LogicalRecord(
                        LogicalRecord.TYPE,
                        "urn:ddi:fr.insee:8585972f-2dc2-4125-87b2-60fd3f243cf3:1",
                        agencyId,
                        "8585972f-2dc2-4125-87b2-60fd3f243cf3",
                        "1",
                        null,
                        new VariablesInRecord(List.of(
                                Reference.of(agencyId, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", "1", "Variable"))))));

        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(mockDataRelationship),
                List.of(mockVariable),
                null,
                null,
                null);

        stubDdi3ToDdi4Conversion(mockDdi4Response);

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
        assertTrue(capturedDdi3.items().stream().noneMatch(i -> CODE_LIST_TYPE.equals(i.itemType())));
        assertTrue(capturedDdi3.items().stream().noneMatch(i -> CATEGORY_TYPE.equals(i.itemType())));
    }

    /**
     * Valeurs sentinelles (#1566) : les items ManagedMissingValuesRepresentation du set de la PI ne
     * sont PAS filtrés par le GET (contrairement aux CodeList/Category) — ils sont passés au
     * converter, qui les agrège dans la réponse DDI 4.
     */
    @Test
    void getPhysicalInstance_passesManagedMissingValuesItemsToConverter() {
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";
        String mmvrType = "c29c3125-2a53-4179-8fa6-aa3beb2bb5ed";

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
                        "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                        "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00",
                        "ManagedMissingValuesRepresentation", mmvrType));

        stubSet(new ColecticaSetItem(instanceId, 1, agencyId), new ColecticaSetItem("mmvr-1", 1, agencyId));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")),
                description(mmvrType, agencyId, 1, "mmvr-1", fragment("ManagedMissingValuesRepresentation")));

        stubDdi3ToDdi4Conversion(new Ddi4Response("ddi:4.0", null, null, null, null, null, null, null));

        ddiRepository.getPhysicalInstance(agencyId, instanceId);

        ArgumentCaptor<Ddi3Response> ddi3Captor = ArgumentCaptor.forClass(Ddi3Response.class);
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(ddi3Captor.capture(), eq("ddi:4.0"));
        assertTrue(
                ddi3Captor.getValue().items().stream().anyMatch(i -> mmvrType.equals(i.itemType())),
                "L'item MMVR du set doit être passé au converter DDI3 -> DDI4");
    }

    @Test
    void getFullPhysicalInstance_convertsEveryItemOfTheSetIncludingCodeListsAndCategories() {
        // Référence de la réconciliation des VersionDate : contrairement à getPhysicalInstance,
        // rien n'est écarté — sans quoi les listes de codes passeraient pour de nouveaux items.
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";
        String codeListId = "2f70f505-4a9e-4abe-82d4-c4ddfed25d52";
        String categoryId = "d363a730-14d4-4c54-9464-982312cf9330";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";
        String categoryType = "7e47c269-bcab-40f7-a778-af7bbc4e3d00";

        stubSet(
                new ColecticaSetItem(instanceId, 1, agencyId),
                new ColecticaSetItem(codeListId, 1, agencyId),
                new ColecticaSetItem(categoryId, 1, agencyId));
        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")),
                description(codeListType, agencyId, 1, codeListId, fragment("CodeList")),
                description(categoryType, agencyId, 1, categoryId, fragment("Category")));
        stubDdi3ToDdi4Conversion(new Ddi4Response("ddi:4.0", null, null, null, null, null, null, null));

        ddiRepository.getFullPhysicalInstance(agencyId, instanceId);

        ArgumentCaptor<Ddi3Response> ddi3Captor = ArgumentCaptor.forClass(Ddi3Response.class);
        verify(ddi3ToDdi4Converter).convertDdi3ToDdi4(ddi3Captor.capture(), eq("ddi:4.0"));
        assertEquals(
                List.of(instanceId, codeListId, categoryId),
                ddi3Captor.getValue().items().stream()
                        .map(Ddi3Response.Ddi3Item::identifier)
                        .toList());
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

        when(instanceConfiguration.itemTypes()).thenReturn(physicalInstanceItemTypesWithCodeLists());

        stubSet(
                new ColecticaSetItem(instanceId, 1, agencyId),
                new ColecticaSetItem("2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", 1, agencyId),
                new ColecticaSetItem(codeListId, 1, agencyId),
                new ColecticaSetItem(categoryId, 1, agencyId));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")),
                description(VARIABLE_TYPE, agencyId, 1, "2636d17c-d59d-4aa7-bd02-9cab5c0bbc7d", fragment("Variable")),
                description(CODE_LIST_TYPE, agencyId, 1, codeListId, fragment("CodeList")),
                description(CATEGORY_TYPE, agencyId, 1, categoryId, fragment("Category")));

        Ddi4CodeList mockCodeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:28.140Z"),
                "urn:ddi:fr.insee:" + codeListId + ":1",
                agencyId,
                codeListId,
                "1",
                LangStrings.of("fr-FR", "ma code list"),
                null,
                List.of(new Code(
                        Code.TYPE,
                        "urn:ddi:fr.insee:6a290143-b9f6-43d3-92ac-70c3b2f516c1:1",
                        agencyId,
                        "6a290143-b9f6-43d3-92ac-70c3b2f516c1",
                        "1",
                        Reference.of(agencyId, categoryId, "1", "Category"),
                        ValueType.of("a"),
                        null)));
        Ddi4Category mockCategory = new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-12-10T11:55:28.140Z"),
                "urn:ddi:fr.insee:" + categoryId + ":1",
                agencyId,
                categoryId,
                "1",
                LangStrings.of("fr-FR", "cat"));

        Ddi4Response mockDdi4Response =
                new Ddi4Response("ddi:4.0", null, null, null, null, List.of(mockCodeList), List.of(mockCategory), null);
        stubDdi3ToDdi4Conversion(mockDdi4Response);

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
        assertTrue(capturedDdi3.items().stream().anyMatch(i -> CODE_LIST_TYPE.equals(i.itemType())));
        assertTrue(capturedDdi3.items().stream().anyMatch(i -> CATEGORY_TYPE.equals(i.itemType())));
    }

    @Test
    void getPhysicalInstanceCodeLists_returnsEmptyWhenNoCodeListInSet() {
        String instanceId = "32799021-0663-41cd-aca6-3ad8dbdae3e3";
        String agencyId = "fr.insee";

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                        "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"));

        stubSet(new ColecticaSetItem(instanceId, 1, agencyId));

        stubDescriptions(description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")));

        List<Ddi4CodeList> result = ddiRepository.getPhysicalInstanceCodeLists(agencyId, instanceId);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Aucune CodeList dans le set -> liste vide, pas d'appel au converter");
        verifyNoInteractions(ddi3ToDdi4Converter);
    }

    @Test
    void shouldGetGroups() {
        // Given

        ColecticaItem group1 = colecticaItem(
                "Group",
                "fr.insee",
                1,
                "group-1",
                Map.of("fr-FR", "Base permanente des équipements", "en", "Permanent Database of Facilities"),
                Map.of("fr-FR", "BPE", "en", "BPE"),
                "2025-01-09T00:00:00",
                true,
                1L);

        ColecticaItem group2 = colecticaItem(
                "Group",
                "fr.insee",
                1,
                "group-2",
                Map.of("fr-FR", "Recensement de la population", "en", "Population Census"),
                Map.of("fr-FR", "RP", "en", "PC"),
                "2025-01-08T00:00:00",
                true,
                2L);

        ColecticaResponse mockResponse = queryResponse(group1, group2);

        // Mock query call - should query for Group itemType (4bd6eef6-99df-40e6-9b11-5b8f64e5cb23)
        when(colecticaClient.query(anyList())).thenReturn(mockResponse);

        // Mock second call to _getList for extracting UserID (seriesIris) from full XML
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[0]);

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
        assertEquals(
                "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23",
                itemTypesCaptor.getValue().get(0)); // Group UUID
    }

    @Test
    void shouldGetGroupById() {
        // Given
        String groupId = "10a689ce-7006-429b-8e84-036b7787b422";
        String agencyId = "fr.insee";

        // Mock DDI set response with Group and StudyUnits
        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n"
                + "    <ddi:TopLevelReference>\n"
                + "        <r:Agency>fr.insee</r:Agency>\n"
                + "        <r:ID>10a689ce-7006-429b-8e84-036b7787b422</r:ID>\n"
                + "        <r:Version>1</r:Version>\n"
                + "        <r:TypeOfObject>Group</r:TypeOfObject>\n"
                + "    </ddi:TopLevelReference>\n"
                + "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n"
                + "        <Group isUniversallyUnique=\"true\" versionDate=\"2025-01-09T09:00:00Z\" xmlns=\"ddi:group:3_3\">\n"
                + "            <r:URN>urn:ddi:fr.insee:10a689ce-7006-429b-8e84-036b7787b422:1</r:URN>\n"
                + "            <r:Agency>fr.insee</r:Agency>\n"
                + "            <r:ID>10a689ce-7006-429b-8e84-036b7787b422</r:ID>\n"
                + "            <r:Version>1</r:Version>\n"
                + "            <r:VersionResponsibility>bauhaus</r:VersionResponsibility>\n"
                + "            <r:Citation>\n"
                + "                <r:Title>\n"
                + "                    <r:String xml:lang=\"fr-FR\">Base permanente des équipements</r:String>\n"
                + "                </r:Title>\n"
                + "            </r:Citation>\n"
                + "            <r:StudyUnitReference>\n"
                + "                <r:Agency>fr.insee</r:Agency>\n"
                + "                <r:ID>89f5e04d-da22-485f-9c08-5fbe452b6c90</r:ID>\n"
                + "                <r:Version>1</r:Version>\n"
                + "                <r:TypeOfObject>StudyUnit</r:TypeOfObject>\n"
                + "            </r:StudyUnitReference>\n"
                + "            <r:StudyUnitReference>\n"
                + "                <r:Agency>fr.insee</r:Agency>\n"
                + "                <r:ID>820a7c14-0ac4-42bc-a8c1-d39f60e304ee</r:ID>\n"
                + "                <r:Version>1</r:Version>\n"
                + "                <r:TypeOfObject>StudyUnit</r:TypeOfObject>\n"
                + "            </r:StudyUnitReference>\n"
                + "        </Group>\n"
                + "    </Fragment>\n"
                + "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n"
                + "        <StudyUnit isUniversallyUnique=\"true\" versionDate=\"2025-01-09T09:00:00Z\" xmlns=\"ddi:studyunit:3_3\">\n"
                + "            <r:URN>urn:ddi:fr.insee:89f5e04d-da22-485f-9c08-5fbe452b6c90:1</r:URN>\n"
                + "            <r:Agency>fr.insee</r:Agency>\n"
                + "            <r:ID>89f5e04d-da22-485f-9c08-5fbe452b6c90</r:ID>\n"
                + "            <r:Version>1</r:Version>\n"
                + "            <r:Citation>\n"
                + "                <r:Title>\n"
                + "                    <r:String xml:lang=\"fr-FR\">BPE 2021</r:String>\n"
                + "                </r:Title>\n"
                + "            </r:Citation>\n"
                + "        </StudyUnit>\n"
                + "    </Fragment>\n"
                + "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n"
                + "        <StudyUnit isUniversallyUnique=\"true\" versionDate=\"2025-01-09T09:00:00Z\" xmlns=\"ddi:studyunit:3_3\">\n"
                + "            <r:URN>urn:ddi:fr.insee:820a7c14-0ac4-42bc-a8c1-d39f60e304ee:1</r:URN>\n"
                + "            <r:Agency>fr.insee</r:Agency>\n"
                + "            <r:ID>820a7c14-0ac4-42bc-a8c1-d39f60e304ee</r:ID>\n"
                + "            <r:Version>1</r:Version>\n"
                + "            <r:Citation>\n"
                + "                <r:Title>\n"
                + "                    <r:String xml:lang=\"fr-FR\">BPE 2022</r:String>\n"
                + "                </r:Title>\n"
                + "            </r:Citation>\n"
                + "        </StudyUnit>\n"
                + "    </Fragment>\n"
                + "</ddi:FragmentInstance>";

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
        assertEquals(
                "Base permanente des équipements",
                result.group().get(0).citation().title().get(0).value());
        assertEquals(2, result.group().get(0).studyUnitReference().size());

        // Verify StudyUnits
        assertNotNull(result.studyUnit());
        assertEquals(2, result.studyUnit().size());
        assertEquals(
                "89f5e04d-da22-485f-9c08-5fbe452b6c90",
                result.studyUnit().get(0).id());
        assertEquals(
                "BPE 2021", result.studyUnit().get(0).citation().title().get(0).value());
        assertEquals(
                "820a7c14-0ac4-42bc-a8c1-d39f60e304ee",
                result.studyUnit().get(1).id());
        assertEquals(
                "BPE 2022", result.studyUnit().get(1).citation().title().get(0).value());

        // Verify TopLevelReference
        assertNotNull(result.topLevelReference());
        assertEquals(1, result.topLevelReference().size());
        assertEquals(groupId, result.topLevelReference().get(0).id());
        assertEquals("Group", result.topLevelReference().get(0).type());

        // Verify ddiset endpoint was called
        verify(colecticaClient).getDdiSet(agencyId, groupId);
    }

    @Test
    void shouldPreserveUtf8AccentsWhenGroupResponseHasNoCharset() {
        // Reproduces the mojibake bug: Colectica returns UTF-8 bytes but with a Content-Type
        // that omits charset, so Spring's StringHttpMessageConverter falls back to ISO-8859-1.
        // The repository must read raw bytes and decode as UTF-8 itself.
        String groupId = "4ae1ad6e-bd5a-3ae7-ab21-57efc2f5e279";
        String agencyId = "fr.insee";

        String ddisetXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\">\n"
                + "    <ddi:TopLevelReference>\n"
                + "        <r:Agency>fr.insee</r:Agency>\n"
                + "        <r:ID>"
                + groupId + "</r:ID>\n" + "        <r:Version>1</r:Version>\n"
                + "        <r:TypeOfObject>Group</r:TypeOfObject>\n"
                + "    </ddi:TopLevelReference>\n"
                + "    <Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">\n"
                + "        <Group isUniversallyUnique=\"true\" versionDate=\"2026-05-11T10:05:24Z\" xmlns=\"ddi:group:3_3\">\n"
                + "            <r:URN>urn:ddi:fr.insee:"
                + groupId + ":1</r:URN>\n" + "            <r:Agency>fr.insee</r:Agency>\n"
                + "            <r:ID>"
                + groupId + "</r:ID>\n" + "            <r:Version>1</r:Version>\n"
                + "            <r:Citation>\n"
                + "                <r:Title>\n"
                + "                    <r:String xml:lang=\"fr-FR\">Enquête capacité à innover et stratégie</r:String>\n"
                + "                </r:Title>\n"
                + "            </r:Citation>\n"
                + "        </Group>\n"
                + "    </Fragment>\n"
                + "</ddi:FragmentInstance>";
        byte[] ddisetBytes = ddisetXml.getBytes(StandardCharsets.UTF_8);

        // Simulate Colectica delivering raw bytes (what a real server returns), not a pre-decoded String.
        when(colecticaClient.getDdiSet(anyString(), anyString())).thenReturn(ddisetBytes);

        Ddi4GroupResponse result = ddiRepository.getGroup(agencyId, groupId);

        assertNotNull(result);
        assertNotNull(result.group());
        assertEquals(1, result.group().size());
        assertEquals(
                "Enquête capacité à innover et stratégie",
                result.group().get(0).citation().title().get(0).value());
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
                "StudyUnit", STUDY_UNIT_TYPE);
    }

    private static ColecticaItem codeListItem(String identifier, String labelFr, String versionDate) {
        return itemOfType(identifier, CODE_LIST_TYPE, labelFr, versionDate);
    }

    private static ColecticaItem itemOfType(String identifier, String itemType, String labelFr, String versionDate) {
        return colecticaItem(
                itemType,
                "fr.insee",
                1,
                identifier,
                labelFr == null ? null : Map.of("fr-FR", labelFr),
                null,
                versionDate,
                false,
                1L);
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
                        RelationshipDirection.BY_SUBJECT, new ItemReference(agencyId, parentId), List.of(childType)))
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

        stubMutualizedPackage(agencyId, packageId);
        stubPackageTree(
                agencyId,
                packageId,
                schemeId,
                groupId,
                new ItemReference(agencyId, cl1Id),
                new ItemReference(agencyId, cl2Id));

        // Labels are resolved through a single repository-wide CodeList query.
        when(colecticaClient.query(List.of(CODE_LIST_TYPE)))
                .thenReturn(queryResponse(
                        codeListItem(cl1Id, "Niveau 1", "2024-10-31T10:43:38"),
                        codeListItem(cl2Id, "Niveau 2", "2024-10-31T10:43:38")));

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(2, result.size());
        assertEquals(cl1Id, result.get(0).id());
        assertEquals("Niveau 1", result.get(0).label());
        assertEquals(agencyId, result.get(0).agency());
        assertEquals(cl2Id, result.get(1).id());

        // Never walks up the parent chain anymore.
        verify(colecticaClient, never()).findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT), any(), anyList());
    }

    @Test
    void mutualizedCodeList_versionDateComesFromItemXmlNotQueryEnvelope() {
        // Le versionDate du _query n'est pas fiable (Colectica renvoie 0001-01-01) : on le lit
        // depuis l'attribut versionDate du XML de l'item, récupéré via un item/_getList.
        String agencyId = "fr.insee";
        String clId = "cl-1";

        stubMutualizedPackageWithSingleCodeList(agencyId, clId);

        // _query carries the (unreliable) envelope versionDate — must be ignored.
        when(colecticaClient.query(List.of(CODE_LIST_TYPE)))
                .thenReturn(queryResponse(codeListItem(clId, "Ma code list", "0001-01-01T00:00:00")));

        // The item XML carries the real versionDate attribute.
        String xml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<CodeList xmlns=\"ddi:logicalproduct:3_3\" versionDate=\"2026-06-29T14:26:32.961778\">"
                + "<r:URN>urn:ddi:fr.insee:cl-1:1</r:URN></CodeList></Fragment>";
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(CODE_LIST_TYPE, agencyId, 1, clId, xml, null, null, false, false, false, "DDI")
        });

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(1, result.size());
        assertEquals(clId, result.get(0).id());
        assertEquals("2026-06-29 14:26:32", utcSeconds(result.get(0).versionDate()));
    }

    @Test
    void mutualizedCodeList_prefersLabelOverItemName() {
        // Une liste de codes mutualisée porte à la fois un nom technique (itemName) et un libellé
        // lisible (label). Le sélecteur doit afficher le libellé, pas le nom.
        String agencyId = "fr.insee";
        String clId = "cl-1";

        stubMutualizedPackageWithSingleCodeList(agencyId, clId);

        ColecticaItem withNameAndLabel = colecticaItem(
                CODE_LIST_TYPE,
                agencyId,
                1,
                clId,
                Map.of("fr-FR", "CL_NOM_TECHNIQUE"), // itemName
                Map.of("fr-FR", "Libellé lisible"), // label
                "2024-10-31T10:43:38",
                false,
                1L);
        when(colecticaClient.query(List.of(CODE_LIST_TYPE))).thenReturn(queryResponse(withNameAndLabel));

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

        stubMutualizedPackage(agencyId, packageId);

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

        stubMutualizedPackage(agencyId, packageId);
        stubPackageTree(
                agencyId,
                packageId,
                schemeId,
                groupId,
                new ItemReference(agencyId, labelledId),
                new ItemReference(agencyId, blankId));

        ColecticaItem blank = colecticaItem(
                CODE_LIST_TYPE,
                agencyId,
                1,
                blankId,
                Map.of("fr-FR", "   "),
                Map.of("fr-FR", ""),
                "2024-10-31T10:43:38",
                false,
                1L);
        when(colecticaClient.query(List.of(CODE_LIST_TYPE)))
                .thenReturn(queryResponse(codeListItem(labelledId, "Has label", "2024-10-31T10:43:38"), blank));

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

        stubMutualizedPackage(agencyId, packageId);

        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE, new ItemReference(agencyId, schemeId));
        stubChildren(
                agencyId,
                schemeId,
                CODE_LIST_GROUP_TYPE,
                new ItemReference(agencyId, groupAId),
                new ItemReference(agencyId, groupBId));
        stubChildren(agencyId, groupAId, CODE_LIST_TYPE, new ItemReference(agencyId, sharedClId));
        stubChildren(agencyId, groupBId, CODE_LIST_TYPE, new ItemReference(agencyId, sharedClId));
        when(colecticaClient.query(List.of(CODE_LIST_TYPE)))
                .thenReturn(queryResponse(codeListItem(sharedClId, "Shared label", "2024-10-31T10:43:38")));

        List<PartialCodesList> result = ddiRepository.getMutualizedCodesLists();

        assertEquals(1, result.size());
        assertEquals(sharedClId, result.get(0).id());
    }

    @Test
    void shouldGetMutualizedCodesListWithCodesAndCategories() {
        // Given
        String agencyId = "fr.insee";
        String codeListId = "fc65a527-a04b-4505-85de-0a181e54dbad";
        String categoryId = "cat-1";
        int version = 1;

        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        stubCodeListSet(agencyId, codeListId, categoryId, version, fragment("CodeList"), fragment("Category"));

        stubDdi3ToDdi4Conversion(codeListResponse(agencyId, codeListId, categoryId, "1"));

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

        // Request with null dataRelationshipLabel - should preserve existing
        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Updated PI Label",
                null, // dataRelationshipLabel is null
                null // logicalRecordLabel is null
                );

        // Mock existing instance with a Label on DataRelationship
        // Existing DataRelationship has a Label with "en-US" language
        List<LangString> existingDrLabel = LangStrings.of("en-US", "Existing DR Label");
        List<LangString> existingLrLabel = LangStrings.of("de-DE", "Existing LR Label");

        // Capture what's passed to the DDI4 to DDI3 converter
        ArgumentCaptor<Ddi4Response> ddi4Captor =
                stubPhysicalInstanceLabelUpdate(agencyId, instanceId, existingDrLabel, existingLrLabel);

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

        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Updated PI Label",
                "New DR Label", // New label text
                "New LR Label");

        // DataRelationship and LogicalRecord have NO existing Label (null)
        ArgumentCaptor<Ddi4Response> ddi4Captor = stubPhysicalInstanceLabelUpdate(agencyId, instanceId, null, null);

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
        assertEquals(
                "New LR Label", updatedDr.logicalRecord().get(0).label().get(0).value());
    }

    @Test
    void shouldPreserveExistingLangWhenUpdatingLabelText() {
        // This tests the createLabelWithFallback behavior when both existingLabel and newText are provided
        // Given
        String instanceId = "test-pi-id";
        String agencyId = "fr.insee";

        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Updated PI Label",
                "Updated DR Label", // New text for existing label
                "Updated LR Label");

        // Existing labels with specific languages (not fr-FR)
        List<LangString> existingDrLabel = LangStrings.of("en-GB", "Old DR Label");
        List<LangString> existingLrLabel = LangStrings.of("es-ES", "Old LR Label");

        ArgumentCaptor<Ddi4Response> ddi4Captor =
                stubPhysicalInstanceLabelUpdate(agencyId, instanceId, existingDrLabel, existingLrLabel);

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
        assertEquals(
                "es-ES", updatedDr.logicalRecord().get(0).label().get(0).language()); // Should preserve existing lang
        assertEquals(
                "Updated LR Label",
                updatedDr.logicalRecord().get(0).label().get(0).value()); // But update text
    }

    @Test
    void shouldGetItemXmlWithVersion() {
        // Given
        String agency = "fr.insee";
        String id = "3b317f4c-79ae-422c-8cd4-04ba9d2e4be4";
        String version = "1";
        String expectedXml = "<CodeList><URN>urn:ddi:fr.insee:3b317f4c-79ae-422c-8cd4-04ba9d2e4be4:1</URN></CodeList>";

        ColecticaItemResponse itemResponse = new ColecticaItemResponse(
                "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                agency,
                1,
                id,
                expectedXml,
                "2025-01-01T00:00:00",
                null,
                true,
                false,
                false,
                "DDI");

        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(itemResponse);

        // When
        String result = ddiRepository.getItemXml(agency, id, version);

        // Then
        assertEquals(expectedXml, result);
    }

    @Test
    void shouldGetItemXmlLatestVersion() {
        // Given
        String agency = "fr.insee";
        String id = "3b317f4c-79ae-422c-8cd4-04ba9d2e4be4";
        String expectedXml = "<CodeList><URN>urn:ddi:fr.insee:3b317f4c-79ae-422c-8cd4-04ba9d2e4be4:2</URN></CodeList>";

        ColecticaItemResponse itemResponse = new ColecticaItemResponse(
                "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                agency,
                2,
                id,
                expectedXml,
                "2025-06-01T00:00:00",
                null,
                true,
                false,
                false,
                "DDI");

        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(itemResponse);

        // When
        String result = ddiRepository.getItemXml(agency, id);

        // Then
        assertEquals(expectedXml, result);
    }

    @Test
    void shouldReturnNullWhenItemXmlResponseIsNull() {
        // Given
        String agency = "fr.insee";
        String id = "3b317f4c-79ae-422c-8cd4-04ba9d2e4be4";
        String version = "1";

        when(colecticaClient.getItem(anyString(), anyString(), any())).thenReturn(null);

        // When
        String result = ddiRepository.getItemXml(agency, id, version);

        // Then
        assertNull(result);
    }

    /**
     * Une StudyUnit sans PhysicalInstance sort quand même dans une {@code FragmentInstance} : le
     * contrat du endpoint est multi-fragments depuis #1145, indépendamment du nombre de fragments.
     */
    @Test
    void shouldFindStudyUnitXmlByOperationIri_returnsFragmentInstanceWhenMatching() {
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String suId = "su-abc";
        String suAgency = "fr.insee";

        String studyUnitXml = studyUnitFragment(operationIri, "");
        stubStudyUnitDescription(suAgency, suId, studyUnitXml);

        Optional<String> result = ddiRepository.findStudyUnitXmlByOperationIri(operationIri);

        assertTrue(result.isPresent());
        assertThat(result.get())
                .contains("<ddi:FragmentInstance")
                .contains("<StudyUnit xmlns=\"ddi:studyunit:3_3\">")
                .contains("</ddi:FragmentInstance>");
    }

    /**
     * #1145 : les PhysicalInstances référencées par la StudyUnit sont déréférencées et leurs
     * fragments suivent celui de la StudyUnit dans la {@code FragmentInstance}.
     */
    @Test
    void shouldFindStudyUnitXmlByOperationIri_appendsDereferencedPhysicalInstanceFragments() {
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String suId = "su-abc";
        String suAgency = "fr.insee";

        String studyUnitXml = studyUnitFragment(
                operationIri,
                physicalInstanceReference(suAgency, "pi-1", "1") + physicalInstanceReference(suAgency, "pi-2", "3"));
        stubStudyUnitDescription(suAgency, suId, studyUnitXml);

        String firstPiXml = physicalInstanceFragment("pi-1");
        String secondPiXml = physicalInstanceFragment("pi-2");
        when(colecticaClient.getDescriptions(List.of(
                        new GetDescriptionsRequest.IdentifierRef(suAgency, "pi-1", 1),
                        new GetDescriptionsRequest.IdentifierRef(suAgency, "pi-2", 3))))
                .thenReturn(new ColecticaItemResponse[] {
                    physicalInstanceItemResponse(suAgency, "pi-1", 1, firstPiXml),
                    physicalInstanceItemResponse(suAgency, "pi-2", 3, secondPiXml)
                });

        Optional<String> result = ddiRepository.findStudyUnitXmlByOperationIri(operationIri);

        assertTrue(result.isPresent());
        assertThat(result.get()).contains("<r:ID>pi-1</r:ID>").contains("<r:ID>pi-2</r:ID>");
        assertThat(result.get().indexOf("<StudyUnit")).isLessThan(result.get().indexOf("<PhysicalInstance"));
    }

    /** #1145 : la même descente, projetée en DDI 4 pour la négociation JSON. */
    @Test
    void shouldFindStudyUnitByOperationIri_returnsStudyUnitAndItsPhysicalInstances() {
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String suId = "su-abc";
        String suAgency = "fr.insee";

        String studyUnitXml = studyUnitFragment(operationIri, physicalInstanceReference(suAgency, "pi-1", "1"));
        stubStudyUnitDescription(suAgency, suId, studyUnitXml);

        String piXml = physicalInstanceFragment("pi-1");
        when(colecticaClient.getDescriptions(List.of(new GetDescriptionsRequest.IdentifierRef(suAgency, "pi-1", 1))))
                .thenReturn(new ColecticaItemResponse[] {physicalInstanceItemResponse(suAgency, "pi-1", 1, piXml)});

        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                null,
                "urn:ddi:" + suAgency + ":" + suId + ":1",
                suAgency,
                suId,
                "1",
                null,
                operationIri,
                null);
        when(ddi3ToDdi4Converter.toStudyUnit(studyUnitXml)).thenReturn(studyUnit);
        Ddi4PhysicalInstance physicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                null,
                "urn:ddi:" + suAgency + ":pi-1:1",
                suAgency,
                "pi-1",
                "1",
                null,
                null,
                null);
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(), eq(Ddi4Response.SCHEMA)))
                .thenReturn(new Ddi4Response(
                        Ddi4Response.SCHEMA, null, List.of(physicalInstance), null, null, null, null, null));

        Optional<Ddi4StudyUnitResponse> result = ddiRepository.findStudyUnitByOperationIri(operationIri);

        assertTrue(result.isPresent());
        assertThat(result.get().studyUnit()).containsExactly(studyUnit);
        assertThat(result.get().physicalInstance()).containsExactly(physicalInstance);
        assertThat(result.get().topLevelReference())
                .containsExactly(Reference.of(suAgency, suId, "1", Ddi4StudyUnit.TYPE));
    }

    @Test
    void shouldFindStudyUnitByOperationIri_returnsEmptyWhenNoMatch() {
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        String suId = "su-xyz";
        String suAgency = "fr.insee";

        stubStudyUnitOfAnotherOperation(suAgency, suId);

        assertFalse(ddiRepository.findStudyUnitByOperationIri(operationIri).isPresent());
    }

    private static ColecticaResponse studyUnitQueryResponse(String agency, String id) {
        ColecticaItem suItem = new ColecticaItem(
                null,
                Map.of("fr-FR", "BPE 2021"),
                Map.of(),
                null,
                null,
                0,
                "repo",
                true,
                List.of(),
                "30ea0200-7121-4f01-8d21-a931a182b86d",
                agency,
                1,
                id,
                null,
                null,
                "2025-01-01T00:00:00",
                null,
                false,
                false,
                false,
                "DDI",
                1L,
                0);
        return new ColecticaResponse(List.of(suItem), 1, 1, null, null, null);
    }

    private static ColecticaItemResponse studyUnitItemResponse(String agency, String id, String xml) {
        return new ColecticaItemResponse(
                "30ea0200-7121-4f01-8d21-a931a182b86d",
                agency,
                1,
                id,
                xml,
                "2025-01-01T00:00:00",
                null,
                false,
                false,
                false,
                "DDI");
    }

    private static ColecticaItemResponse physicalInstanceItemResponse(
            String agency, String id, int version, String xml) {
        return new ColecticaItemResponse(
                "a51e85bb-6259-4488-8df2-f08cb43485f8",
                agency,
                version,
                id,
                xml,
                "2025-01-01T00:00:00",
                null,
                false,
                false,
                false,
                "DDI");
    }

    private static String studyUnitFragment(String operationIri, String physicalInstanceReferences) {
        return "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\">"
                + "<r:UserID>" + operationIri + "</r:UserID>"
                + physicalInstanceReferences
                + "</StudyUnit></Fragment>";
    }

    private static String physicalInstanceReference(String agency, String id, String version) {
        return "<r:PhysicalInstanceReference>"
                + "<r:Agency>" + agency + "</r:Agency>"
                + "<r:ID>" + id + "</r:ID>"
                + "<r:Version>" + version + "</r:Version>"
                + "<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>"
                + "</r:PhysicalInstanceReference>";
    }

    private static String physicalInstanceFragment(String id) {
        return "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<PhysicalInstance xmlns=\"ddi:physicalinstance:3_3\">"
                + "<r:ID>" + id + "</r:ID>"
                + "</PhysicalInstance></Fragment>";
    }

    @Test
    void shouldFindStudyUnitXmlByOperationIri_returnsEmptyWhenNoMatch() {
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        String suId = "su-xyz";
        String suAgency = "fr.insee";

        stubStudyUnitOfAnotherOperation(suAgency, suId);

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
        stubParents(agencyId, piId, studyUnitType, new ItemReference("fr.insee", "su-222"));
        // StudyUnit → Group
        stubParents("fr.insee", "su-222", groupType, new ItemReference("fr.insee", "grp-333"));

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

    @Test
    void shouldThrowStudyUnitNotFoundWhenPhysicalInstanceHasNoStudyUnit() {
        // Given
        String studyUnitType = "30ea0200-7121-4f01-8d21-a931a182b86d";
        String agencyId = "fr.insee";
        String piId = "pi-111";

        stubParents(agencyId, piId, studyUnitType);

        // When / Then
        StudyUnitNotFoundException exception = assertThrows(
                StudyUnitNotFoundException.class, () -> ddiRepository.getPhysicalInstanceParents(agencyId, piId));
        assertThat(exception.getMessage()).isEqualTo("No study unit found for physical instance fr.insee/pi-111");
    }

    // ---- #485 : getCodeList / getCodeListXml (CodeList + Categories, versioned) ----

    @Test
    void getCodeList_withVersion_usesVersionedSetUrlAndConvertsToDdi4() {
        String agencyId = "fr.insee";
        String codeListId = "fc65a527-a04b-4505-85de-0a181e54dbad";
        String categoryId = "cat-1";
        String version = "2";

        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        stubCodeListSet(agencyId, codeListId, categoryId, 2, fragment("CodeList"), fragment("Category"));

        stubDdi3ToDdi4Conversion(codeListResponse(agencyId, codeListId, categoryId, "2"));

        Ddi4Response result = ddiRepository.getCodeList(agencyId, codeListId, version);

        assertNotNull(result);
        assertEquals(1, result.codeList().size());
        assertEquals(codeListId, result.codeList().get(0).id());
        assertEquals(1, result.category().size());
        verify(colecticaClient).getSet(agencyId, codeListId, version);
        verify(colecticaClient).getDescriptions(anyList());
    }

    @Test
    void getCodeListXml_returnsMultiFragmentFragmentInstance() {
        String agencyId = "fr.insee";
        String codeListId = "cl-1";
        String categoryId = "cat-1";

        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        stubCodeListSet(
                agencyId,
                codeListId,
                categoryId,
                1,
                fragment("CodeList xmlns=\"ddi:logicalproduct:3_3\""),
                fragment("Category xmlns=\"ddi:logicalproduct:3_3\""));

        String xml = ddiRepository.getCodeListXml(agencyId, codeListId, null);

        assertNotNull(xml);
        assertTrue(xml.contains("FragmentInstance"));
        assertTrue(xml.contains("<CodeList"));
        assertTrue(xml.contains("<Category"));
        verify(colecticaClient).getSet(eq(agencyId), eq(codeListId), any());
    }

    @Test
    void getCodeListXml_returnsNullWhenSetEmpty() {
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(new ColecticaSetItem[0]);

        assertNull(ddiRepository.getCodeListXml("fr.insee", "unknown", null));
    }

    @Test
    void getCodeList_returnsNull_whenRootIsNotCodeList() {
        // #493 : un {id} pointant vers un autre type (ici PhysicalInstance) ne renvoie aucun contenu.
        String agencyId = "fr.insee";
        String id = "not-a-codelist";
        stubStandardSetWithSingleRoot(agencyId, id, PHYSICAL_INSTANCE_TYPE, "PhysicalInstance");

        assertNull(ddiRepository.getCodeList(agencyId, id, null));
        verify(ddi3ToDdi4Converter, never()).convertDdi3ToDdi4(any(), any());
    }

    @Test
    void getCodeListXml_returnsNull_whenRootIsNotCodeList() {
        // #493 : même garde de type sur la variante XML.
        String agencyId = "fr.insee";
        String id = "not-a-codelist";
        stubStandardSetWithSingleRoot(agencyId, id, PHYSICAL_INSTANCE_TYPE, "PhysicalInstance");

        assertNull(ddiRepository.getCodeListXml(agencyId, id, null));
    }

    // ---- #447 : getDataRelationships / getDataRelationshipsXml (PhysicalInstance) ----

    @Test
    void getDataRelationships_keepsPhysicalInstanceDataRelationshipAndReferencedVariables() {
        // #447 / #1146 : la réponse /fichier porte la PhysicalInstance, les DataRelationship ET les
        // Variable référencées (VariableUsedReference des VariablesInRecord), mais pas les
        // CodeList/Category référencées.
        String agencyId = "fr.insee";
        String piId = "pi-1";

        when(instanceConfiguration.itemTypes()).thenReturn(physicalInstanceItemTypesWithCodeLists());

        stubSet(setItems(agencyId, piId, "dr-1", "var-1", "cl-1", "cat-1"));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, piId, fragment("PhysicalInstance")),
                description(DATA_RELATIONSHIP_TYPE, agencyId, 1, "dr-1", fragment("DataRelationship")),
                description(VARIABLE_TYPE, agencyId, 1, "var-1", fragment("Variable")),
                description(CODE_LIST_TYPE, agencyId, 1, "cl-1", fragment("CodeList")),
                description(CATEGORY_TYPE, agencyId, 1, "cat-1", fragment("Category")));

        ArgumentCaptor<Ddi3Response> captor = captureDdi3ToDdi4Conversion(
                new Ddi4Response("ddi:4.0", null, List.of(), List.of(), List.of(), List.of(), List.of(), null));

        ddiRepository.getDataRelationships(agencyId, piId, null);

        // PI + DataRelationship + Variable sont convertis ; CodeList et Category sont écartés.
        List<Ddi3Response.Ddi3Item> converted = captor.getValue().items();
        assertEquals(3, converted.size());
        // #1146 : la PhysicalInstance ouvre la liste, avant les éléments qui la composent.
        assertEquals(PHYSICAL_INSTANCE_TYPE, converted.get(0).itemType());
        assertTrue(converted.stream().anyMatch(i -> DATA_RELATIONSHIP_TYPE.equals(i.itemType())));
        assertTrue(converted.stream().anyMatch(i -> VARIABLE_TYPE.equals(i.itemType())));
        assertTrue(converted.stream().noneMatch(i -> CODE_LIST_TYPE.equals(i.itemType())));
        assertTrue(converted.stream().noneMatch(i -> CATEGORY_TYPE.equals(i.itemType())));
    }

    @Test
    void getDataRelationships_keepsSentinelValuesRepresentationWithItsCodeListAndCategories() {
        // #1591 : les valeurs sentinelles d'une variable doivent être consultables sur /fichier — la
        // ManagedMissingValuesRepresentation, la CodeList de sentinelles qu'elle référence et les
        // Category de ses codes. Les CodeList/Category de représentation restent écartées.
        String agencyId = "fr.insee";
        String piId = "pi-1";
        String mmvrType = "c9ec9f5b-b9b4-4a1a-a5b6-2a89b1a52ffe";

        when(instanceConfiguration.itemTypes()).thenReturn(dataRelationshipItemTypesWithSentinels(mmvrType));

        stubSet(setItems(
                agencyId,
                piId,
                "dr-1",
                "var-1",
                "mmvr-1",
                "cl-sentinel",
                "cat-sentinel",
                "cl-representation",
                "cat-representation"));

        List<ColecticaItemResponse> descriptions = new ArrayList<>(List.of(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, piId, fragment("PhysicalInstance")),
                description(DATA_RELATIONSHIP_TYPE, agencyId, 1, "dr-1", fragment("DataRelationship"))));
        descriptions.addAll(variableWithSentinelValuesDescriptions(agencyId, mmvrType));
        descriptions.add(description(
                CATEGORY_TYPE, agencyId, 1, "cat-representation", fragment("Category ID=\"cat-representation\"")));
        stubDescriptions(descriptions.toArray(ColecticaItemResponse[]::new));

        stubSentinelValuesConversion(agencyId);

        ArgumentCaptor<Ddi3Response> captor = captureDdi3ToDdi4Conversion(
                new Ddi4Response("ddi:4.0", null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));

        ddiRepository.getDataRelationships(agencyId, piId, null);

        List<Ddi3Response.Ddi3Item> converted = captor.getValue().items();
        assertThat(converted)
                .extracting(Ddi3Response.Ddi3Item::identifier)
                .containsExactlyInAnyOrder(piId, "dr-1", "var-1", "mmvr-1", "cl-sentinel", "cat-sentinel");
        // #1146 : la PhysicalInstance ouvre toujours la liste.
        assertEquals(piId, converted.get(0).identifier());
    }

    @Test
    void getDataRelationshipsXml_includesSentinelValuesFragments() {
        // #1591 : même contenu côté XML — les fragments MMVR, CodeList de sentinelles et Category
        // associées figurent dans la FragmentInstance.
        String agencyId = "fr.insee";
        String piId = "pi-1";
        String mmvrType = "c9ec9f5b-b9b4-4a1a-a5b6-2a89b1a52ffe";

        when(instanceConfiguration.itemTypes()).thenReturn(dataRelationshipItemTypesWithSentinels(mmvrType));

        stubSet(setItems(agencyId, piId, "var-1", "mmvr-1", "cl-sentinel", "cat-sentinel", "cl-representation"));

        List<ColecticaItemResponse> descriptions = new ArrayList<>(
                List.of(description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, piId, fragment("PhysicalInstance"))));
        descriptions.addAll(variableWithSentinelValuesDescriptions(agencyId, mmvrType));
        stubDescriptions(descriptions.toArray(ColecticaItemResponse[]::new));

        stubSentinelValuesConversion(agencyId);

        String xml = ddiRepository.getDataRelationshipsXml(agencyId, piId, null);

        assertNotNull(xml);
        assertTrue(xml.contains("<ManagedMissingValuesRepresentation"));
        assertTrue(xml.contains("<CodeList ID=\"cl-sentinel\""));
        assertTrue(xml.contains("<Category ID=\"cat-sentinel\""));
        assertFalse(xml.contains("cl-representation"));
    }

    @Test
    void getDataRelationships_setsPhysicalInstanceTopLevelReference() {
        // #494 : le TopLevelReference de /variables doit pointer la PhysicalInstance interrogée,
        // pas rester null (la PI est écartée de la sortie mais reste la racine du FragmentInstance).
        String agencyId = "fr.insee";
        String piId = "pi-1";

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                        "DataRelationship", DATA_RELATIONSHIP_TYPE,
                        "Variable", VARIABLE_TYPE));

        stubSet(new ColecticaSetItem(piId, 3, agencyId), new ColecticaSetItem("dr-1", 3, agencyId));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 3, piId, fragment("PhysicalInstance")),
                description(DATA_RELATIONSHIP_TYPE, agencyId, 3, "dr-1", fragment("DataRelationship")));

        // Le convertisseur renvoie ici un TopLevelReference null : la référence est alors reconstruite
        // depuis les descriptions brutes du set.
        Ddi4Response mockDdi4 = new Ddi4Response("ddi:4.0", null, null, List.of(), List.of(), null, null, null);
        stubDdi3ToDdi4Conversion(mockDdi4);

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

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "CodeList", CODE_LIST_TYPE,
                        "Category", CATEGORY_TYPE));

        stubSet(new ColecticaSetItem(codeListId, 5, agencyId), new ColecticaSetItem(categoryId, 5, agencyId));

        stubDescriptions(
                description(CODE_LIST_TYPE, agencyId, 5, codeListId, fragment("CodeList")),
                description(CATEGORY_TYPE, agencyId, 5, categoryId, fragment("Category")));

        Ddi4Response mockDdi4 = new Ddi4Response("ddi:4.0", null, null, null, null, List.of(), List.of(), null);
        stubDdi3ToDdi4Conversion(mockDdi4);

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
    void getDataRelationshipsXml_includesPhysicalInstanceThenDataRelationshipAndVariableFragments() {
        String agencyId = "fr.insee";
        String piId = "pi-1";

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                        "DataRelationship", DATA_RELATIONSHIP_TYPE,
                        "Variable", VARIABLE_TYPE,
                        "CodeList", CODE_LIST_TYPE));

        stubSet(
                new ColecticaSetItem(piId, 1, agencyId),
                new ColecticaSetItem("dr-1", 1, agencyId),
                new ColecticaSetItem("var-1", 1, agencyId),
                new ColecticaSetItem("cl-1", 1, agencyId));

        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, piId, fragment("PhysicalInstance")),
                description(
                        DATA_RELATIONSHIP_TYPE,
                        agencyId,
                        1,
                        "dr-1",
                        fragment("DataRelationship xmlns=\"ddi:logicalproduct:3_3\"")),
                description(VARIABLE_TYPE, agencyId, 1, "var-1", fragment("Variable")),
                description(CODE_LIST_TYPE, agencyId, 1, "cl-1", fragment("CodeList")));

        String xml = ddiRepository.getDataRelationshipsXml(agencyId, piId, null);

        assertNotNull(xml);
        assertTrue(xml.contains("FragmentInstance"));
        assertTrue(xml.contains("<PhysicalInstance"));
        assertTrue(xml.contains("<DataRelationship"));
        assertTrue(xml.contains("<Variable"));
        assertFalse(xml.contains("<CodeList"));
        // #1146 : le fragment de la PhysicalInstance précède ceux des éléments qui la composent.
        assertTrue(xml.indexOf("<PhysicalInstance") < xml.indexOf("<DataRelationship"));
    }

    @Test
    void getDataRelationshipsXml_putsPhysicalInstanceFragmentFirst_whenColecticaReturnsItLast() {
        // #1146 : l'ordre de la sortie ne dépend pas de celui des descriptions Colectica.
        String agencyId = "fr.insee";
        String piId = "pi-1";

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                        "DataRelationship", DATA_RELATIONSHIP_TYPE));

        stubSet(new ColecticaSetItem("dr-1", 1, agencyId), new ColecticaSetItem(piId, 1, agencyId));
        stubDescriptions(
                description(DATA_RELATIONSHIP_TYPE, agencyId, 1, "dr-1", fragment("DataRelationship")),
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, piId, fragment("PhysicalInstance")));

        String xml = ddiRepository.getDataRelationshipsXml(agencyId, piId, null);

        assertNotNull(xml);
        assertTrue(xml.contains("<PhysicalInstance"));
        assertTrue(xml.indexOf("<PhysicalInstance") < xml.indexOf("<DataRelationship"));
    }

    @Test
    void getDataRelationships_returnsNull_whenRootIsNotPhysicalInstance() {
        // #493 : /variables sur un {id} qui n'est pas une PhysicalInstance (ici CodeList) ne renvoie rien.
        String agencyId = "fr.insee";
        String id = "not-a-pi";
        stubStandardSetWithSingleRoot(agencyId, id, CODE_LIST_TYPE, "CodeList");

        assertNull(ddiRepository.getDataRelationships(agencyId, id, null));
        verify(ddi3ToDdi4Converter, never()).convertDdi3ToDdi4(any(), any());
    }

    @Test
    void getDataRelationshipsXml_returnsNull_whenRootIsNotPhysicalInstance() {
        // #493 : même garde de type sur la variante XML.
        String agencyId = "fr.insee";
        String id = "not-a-pi";
        stubStandardSetWithSingleRoot(agencyId, id, CODE_LIST_TYPE, "CodeList");

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
        stubChildren(agencyId, groupId, lpType, new ItemReference(agencyId, "lp-1"));

        // Repository-wide LogicalProduct query carries the labels. lp-2 exists globally but is not
        // referenced by the group, so it must be filtered out.
        ColecticaItem lp1 =
                labelledItem("LogicalProduct", agencyId, "lp-1", "Produit Logique 1", "2025-01-01T00:00:00", 1L);
        ColecticaItem lp2 =
                labelledItem("LogicalProduct", agencyId, "lp-2", "Produit hors groupe", "2025-01-01T00:00:00", 2L);
        when(colecticaClient.query(anyList())).thenReturn(queryResponse(lp1, lp2));

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
        stubChildren(agencyId, groupId, lpType);

        List<PartialLogicalProduct> result = ddiRepository.getLogicalProductsByGroup(agencyId, groupId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCodeListSchemesByLogicalProduct_keepsOnlySchemesReferencedByLogicalProduct() {
        String agencyId = "fr.insee";
        String logicalProductId = "lp-1";
        String clsType = "4193d389-b5ae-4368-b399-cd5a7ee3653c";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeListScheme", clsType));

        // Colectica returns only the CodeListScheme directly referenced by the logical product,
        // filtered server-side by item type.
        stubChildren(agencyId, logicalProductId, clsType, new ItemReference(agencyId, "cls-1"));

        // Repository-wide CodeListScheme query carries the labels. cls-2 exists globally but is not
        // referenced by the logical product, so it must be filtered out.
        ColecticaItem cls1 = labelledItem("CodeListScheme", agencyId, "cls-1", "Schéma 1", "2025-01-01T00:00:00", 1L);
        ColecticaItem cls2 =
                labelledItem("CodeListScheme", agencyId, "cls-2", "Schéma hors LP", "2025-01-01T00:00:00", 2L);
        when(colecticaClient.query(anyList())).thenReturn(queryResponse(cls1, cls2));

        List<PartialCodeListScheme> result =
                ddiRepository.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);

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
        stubChildren(agencyId, logicalProductId, clsType);

        List<PartialCodeListScheme> result =
                ddiRepository.getCodeListSchemesByLogicalProduct(agencyId, logicalProductId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCodeListSchemes_returnsAllSchemesWithLabels() {
        String agencyId = "fr.insee";
        String clsType = "4193d389-b5ae-4368-b399-cd5a7ee3653c";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeListScheme", clsType));

        ColecticaItem cls1 = labelledItem("CodeListScheme", agencyId, "cls-1", "Schéma 1", "2025-01-01T00:00:00", 1L);
        ColecticaItem cls2 = labelledItem("CodeListScheme", agencyId, "cls-2", "Schéma 2", "2025-01-01T00:00:00", 2L);
        when(colecticaClient.query(List.of(clsType))).thenReturn(queryResponse(cls1, cls2));

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
        stubChildren(agencyId, codeListSchemeId, codeListType, new ItemReference(agencyId, "code-list-1"));

        // Repository-wide CodeList query carries the labels. code-list-2 exists globally but is not
        // referenced by the scheme, so it must be filtered out.
        ColecticaItem codeList1 =
                labelledItem("CodeList", agencyId, "code-list-1", "Liste 1", "2025-01-01T00:00:00", 1L);
        ColecticaItem codeList2 =
                labelledItem("CodeList", agencyId, "code-list-2", "Liste hors scheme", "2025-01-01T00:00:00", 2L);
        when(colecticaClient.query(anyList())).thenReturn(queryResponse(codeList1, codeList2));

        List<PartialCodesList> result = ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("code-list-1", result.get(0).id());
        assertEquals("Liste 1", result.get(0).label());
    }

    @Test
    void getCodeListsByCodeListScheme_versionDateComesFromItemXml() {
        // Comme pour les mutualisées : le versionDate fiable est lu depuis le XML de l'item
        // (item/_getList), pas depuis l'enveloppe _query.
        String agencyId = "fr.insee";
        String codeListSchemeId = "cls-1";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeList", codeListType));
        stubChildren(agencyId, codeListSchemeId, codeListType, new ItemReference(agencyId, "code-list-1"));

        ColecticaItem codeList1 =
                labelledItem("CodeList", agencyId, "code-list-1", "Liste 1", "0001-01-01T00:00:00", 1L);
        when(colecticaClient.query(anyList())).thenReturn(queryResponse(codeList1));

        String xml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<CodeList xmlns=\"ddi:logicalproduct:3_3\" versionDate=\"2026-06-29T14:26:32.961778\">"
                + "<r:URN>urn:ddi:fr.insee:code-list-1:1</r:URN></CodeList></Fragment>";
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(
                    codeListType, agencyId, 1, "code-list-1", xml, null, null, false, false, false, "DDI")
        });

        List<PartialCodesList> result = ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);

        assertEquals(1, result.size());
        assertEquals("code-list-1", result.get(0).id());
        assertEquals("2026-06-29 14:26:32", utcSeconds(result.get(0).versionDate()));
    }

    @Test
    void getCodeListsByCodeListScheme_returnsEmptyWhenSchemeHasNoCodeList() {
        String agencyId = "fr.insee";
        String codeListSchemeId = "cls-empty";
        String codeListType = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeList", codeListType));
        stubChildren(agencyId, codeListSchemeId, codeListType);

        List<PartialCodesList> result = ddiRepository.getCodeListsByCodeListScheme(agencyId, codeListSchemeId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- getMissingCodesListsByGroup (valeurs sentinelles, cf. #1566) ---

    private static final String LOGICAL_PRODUCT_TYPE = "965c8d28-7d48-4950-bea7-04b27e52bb9b";
    private static final String MANAGED_REPRESENTATION_SCHEME_TYPE = "16d4d829-41e1-4677-aa17-81190b6a0e66";
    private static final String MANAGED_MISSING_VALUES_REPRESENTATION_TYPE = "c29c3125-2a53-4179-8fa6-aa3beb2bb5ed";
    private static final String CODE_LIST_ITEM_TYPE = "8b108ef8-b642-4484-9c49-f88e4bf7cf1d";

    private void mockMissingCodesItemTypes() {
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "LogicalProduct", LOGICAL_PRODUCT_TYPE,
                        "ManagedRepresentationScheme", MANAGED_REPRESENTATION_SCHEME_TYPE,
                        "ManagedMissingValuesRepresentation", MANAGED_MISSING_VALUES_REPRESENTATION_TYPE,
                        "CodeList", CODE_LIST_ITEM_TYPE));
    }

    @Test
    void getMissingCodesListsByGroup_walksManagedRepresentationChainAndDeduplicates() {
        // Group → LogicalProduct → ManagedRepresentationScheme → ManagedMissingValuesRepresentation
        // → CodeList, chaque étape en bysubject filtré par type côté serveur. code-list-1 est
        // référencée par les deux MMVR : elle ne doit sortir qu'une fois.
        String agencyId = "fr.insee";
        String groupId = "group-1";
        mockMissingCodesItemTypes();

        stubManagedMissingValuesChain(
                agencyId, groupId, new ItemReference(agencyId, "mmvr-1"), new ItemReference(agencyId, "mmvr-2"));
        stubChildren(agencyId, "mmvr-1", CODE_LIST_ITEM_TYPE, new ItemReference(agencyId, "code-list-1"));
        stubChildren(
                agencyId,
                "mmvr-2",
                CODE_LIST_ITEM_TYPE,
                new ItemReference(agencyId, "code-list-2"),
                new ItemReference(agencyId, "code-list-1"));

        ColecticaItem codeList1 =
                labelledItem("CodeList", agencyId, "code-list-1", "Sentinelles âge", "0001-01-01T00:00:00", 1L);
        ColecticaItem codeList2 =
                labelledItem("CodeList", agencyId, "code-list-2", "Sentinelles revenu", "0001-01-01T00:00:00", 1L);
        // Une CodeList du référentiel non référencée par un MMVR : écartée.
        ColecticaItem unrelated =
                labelledItem("CodeList", agencyId, "code-list-other", "Liste ordinaire", "0001-01-01T00:00:00", 1L);
        when(colecticaClient.query(List.of(CODE_LIST_ITEM_TYPE)))
                .thenReturn(queryResponse(codeList1, codeList2, unrelated));

        // versionDate fiable lu depuis le XML de l'item (comme les autres listings de CodeLists).
        String xml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<CodeList xmlns=\"ddi:logicalproduct:3_3\" versionDate=\"2026-06-29T14:26:32.961778\">"
                + "<r:URN>urn:ddi:fr.insee:code-list-1:1</r:URN></CodeList></Fragment>";
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            new ColecticaItemResponse(
                    CODE_LIST_ITEM_TYPE, agencyId, 1, "code-list-1", xml, null, null, false, false, false, "DDI")
        });

        List<PartialCodesList> result = ddiRepository.getMissingCodesListsByGroup(agencyId, groupId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(
                Set.of("code-list-1", "code-list-2"),
                result.stream().map(PartialCodesList::id).collect(Collectors.toSet()));
        PartialCodesList first = result.stream()
                .filter(cl -> "code-list-1".equals(cl.id()))
                .findFirst()
                .orElseThrow();
        assertEquals("Sentinelles âge", first.label());
        assertEquals("2026-06-29 14:26:32", utcSeconds(first.versionDate()));
    }

    /**
     * Valeurs sentinelles (#1566) : liste des MMVR réutilisables du groupe pour le sélecteur de
     * réutilisation — identité + libellé de chaque MMVR, et aperçu des codes (valeurs) de la
     * CodeList de sentinelles qu'elle référence.
     */
    @Test
    void getMissingValuesRepresentationsByGroup_returnsLabelsAndCodePreview() {
        String agencyId = "fr.insee";
        String groupId = "group-1";
        mockMissingCodesItemTypes();

        stubManagedMissingValuesChain(agencyId, groupId, new ItemReference(agencyId, "mmvr-1"));

        // Le fragment MMVR est récupéré et parsé : libellé + référence de la CodeList de sentinelles.
        lenient()
                .when(colecticaClient.getItem(agencyId, "mmvr-1", null))
                .thenReturn(new ColecticaItemResponse(
                        MANAGED_MISSING_VALUES_REPRESENTATION_TYPE,
                        agencyId,
                        1,
                        "mmvr-1",
                        "<mmvr/>",
                        null,
                        null,
                        false,
                        false,
                        false,
                        "DDI"));
        lenient()
                .when(ddi3ToDdi4Converter.toManagedMissingValuesRepresentation("<mmvr/>"))
                .thenReturn(new Ddi4ManagedMissingValuesRepresentation(
                        Ddi4ManagedMissingValuesRepresentation.TYPE,
                        CogsDate.ofDateTime("2026-06-29T14:26:32Z"),
                        "urn:ddi:fr.insee:mmvr-1:1",
                        agencyId,
                        "mmvr-1",
                        "1",
                        LangStrings.of("fr-FR", "Valeurs sentinelles NSP/REF"),
                        List.of(new CodeRepresentation(
                                CodeRepresentation.TYPE,
                                false,
                                Reference.of(agencyId, "cl-sentinelles", "1", "CodeList")))));

        // La CodeList référencée est récupérée et parsée pour l'aperçu des codes.
        lenient()
                .when(colecticaClient.getItem(agencyId, "cl-sentinelles", null))
                .thenReturn(new ColecticaItemResponse(
                        CODE_LIST_ITEM_TYPE,
                        agencyId,
                        1,
                        "cl-sentinelles",
                        "<cl/>",
                        null,
                        null,
                        false,
                        false,
                        false,
                        "DDI"));
        lenient()
                .when(ddi3ToDdi4Converter.toCodeList("<cl/>"))
                .thenReturn(new Ddi4CodeList(
                        Ddi4CodeList.TYPE,
                        CogsDate.ofDateTime("2026-06-29T14:26:32Z"),
                        "urn:ddi:fr.insee:cl-sentinelles:1",
                        agencyId,
                        "cl-sentinelles",
                        "1",
                        LangStrings.of("fr-FR", "Sentinelles"),
                        null,
                        List.of(
                                new Code(
                                        Code.TYPE,
                                        "urn:ddi:fr.insee:c1:1",
                                        agencyId,
                                        "c1",
                                        "1",
                                        Reference.of(agencyId, "cat-nsp", "1", "Category"),
                                        ValueType.of("NSP"),
                                        null),
                                new Code(
                                        Code.TYPE,
                                        "urn:ddi:fr.insee:c2:1",
                                        agencyId,
                                        "c2",
                                        "1",
                                        Reference.of(agencyId, "cat-ref", "1", "Category"),
                                        ValueType.of("REF"),
                                        null))));

        List<PartialMissingValuesRepresentation> result =
                ddiRepository.getMissingValuesRepresentationsByGroup(agencyId, groupId);

        assertEquals(1, result.size());
        PartialMissingValuesRepresentation mmvr = result.get(0);
        assertEquals("mmvr-1", mmvr.id());
        assertEquals(agencyId, mmvr.agency());
        assertEquals("1", mmvr.version());
        assertEquals("Valeurs sentinelles NSP/REF", mmvr.label());
        assertEquals("cl-sentinelles", mmvr.codeListId());
        assertEquals(List.of("NSP", "REF"), mmvr.codeValues());
    }

    @Test
    void getMissingCodesListsByGroup_returnsEmptyWithoutCodeListQueryWhenSchemeHasNoMissingRepresentation() {
        String agencyId = "fr.insee";
        String groupId = "group-1";
        mockMissingCodesItemTypes();

        stubManagedMissingValuesChain(agencyId, groupId);

        List<PartialCodesList> result = ddiRepository.getMissingCodesListsByGroup(agencyId, groupId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(colecticaClient, never()).query(anyList());
    }

    @Test
    void getVariablesUsingCodeList_returnsStudyUnitPhysicalInstanceVariableWithLabels() {
        String agencyId = "fr.insee";
        String codeListId = "cl-1";

        when(instanceConfiguration.itemTypes()).thenReturn(usageItemTypes(DATA_RELATIONSHIP_TYPE));

        // CodeList ← Variable ← DataRelationship ← PhysicalInstance ← StudyUnit.
        // Labels come from the /descriptions endpoint directly (findRelatedItems → ColecticaItem),
        // so no separate label query is made. DataRelationships are only intermediate (bare refs).
        stubRelatedItems(agencyId, codeListId, VARIABLE_TYPE, labelItem(VARIABLE_TYPE, agencyId, "var-1", "Sexe"));
        stubVariableUsageParents(agencyId);

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

        when(instanceConfiguration.itemTypes()).thenReturn(usageItemTypes(DATA_RELATIONSHIP_TYPE));
        stubRelatedItems(agencyId, codeListId, VARIABLE_TYPE);

        List<CodeListVariableUsage> result = ddiRepository.getVariablesUsingCodeList(agencyId, codeListId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCodeListsUsingCategory_returnsCodeListsJoinedToVariablesWithParents() {
        String agencyId = "fr.insee";
        String categoryId = "cat-1";
        // Le type Group n'est PAS dans la map itemTypes de la configuration réelle : l'impl doit
        // utiliser sa constante GROUP_ITEM_TYPE, pas types.get("Group").
        String groupType = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

        when(instanceConfiguration.itemTypes()).thenReturn(categoryUsageItemTypes());

        // Category ← CodeList (via les codes de la liste) : marche byobject, type-filtrée.
        stubRelatedItems(agencyId, categoryId, CODE_LIST_TYPE, labelItem(CODE_LIST_TYPE, agencyId, "cl-1", "Pays"));

        // CodeList ← Variable ← DataRelationship ← PhysicalInstance ← StudyUnit (même marche que
        // getVariablesUsingCodeList)…
        stubRelatedItems(agencyId, "cl-1", VARIABLE_TYPE, labelItem(VARIABLE_TYPE, agencyId, "var-1", "Sexe"));
        stubVariableUsageParents(agencyId);

        // …puis StudyUnit ← Group pour le niveau racine de l'arbre du front.
        stubRelatedItems(agencyId, "su-1", groupType, labelItem(groupType, agencyId, "grp-1", "Groupe démographie"));

        List<CategoryCodeListUsage> result = ddiRepository.getCodeListsUsingCategory(agencyId, categoryId);

        assertNotNull(result);
        assertEquals(1, result.size());
        CategoryCodeListUsage usage = result.get(0);
        assertEquals(new UsageItem(agencyId, "grp-1", "Groupe démographie"), usage.group());
        assertEquals(new UsageItem(agencyId, "su-1", "Recensement 2024"), usage.studyUnit());
        assertEquals(new UsageItem(agencyId, "pi-1", "Fichier détail"), usage.physicalInstance());
        assertEquals(new UsageItem(agencyId, "var-1", "Sexe"), usage.variable());
        assertEquals(new UsageItem(agencyId, "cl-1", "Pays"), usage.codeList());
    }

    @Test
    void getCodeListsUsingCategory_returnsListOnlyRowWhenNoVariableUsesTheList() {
        String agencyId = "fr.insee";
        String categoryId = "cat-1";

        when(instanceConfiguration.itemTypes()).thenReturn(categoryUsageItemTypes());

        stubRelatedItems(
                agencyId, categoryId, CODE_LIST_TYPE, labelItem(CODE_LIST_TYPE, agencyId, "cl-orpheline", "Pays"));
        stubRelatedItems(agencyId, "cl-orpheline", VARIABLE_TYPE);

        List<CategoryCodeListUsage> result = ddiRepository.getCodeListsUsingCategory(agencyId, categoryId);

        // La liste sans variable utilisatrice apparaît quand même (parents null).
        assertNotNull(result);
        assertEquals(1, result.size());
        CategoryCodeListUsage usage = result.get(0);
        assertEquals(new UsageItem(agencyId, "cl-orpheline", "Pays"), usage.codeList());
        assertNull(usage.group());
        assertNull(usage.studyUnit());
        assertNull(usage.physicalInstance());
        assertNull(usage.variable());
    }

    /**
     * Une catégorie très partagée (« Oui/Non ») traverse des dizaines de listes qui retombent sur
     * les mêmes fichiers : la StudyUnit d'une PhysicalInstance et le Group d'une StudyUnit ne
     * doivent être demandés qu'une fois pour tout l'appel, sans quoi la popup de confirmation
     * enchaîne autant d'allers-retours Colectica que de couples (liste, variable).
     */
    @Test
    void getCodeListsUsingCategory_resolvesEachStudyUnitAndGroupOnlyOnce() {
        String agencyId = "fr.insee";
        String categoryId = "cat-1";
        String groupType = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

        when(instanceConfiguration.itemTypes()).thenReturn(categoryUsageItemTypes());

        // Deux listes de codes utilisent la catégorie…
        stubRelatedItems(
                agencyId,
                categoryId,
                CODE_LIST_TYPE,
                labelItem(CODE_LIST_TYPE, agencyId, "cl-1", "Pays"),
                labelItem(CODE_LIST_TYPE, agencyId, "cl-2", "Pays de naissance"));

        // …chacune par une variable différente, mais du MÊME fichier, donc de la même StudyUnit
        // et du même Group.
        stubRelatedItems(agencyId, "cl-1", VARIABLE_TYPE, labelItem(VARIABLE_TYPE, agencyId, "var-1", "Sexe"));
        stubRelatedItems(agencyId, "cl-2", VARIABLE_TYPE, labelItem(VARIABLE_TYPE, agencyId, "var-2", "Âge"));
        when(colecticaClient.findRelatedDescriptions(
                        eq(RelationshipDirection.BY_OBJECT),
                        any(ItemReference.class),
                        eq(List.of(DATA_RELATIONSHIP_TYPE))))
                .thenReturn(List.of(new ItemReference(agencyId, "dr-1")));
        stubRelatedItems(
                agencyId,
                "dr-1",
                PHYSICAL_INSTANCE_TYPE,
                labelItem(PHYSICAL_INSTANCE_TYPE, agencyId, "pi-1", "Fichier détail"));
        stubRelatedItems(
                agencyId,
                "pi-1",
                STUDY_UNIT_ITEM_TYPE,
                labelItem(STUDY_UNIT_ITEM_TYPE, agencyId, "su-1", "Recensement 2024"));
        stubRelatedItems(agencyId, "su-1", groupType, labelItem(groupType, agencyId, "grp-1", "Groupe démographie"));

        List<CategoryCodeListUsage> result = ddiRepository.getCodeListsUsingCategory(agencyId, categoryId);

        // Les deux lignes sont bien renseignées jusqu'au Group…
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(usage -> new UsageItem(agencyId, "grp-1", "Groupe démographie").equals(usage.group())));
        // …mais la PhysicalInstance et la StudyUnit partagées n'ont été résolues qu'une fois.
        verify(colecticaClient, times(1))
                .findRelatedItems(
                        RelationshipDirection.BY_OBJECT,
                        new ItemReference(agencyId, "pi-1"),
                        List.of(STUDY_UNIT_ITEM_TYPE));
        verify(colecticaClient, times(1))
                .findRelatedItems(
                        RelationshipDirection.BY_OBJECT, new ItemReference(agencyId, "su-1"), List.of(groupType));
    }

    @Test
    void getCodeListsUsingCategory_returnsEmptyWhenNoCodeListUsesIt() {
        String agencyId = "fr.insee";
        String categoryId = "cat-unused";

        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("CodeList", CODE_LIST_TYPE));
        stubRelatedItems(agencyId, categoryId, CODE_LIST_TYPE);

        List<CategoryCodeListUsage> result = ddiRepository.getCodeListsUsingCategory(agencyId, categoryId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Valeurs sentinelles (#1566) : les usages d'une MMVR (variables qui la référencent) suivent la
     * même marche {@code byobject} que ceux d'une CodeList — MMVR ← Variable ← DataRelationship ←
     * PhysicalInstance ← StudyUnit. Alimente la règle lecture seule/écriture du front.
     */
    @Test
    void getVariablesUsingMissingValuesRepresentation_returnsUsagesWithLabels() {
        String agencyId = "fr.insee";
        String mmvrId = "mmvr-1";

        when(instanceConfiguration.itemTypes()).thenReturn(usageItemTypes(DATA_RELATIONSHIP_TYPE));

        stubRelatedItems(agencyId, mmvrId, VARIABLE_TYPE, labelItem(VARIABLE_TYPE, agencyId, "var-1", "Sexe"));
        stubVariableUsageParents(agencyId);

        List<CodeListVariableUsage> result =
                ddiRepository.getVariablesUsingMissingValuesRepresentation(agencyId, mmvrId);

        assertNotNull(result);
        assertEquals(1, result.size());
        CodeListVariableUsage usage = result.get(0);
        assertEquals("var-1", usage.variableId());
        assertEquals("Sexe", usage.variableLabel());
        assertEquals("pi-1", usage.physicalInstanceId());
        assertEquals("su-1", usage.studyUnitId());
    }

    @Test
    void getPhysicalInstanceSearchRows_joinsPiWithStudyUnitAndGroupLabels() {
        String agency = "agency1";
        stubAdvancedPhysicalInstance(agency, "pi-1", "Fichier détail");
        // Descente : Groups (query) -> StudyUnits (bysubject) -> PhysicalInstances (bysubject).
        stubSingleGroup(agency);
        stubStudyUnitsOfGroup(agency, labelItem(STUDY_UNIT_ITEM_TYPE, agency, "su-1", "Recensement 2024"));
        stubChildren(agency, "su-1", PHYSICAL_INSTANCE_TYPE, new ItemReference(agency, "pi-1"));

        List<PhysicalInstanceSearchRow> rows = ddiRepository.getPhysicalInstanceSearchRows();

        assertEquals(1, rows.size());
        PhysicalInstanceSearchRow row = rows.get(0);
        assertEquals("pi-1", row.id());
        assertEquals("Fichier détail", row.label());
        assertEquals("su-1", row.studyUnitId());
        assertEquals("Recensement 2024", row.studyUnitLabel());
        assertEquals("g1", row.groupId());
        assertEquals("Groupe BPE", row.groupLabel());
    }

    @Test
    void getPhysicalInstanceSearchRows_leavesParentsNullForPiAttachedToNoGroup() {
        String agency = "agency1";
        stubAdvancedPhysicalInstance(agency, "pi-9", "Orpheline");
        // Aucun groupe : la PI ne peut être rattachée -> orpheline (parents null), mais présente.
        when(colecticaClient.query(List.of(GROUP_ITEM_TYPE))).thenReturn(queryResponse());

        List<PhysicalInstanceSearchRow> rows = ddiRepository.getPhysicalInstanceSearchRows();

        assertEquals(1, rows.size());
        assertEquals("pi-9", rows.get(0).id());
        assertEquals("Orpheline", rows.get(0).label());
        assertNull(rows.get(0).studyUnitId());
        assertNull(rows.get(0).groupLabel());
    }

    @Test
    void getPhysicalInstanceSearchRows_keepsOnlyTheLatestVersionOfEachStudyUnit() {
        // Colectica indexe les relations par PAIRE VERSIONNÉE : une StudyUnit en 2 versions sous le
        // même groupe ressort deux fois de _query/relationship/bysubject/descriptions.
        String agency = "agency1";
        stubAdvancedPhysicalInstance(agency, "pi-1", "Fichier détail");
        stubSingleGroup(agency);
        stubStudyUnitsOfGroup(
                agency,
                versionedLabelItem(STUDY_UNIT_ITEM_TYPE, agency, "su-1", "Recensement 2024", 1),
                versionedLabelItem(STUDY_UNIT_ITEM_TYPE, agency, "su-1", "Recensement 2024 (v2)", 2));
        stubChildren(agency, "su-1", PHYSICAL_INSTANCE_TYPE, new ItemReference(agency, "pi-1"));

        List<PhysicalInstanceSearchRow> rows = ddiRepository.getPhysicalInstanceSearchRows();

        assertEquals(1, rows.size());
        assertEquals("pi-1", rows.get(0).id());
        assertEquals("su-1", rows.get(0).studyUnitId());
        assertEquals("Recensement 2024 (v2)", rows.get(0).studyUnitLabel());
        // Une seule descente par StudyUnit, pas une par version.
        verify(colecticaClient, times(1))
                .findRelatedDescriptions(
                        RelationshipDirection.BY_SUBJECT,
                        new ItemReference(agency, "su-1"),
                        List.of(PHYSICAL_INSTANCE_TYPE));
    }

    @Test
    void getPhysicalInstanceSearchRows_doesNotRepeatAPhysicalInstanceReferencedBySeveralRelationshipVersions() {
        // Même cause côté StudyUnit -> PhysicalInstance : une PI en plusieurs versions produit
        // autant de descriptions de relation, que le DTO ItemReference rend indiscernables.
        String agency = "agency1";
        stubAdvancedPhysicalInstance(agency, "pi-1", "Fichier détail");
        stubSingleGroup(agency);
        stubStudyUnitsOfGroup(agency, labelItem(STUDY_UNIT_ITEM_TYPE, agency, "su-1", "Recensement 2024"));
        stubChildren(
                agency,
                "su-1",
                PHYSICAL_INSTANCE_TYPE,
                new ItemReference(agency, "pi-1"),
                new ItemReference(agency, "pi-1"));

        List<PhysicalInstanceSearchRow> rows = ddiRepository.getPhysicalInstanceSearchRows();

        assertEquals(1, rows.size());
        assertEquals("pi-1", rows.get(0).id());
    }

    @Test
    void getStudyUnits_keepsOnlyTheLatestVersionOfEachStudyUnit() {
        when(colecticaClient.query(anyList()))
                .thenReturn(queryResponse(
                        versionedLabelItem(STUDY_UNIT_ITEM_TYPE, "fr.insee", "su-1", "Recensement 2024", 1),
                        versionedLabelItem(STUDY_UNIT_ITEM_TYPE, "fr.insee", "su-1", "Recensement 2024 (v3)", 3),
                        versionedLabelItem(STUDY_UNIT_ITEM_TYPE, "fr.insee", "su-1", "Recensement 2024 (v2)", 2),
                        versionedLabelItem(STUDY_UNIT_ITEM_TYPE, "fr.insee", "su-2", "BPE 2023", 1)));

        List<PartialStudyUnit> result = ddiRepository.getStudyUnits();

        assertEquals(2, result.size());
        assertEquals("su-1", result.get(0).id());
        assertEquals("Recensement 2024 (v3)", result.get(0).label());
        assertEquals("su-2", result.get(1).id());
    }

    @Test
    void getPhysicalInstancesViaAdvancedQuery_keepsOnlyTheLatestVersionOfEachPhysicalInstance() {
        String piType = "a51e85bb-6259-4488-8df2-f08cb43485f8";
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("PhysicalInstance", piType));
        when(colecticaClient.queryAdvanced(anyList()))
                .thenReturn(new ColecticaAdvancedResponse(
                        List.of(
                                new ColecticaAdvancedItem(
                                        "fr.insee",
                                        "pi-1",
                                        1,
                                        piType,
                                        false,
                                        Map.of("label", List.of(new LocalizedText("Fichier détail", "fr-FR"))),
                                        Map.of(),
                                        Map.of()),
                                new ColecticaAdvancedItem(
                                        "fr.insee",
                                        "pi-1",
                                        2,
                                        piType,
                                        false,
                                        Map.of("label", List.of(new LocalizedText("Fichier détail (v2)", "fr-FR"))),
                                        Map.of(),
                                        Map.of())),
                        2,
                        null));

        List<PartialPhysicalInstance> result = ddiRepository.getPhysicalInstancesViaAdvancedQuery();

        assertEquals(1, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Fichier détail (v2)", result.get(0).label());
    }

    @Test
    void getVariablesUsingCodeList_keepsOnlyTheLatestVersionOfEachVariableAndPhysicalInstance() {
        String agencyId = "fr.insee";
        String dataRelationshipType = "e5bf1809-e5f2-4e7a-b9f9-f1e7c6e0b1e0";
        when(instanceConfiguration.itemTypes()).thenReturn(usageItemTypes(dataRelationshipType));

        // La variable et la PI sont chacune en 2 versions : autant de descriptions de relation.
        stubRelatedItems(
                agencyId,
                "cl-1",
                VARIABLE_TYPE,
                versionedLabelItem(VARIABLE_TYPE, agencyId, "var-1", "Sexe", 1),
                versionedLabelItem(VARIABLE_TYPE, agencyId, "var-1", "Sexe (v2)", 2));
        stubParents(
                agencyId,
                "var-1",
                dataRelationshipType,
                new ItemReference(agencyId, "dr-1"),
                new ItemReference(agencyId, "dr-1"));
        stubRelatedItems(
                agencyId,
                "dr-1",
                PHYSICAL_INSTANCE_TYPE,
                versionedLabelItem(PHYSICAL_INSTANCE_TYPE, agencyId, "pi-1", "Fichier détail", 1),
                versionedLabelItem(PHYSICAL_INSTANCE_TYPE, agencyId, "pi-1", "Fichier détail (v2)", 2));
        stubRelatedItems(
                agencyId,
                "pi-1",
                STUDY_UNIT_ITEM_TYPE,
                versionedLabelItem(STUDY_UNIT_ITEM_TYPE, agencyId, "su-1", "Recensement 2024", 1),
                versionedLabelItem(STUDY_UNIT_ITEM_TYPE, agencyId, "su-1", "Recensement 2024 (v2)", 2));

        List<CodeListVariableUsage> result = ddiRepository.getVariablesUsingCodeList(agencyId, "cl-1");

        assertEquals(1, result.size());
        CodeListVariableUsage usage = result.get(0);
        assertEquals("Sexe (v2)", usage.variableLabel());
        assertEquals("Fichier détail (v2)", usage.physicalInstanceLabel());
        assertEquals("Recensement 2024 (v2)", usage.studyUnitLabel());
    }

    private static ColecticaItem versionedLabelItem(
            String itemType, String agency, String id, String label, int version) {
        return colecticaItem(itemType, agency, version, id, Map.of("fr-FR", label), null, null, true, 1L);
    }

    private static ColecticaItem labelItem(String itemType, String agency, String id, String label) {
        return versionedLabelItem(itemType, agency, id, label, 1);
    }

    private static final String STUDY_UNIT_ITEM_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";
    private static final String GROUP_ITEM_TYPE = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";

    @Test
    void updateFullPhysicalInstance_attachesNonMutualizedCodeListsToGroupCodeListScheme() {
        stubConvertedPhysicalInstance();

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "LogicalProduct",
                        "lp-type",
                        "CodeListScheme",
                        CODE_LIST_SCHEME_TYPE,
                        "CodeListGroup",
                        CODE_LIST_GROUP_TYPE,
                        "CodeList",
                        CODE_LIST_TYPE));

        // Mutualized package: walking it top-down (scheme → group → code list) reaches CL_MUT only;
        // CL_NEW is not part of the package tree.
        stubMutualizedPackageReachingOnlyClMut();

        // Parents: PI -> StudyUnit -> Group
        stubPhysicalInstanceParents();

        // Scheme resolution: Group -> LogicalProduct -> CodeListScheme
        stubChildren("fr.insee", "group-1", "lp-type", new ItemReference("fr.insee", "lp-1"));
        stubChildren("fr.insee", "lp-1", CODE_LIST_SCHEME_TYPE, new ItemReference("fr.insee", "CLS_1"));

        // Existing scheme already references CL_EXISTING.
        when(colecticaClient.getItem("fr.insee", "CLS_1", null))
                .thenReturn(storedItem("cls-type", "CLS_1", "<scheme/>"));
        Ddi4CodeListScheme parsedScheme = new Ddi4CodeListScheme(
                Ddi4CodeListScheme.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:CLS_1:1",
                "fr.insee",
                "CLS_1",
                "1",
                LangStrings.of("fr-FR", "Scheme"),
                new ArrayList<>(List.of(Reference.of("fr.insee", "CL_EXISTING", "1", "CodeList"))));
        when(ddi3ToDdi4Converter.toCodeListScheme("<scheme/>")).thenReturn(parsedScheme);

        ArgumentCaptor<Ddi4CodeListScheme> schemeCaptor = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        when(ddi4ToDdi3Converter.toCodeListSchemeItem(schemeCaptor.capture()))
                .thenReturn(ddi3Item("cls-type", "CLS_1", "<scheme-updated/>"));

        Ddi4CodeList clMut = codeList("CL_MUT", "mut");
        Ddi4CodeList clNew = codeList("CL_NEW", "new");
        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, null, List.of(clMut, clNew), null, null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // The merged scheme keeps CL_EXISTING and adds the non-mutualized CL_NEW, but not CL_MUT.
        assertThat(schemeCaptor.getValue().codeListReference())
                .extracting(Reference::id)
                .containsExactlyInAnyOrder("CL_EXISTING", "CL_NEW");

        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).contains("CLS_1");
    }

    @Test
    void updateFullPhysicalInstance_autoProvisionsGroupCodeListSchemeWhenGroupHasNone() {
        stubConvertedPhysicalInstance();

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of("LogicalProduct", "lp-type", "CodeListScheme", CODE_LIST_SCHEME_TYPE));

        // No mutualized package configured: every code list is non-mutualized.
        when(colecticaConfiguration.mutualizedCodesPackage()).thenReturn(null);

        // Parents: PI -> StudyUnit -> Group
        stubPhysicalInstanceParents();

        // The group has NO LogicalProduct at all -> no CodeListScheme reachable
        stubChildren("fr.insee", "group-1", "lp-type");

        // The existing group can be fetched and parsed (to re-register it with a LogicalProductReference)
        stubExistingGroup();

        // Capture the provisioned scheme / LP / re-registered group
        ArgumentCaptor<Ddi4CodeListScheme> schemeCaptor = ArgumentCaptor.forClass(Ddi4CodeListScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        when(ddi4ToDdi3Converter.toCodeListSchemeItem(schemeCaptor.capture()))
                .thenReturn(ddi3Item("cls-type", "CLS_AUTO", "<cls/>"));
        when(ddi4ToDdi3Converter.toLogicalProductItem(lpCaptor.capture()))
                .thenReturn(ddi3Item("lp-type", "LP_AUTO", "<lp/>"));
        when(ddi4ToDdi3Converter.toGroupItem(groupCaptor.capture(), anyString()))
                .thenReturn(ddi3Item("group-type", "group-1", "<group-updated/>"));

        Ddi4CodeList clNew = codeList("CL_NEW", "new");
        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, null, List.of(clNew), null, null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // A fresh CodeListScheme is created holding the new code list...
        Ddi4CodeListScheme scheme = schemeCaptor.getValue();
        assertThat(scheme.codeListReference()).extracting(Reference::id).containsExactly("CL_NEW");
        // ...filed under a fresh LogicalProduct that references it...
        Ddi4LogicalProduct lp = lpCaptor.getValue();
        assertThat(lp.codeListSchemeReference()).extracting(Reference::id).containsExactly(scheme.id());
        // ...and the group is re-registered pointing at that LogicalProduct.
        Ddi4Group group = groupCaptor.getValue();
        assertThat(group.logicalProductReference()).extracting(Reference::id).containsExactly(lp.id());
        assertThat(group.studyUnitReference()).extracting(Reference::id).containsExactly("su-1");

        // All three provisioned items ship in the same batch as the PhysicalInstance.
        assertThat(sentItems())
                .extracting(ColecticaItemResponse::identifier)
                .contains("pi-1", "CLS_AUTO", "LP_AUTO", "group-1");
    }

    /**
     * Valeurs sentinelles (#1566) : les MMVR du payload sont rangées dans le
     * ManagedRepresentationScheme existant du groupe (fusion des références, dédup incluse).
     */
    @Test
    void updateFullPhysicalInstance_filesManagedMissingValuesUnderExistingScheme() {
        // Scheme resolution: Group -> LogicalProduct -> ManagedRepresentationScheme
        stubGroupLogicalProductForManagedRepresentations();
        stubChildren("fr.insee", "lp-1", MANAGED_REPRESENTATION_SCHEME_TYPE, new ItemReference("fr.insee", "MRS_1"));

        // Existing scheme already references MMVR_EXISTING.
        lenient()
                .when(colecticaClient.getItem("fr.insee", "MRS_1", null))
                .thenReturn(storedItem("mrs-type", "MRS_1", "<mrs/>"));
        Ddi4ManagedRepresentationScheme parsedScheme = new Ddi4ManagedRepresentationScheme(
                Ddi4ManagedRepresentationScheme.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:MRS_1:1",
                "fr.insee",
                "MRS_1",
                "1",
                LangStrings.of("fr-FR", "MRS"),
                new ArrayList<>(
                        List.of(Reference.of("fr.insee", "MMVR_EXISTING", "1", "ManagedMissingValuesRepresentation"))));
        lenient()
                .when(ddi3ToDdi4Converter.toManagedRepresentationScheme("<mrs/>"))
                .thenReturn(parsedScheme);

        ArgumentCaptor<Ddi4ManagedRepresentationScheme> schemeCaptor =
                ArgumentCaptor.forClass(Ddi4ManagedRepresentationScheme.class);
        lenient()
                .when(ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(schemeCaptor.capture()))
                .thenReturn(ddi3Item("mrs-type", "MRS_1", "<mrs-updated/>"));

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", newMissingValuesRepresentationPayload());

        // The scheme item ships in the batch, merged with the pre-existing member reference.
        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).contains("MRS_1");
        assertThat(schemeCaptor.getValue().managedRepresentationReference())
                .extracting(Reference::id)
                .containsExactlyInAnyOrder("MMVR_EXISTING", "MMVR_NEW");
    }

    /**
     * Valeurs sentinelles (#1566) : quand le groupe n'a pas encore de ManagedRepresentationScheme,
     * il est auto-provisionné et rangé sous le LogicalProduct existant du groupe.
     */
    @Test
    void updateFullPhysicalInstance_autoProvisionsManagedRepresentationSchemeWhenGroupHasNone() {
        // The group exposes a LogicalProduct, but no ManagedRepresentationScheme under it.
        stubGroupLogicalProductForManagedRepresentations();
        stubChildren("fr.insee", "lp-1", MANAGED_REPRESENTATION_SCHEME_TYPE);

        // The existing LogicalProduct is fetched and completed with the new scheme reference.
        lenient()
                .when(colecticaClient.getItem("fr.insee", "lp-1", null))
                .thenReturn(storedItem("lp-type", "lp-1", "<lp/>"));
        Ddi4LogicalProduct parsedLp = new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:lp-1:1",
                "fr.insee",
                "lp-1",
                "1",
                LangStrings.of("fr-FR", "LP"),
                List.of(Reference.of("fr.insee", "CLS_1", "1", "CodeListScheme")),
                null,
                null,
                null);
        lenient().when(ddi3ToDdi4Converter.toLogicalProduct("<lp/>")).thenReturn(parsedLp);

        ArgumentCaptor<Ddi4ManagedRepresentationScheme> schemeCaptor =
                ArgumentCaptor.forClass(Ddi4ManagedRepresentationScheme.class);
        lenient()
                .when(ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(schemeCaptor.capture()))
                .thenReturn(ddi3Item("mrs-type", "MRS_AUTO", "<mrs/>"));
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        lenient()
                .when(ddi4ToDdi3Converter.toLogicalProductItem(lpCaptor.capture()))
                .thenReturn(ddi3Item("lp-type", "lp-1", "<lp-updated/>"));

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", newMissingValuesRepresentationPayload());

        // A fresh ManagedRepresentationScheme is created holding the MMVR reference...
        Ddi4ManagedRepresentationScheme scheme = schemeCaptor.getValue();
        assertThat(scheme.managedRepresentationReference())
                .extracting(Reference::id)
                .containsExactly("MMVR_NEW");
        // ...and the existing LogicalProduct now references it (its other schemes preserved).
        Ddi4LogicalProduct lp = lpCaptor.getValue();
        assertThat(lp.managedRepresentationSchemeReference())
                .extracting(Reference::id)
                .containsExactly(scheme.id());
        assertThat(lp.codeListSchemeReference()).extracting(Reference::id).containsExactly("CLS_1");
        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).contains("pi-1", "MRS_AUTO", "lp-1");
    }

    @Test
    void updateFullPhysicalInstance_doesNotTouchSchemeWhenAllCodeListsAreMutualized() {
        stubConvertedPhysicalInstance();

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "CodeListScheme",
                        CODE_LIST_SCHEME_TYPE,
                        "CodeListGroup",
                        CODE_LIST_GROUP_TYPE,
                        "CodeList",
                        CODE_LIST_TYPE));

        // CL_MUT belongs to the package tree (package → scheme → group → CL_MUT).
        stubMutualizedPackageReachingOnlyClMut();

        Ddi4CodeList clMut = codeList("CL_MUT", "mut");
        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, null, List.of(clMut), null, null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // No scheme resolution, no scheme item: only the converted items are sent.
        verify(colecticaClient, never()).getItem(anyString(), anyString(), any());
        verify(ddi4ToDdi3Converter, never()).toCodeListSchemeItem(any());
        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).containsExactly("pi-1");
    }

    @Test
    void updateFullPhysicalInstance_filesCategoriesUnderExistingGroupCategoryScheme() {
        stubConvertedPhysicalInstance();
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of("LogicalProduct", "lp-type", "CategoryScheme", "cats-type"));

        stubPhysicalInstanceParents();
        // Group -> LogicalProduct -> CategoryScheme
        stubChildren("fr.insee", "group-1", "lp-type", new ItemReference("fr.insee", "lp-1"));
        stubChildren("fr.insee", "lp-1", "cats-type", new ItemReference("fr.insee", "CATS_1"));

        when(colecticaClient.getItem("fr.insee", "CATS_1", null))
                .thenReturn(storedItem("cats-type", "CATS_1", "<cats/>"));
        Ddi4CategoryScheme parsed = new Ddi4CategoryScheme(
                Ddi4CategoryScheme.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:CATS_1:1",
                "fr.insee",
                "CATS_1",
                "1",
                LangStrings.of("fr-FR", "Cats"),
                new ArrayList<>(List.of(Reference.of("fr.insee", "CAT_OLD", "1", "Category"))));
        when(ddi3ToDdi4Converter.toCategoryScheme("<cats/>")).thenReturn(parsed);

        ArgumentCaptor<Ddi4CategoryScheme> schemeCaptor = ArgumentCaptor.forClass(Ddi4CategoryScheme.class);
        when(ddi4ToDdi3Converter.toCategorySchemeItem(schemeCaptor.capture()))
                .thenReturn(ddi3Item("cats-type", "CATS_1", "<cats-updated/>"));

        updateFullPhysicalInstanceWithNewCategory();

        assertThat(schemeCaptor.getValue().categoryReference())
                .extracting(Reference::id)
                .containsExactlyInAnyOrder("CAT_OLD", "CAT_NEW");
        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).contains("CATS_1");
    }

    @Test
    void updateFullPhysicalInstance_autoProvisionsStudyUnitVariableSchemeWhenMissing() {
        stubConvertedPhysicalInstance();
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of("LogicalProduct", "lp-type", "VariableScheme", "vs-type"));

        stubPhysicalInstanceParents();
        // StudyUnit has no LogicalProduct -> no VariableScheme
        stubChildren("fr.insee", "su-1", "lp-type");

        when(colecticaClient.getItem("fr.insee", "su-1", null)).thenReturn(storedItem("su-type", "su-1", "<su/>"));
        Ddi4StudyUnit parsedSu = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:su-1:1",
                "fr.insee",
                "su-1",
                "1",
                new Citation(LangStrings.of("fr-FR", "SU")),
                "http://id.insee.fr/operations/operation/op1",
                List.of(Reference.of("fr.insee", "pi-1", "1", "PhysicalInstance")));
        when(ddi3ToDdi4Converter.toStudyUnit("<su/>")).thenReturn(parsedSu);

        ArgumentCaptor<Ddi4VariableScheme> vsCaptor = ArgumentCaptor.forClass(Ddi4VariableScheme.class);
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        ArgumentCaptor<Ddi4StudyUnit> suCaptor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        when(ddi4ToDdi3Converter.toVariableSchemeItem(vsCaptor.capture()))
                .thenReturn(ddi3Item("vs-type", "VS_AUTO", "<vs/>"));
        when(ddi4ToDdi3Converter.toLogicalProductItem(lpCaptor.capture()))
                .thenReturn(ddi3Item("lp-type", "LP_AUTO", "<lp/>"));
        when(ddi4ToDdi3Converter.toStudyUnitItem(suCaptor.capture(), anyString()))
                .thenReturn(ddi3Item("su-type", "su-1", "<su-updated/>"));

        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, List.of(variable("VAR_1")), null, null, null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        assertThat(vsCaptor.getValue().variableReference())
                .extracting(Reference::id)
                .containsExactly("VAR_1");
        Ddi4LogicalProduct lp = lpCaptor.getValue();
        assertThat(lp.variableSchemeReference())
                .extracting(Reference::id)
                .containsExactly(vsCaptor.getValue().id());
        assertThat(lp.codeListSchemeReference()).isNullOrEmpty();
        Ddi4StudyUnit su = suCaptor.getValue();
        assertThat(su.logicalProductReferences()).extracting(Reference::id).containsExactly(lp.id());
        // the PhysicalInstanceReference already on the study unit is preserved
        assertThat(su.physicalInstanceReferences()).extracting(Reference::id).containsExactly("pi-1");

        assertThat(sentItems())
                .extracting(ColecticaItemResponse::identifier)
                .contains("pi-1", "VS_AUTO", "LP_AUTO", "su-1");
    }

    @Test
    void updateFullPhysicalInstance_savesWithoutSchemeFilingWhenPhysicalInstanceHasNoStudyUnitYet() {
        // Duplication step 1: the raw PUT of a duplicated PI (with variables) happens BEFORE the
        // instance is attached to a StudyUnit — parents resolution must not make the save fail.
        stubConvertedPhysicalInstance();

        stubParents("fr.insee", "pi-1", STUDY_UNIT_ITEM_TYPE);

        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, List.of(variable("VAR_1")), null, null, null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // The instance is saved as-is; the variable filing is simply skipped (no study unit yet).
        verify(ddi4ToDdi3Converter, never()).toVariableSchemeItem(any());
        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).containsExactly("pi-1");
    }

    @Test
    void updatePhysicalInstance_filesVariablesUsingRequestParentsWhenAttachingToStudyUnit() {
        // Duplication step 2: the PATCH attaches the PI to its StudyUnit in the same batch, so the
        // parents cannot be resolved through Colectica relationships yet — the request carries them.
        String agencyId = "fr.insee";
        String instanceId = "pi-1";
        UpdatePhysicalInstanceRequest updateRequest = new UpdatePhysicalInstanceRequest(
                "Copied PI", "Copied DR", "Copied LR", "su-1", "fr.insee", "group-1", "fr.insee");

        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "StudyUnit", STUDY_UNIT_ITEM_TYPE, "LogicalProduct", "lp-type", "VariableScheme", "vs-type"));
        when(instanceConfiguration.itemFormat()).thenReturn("fmt");

        // getPhysicalInstance: the freshly duplicated instance carries one variable
        Ddi4PhysicalInstance mockPhysicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:pi-1:1",
                agencyId,
                instanceId,
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Copied PI")),
                null);
        Ddi4Response mockDdi4Response = new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(mockPhysicalInstance),
                List.of(),
                List.of(variable("VAR_1")),
                List.of(),
                List.of(),
                null);
        stubSet(new ColecticaSetItem(instanceId, 1, agencyId));
        stubDescriptions(description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")));
        stubDdi3ToDdi4Conversion(mockDdi4Response);

        // StudyUnit fetched to inject the PhysicalInstanceReference
        String studyUnitXml = "<Fragment xmlns:r=\"ddi:reusable:3_3\" xmlns=\"ddi:instance:3_3\">"
                + "<StudyUnit xmlns=\"ddi:studyunit:3_3\" isUniversallyUnique=\"true\"/>"
                + "</Fragment>";
        when(colecticaClient.getItem("fr.insee", "su-1", null))
                .thenReturn(new ColecticaItemResponse(
                        STUDY_UNIT_ITEM_TYPE,
                        "fr.insee",
                        2,
                        "su-1",
                        studyUnitXml,
                        "2026-01-01T00:00:00",
                        "resp",
                        false,
                        false,
                        false,
                        "fmt"));

        // The StudyUnit from the request already files a VariableScheme (StudyUnit -> LP -> VS)
        stubChildren("fr.insee", "su-1", "lp-type", new ItemReference("fr.insee", "lp-1"));
        stubChildren("fr.insee", "lp-1", "vs-type", new ItemReference("fr.insee", "VS_1"));
        when(colecticaClient.getItem("fr.insee", "VS_1", null)).thenReturn(storedItem("vs-type", "VS_1", "<vs/>"));
        Ddi4VariableScheme parsedScheme = new Ddi4VariableScheme(
                Ddi4VariableScheme.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:VS_1:1",
                "fr.insee",
                "VS_1",
                "1",
                LangStrings.of("fr-FR", "VS"),
                new ArrayList<>(List.of(Reference.of("fr.insee", "VAR_OLD", "1", "Variable"))));
        when(ddi3ToDdi4Converter.toVariableScheme("<vs/>")).thenReturn(parsedScheme);

        ArgumentCaptor<Ddi4VariableScheme> vsCaptor = ArgumentCaptor.forClass(Ddi4VariableScheme.class);
        when(ddi4ToDdi3Converter.toVariableSchemeItem(vsCaptor.capture()))
                .thenReturn(ddi3Item("vs-type", "VS_1", "<vs-updated/>"));

        stubConvertedPhysicalInstance();
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");

        ddiRepository.updatePhysicalInstance(agencyId, instanceId, updateRequest);

        // The parents come from the request: no Colectica relationship lookup for them.
        verify(colecticaClient, never()).findRelatedDescriptions(eq(RelationshipDirection.BY_OBJECT), any(), anyList());
        // The variable is merged into the study unit's existing VariableScheme...
        assertThat(vsCaptor.getValue().variableReference())
                .extracting(Reference::id)
                .containsExactlyInAnyOrder("VAR_OLD", "VAR_1");
        // ...and the batch ships the PI, the updated scheme and the re-registered StudyUnit together.
        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).contains(instanceId, "VS_1", "su-1");
    }

    @Test
    void updateFullPhysicalInstance_filesProvisionedCategorySchemeUnderTheGroupExistingLogicalProduct() {
        stubConvertedPhysicalInstance();
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of("LogicalProduct", "lp-type", "CategoryScheme", "cats-type"));

        stubPhysicalInstanceParents();
        // The group already exposes a LogicalProduct (filing its CodeListScheme), but no CategoryScheme yet.
        stubChildren("fr.insee", "group-1", "lp-type", new ItemReference("fr.insee", "lp-1"));
        stubChildren("fr.insee", "lp-1", "cats-type");

        when(colecticaClient.getItem("fr.insee", "lp-1", null)).thenReturn(storedItem("lp-type", "lp-1", "<lp/>"));
        Ddi4LogicalProduct existingLp = new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:lp-1:1",
                "fr.insee",
                "lp-1",
                "1",
                LangStrings.of("fr-FR", "Logical Product"),
                List.of(Reference.of("fr.insee", "CLS_1", "1", "CodeListScheme")),
                null,
                null,
                List.of(Reference.of("fr.insee", "MRS_1", "1", "ManagedRepresentationScheme")));
        when(ddi3ToDdi4Converter.toLogicalProduct("<lp/>")).thenReturn(existingLp);

        ArgumentCaptor<Ddi4CategoryScheme> schemeCaptor = ArgumentCaptor.forClass(Ddi4CategoryScheme.class);
        when(ddi4ToDdi3Converter.toCategorySchemeItem(schemeCaptor.capture()))
                .thenReturn(ddi3Item("cats-type", "CATS_AUTO", "<cats/>"));
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        when(ddi4ToDdi3Converter.toLogicalProductItem(lpCaptor.capture()))
                .thenReturn(ddi3Item("lp-type", "lp-1", "<lp-updated/>"));

        updateFullPhysicalInstanceWithNewCategory();

        // The existing LogicalProduct is completed with the fresh CategoryScheme, keeping its
        // CodeListScheme and ManagedRepresentationScheme: all schemes stay under the SAME
        // Group > LogicalProduct.
        Ddi4LogicalProduct lp = lpCaptor.getValue();
        assertThat(lp.id()).isEqualTo("lp-1");
        assertThat(lp.codeListSchemeReference()).extracting(Reference::id).containsExactly("CLS_1");
        assertThat(lp.categorySchemeReference())
                .extracting(Reference::id)
                .containsExactly(schemeCaptor.getValue().id());
        assertThat(lp.managedRepresentationSchemeReference())
                .extracting(Reference::id)
                .containsExactly("MRS_1");
        // The group already references that LogicalProduct: no re-registration needed.
        verify(ddi4ToDdi3Converter, never()).toGroupItem(any(), anyString());

        assertThat(sentItems()).extracting(ColecticaItemResponse::identifier).contains("pi-1", "CATS_AUTO", "lp-1");
    }

    @Test
    void updateFullPhysicalInstance_reRegistersGroupOnceWhenBothCodeListAndCategorySchemesAreProvisioned() {
        stubConvertedPhysicalInstance();
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "LogicalProduct",
                        "lp-type",
                        "CodeListScheme",
                        CODE_LIST_SCHEME_TYPE,
                        "CategoryScheme",
                        "cats-type"));
        when(colecticaConfiguration.mutualizedCodesPackage()).thenReturn(null);

        stubPhysicalInstanceParents();
        // Group has no LogicalProduct -> neither CodeListScheme nor CategoryScheme
        stubChildren("fr.insee", "group-1", "lp-type");

        stubExistingGroup();

        when(ddi4ToDdi3Converter.toCodeListSchemeItem(any())).thenReturn(ddi3Item("cls-type", "CLS_AUTO", "<cls/>"));
        when(ddi4ToDdi3Converter.toCategorySchemeItem(any())).thenReturn(ddi3Item("cats-type", "CATS_AUTO", "<cats/>"));
        ArgumentCaptor<Ddi4LogicalProduct> lpCaptor = ArgumentCaptor.forClass(Ddi4LogicalProduct.class);
        when(ddi4ToDdi3Converter.toLogicalProductItem(lpCaptor.capture()))
                .thenReturn(ddi3Item("lp-type", "LP_AUTO", "<lp/>"));
        ArgumentCaptor<Ddi4Group> groupCaptor = ArgumentCaptor.forClass(Ddi4Group.class);
        when(ddi4ToDdi3Converter.toGroupItem(groupCaptor.capture(), anyString()))
                .thenReturn(ddi3Item("group-type", "group-1", "<group-updated/>"));

        Ddi4CodeList cl = codeList("CL_NEW", "cl");
        Ddi4Category cat = category("CAT_NEW", "cat");
        Ddi4Response ddi4 = new Ddi4Response("schema", null, null, null, null, List.of(cl), List.of(cat), null);

        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);

        // A SINGLE LogicalProduct is provisioned, filing both the CodeListScheme and the CategoryScheme...
        assertThat(lpCaptor.getAllValues()).hasSize(1);
        Ddi4LogicalProduct lp = lpCaptor.getValue();
        assertThat(lp.codeListSchemeReference()).hasSize(1);
        assertThat(lp.categorySchemeReference()).hasSize(1);
        // ...and the group is re-registered exactly once, pointing at that single LogicalProduct.
        verify(ddi4ToDdi3Converter, times(1)).toGroupItem(any(), anyString());
        assertThat(groupCaptor.getValue().logicalProductReference())
                .extracting(Reference::id)
                .containsExactly(lp.id());

        assertThat(sentItems())
                .extracting(ColecticaItemResponse::identifier)
                .contains("pi-1", "CLS_AUTO", "CATS_AUTO", "LP_AUTO", "group-1");
    }

    // ---- Shared fixtures ----

    private static final String SENTINEL_MMVR_XML =
            "<Fragment xmlns=\"ddi:instance:3_3\"><ManagedMissingValuesRepresentation/></Fragment>";
    private static final String SENTINEL_CODE_LIST_XML =
            "<Fragment xmlns=\"ddi:instance:3_3\"><CodeList ID=\"cl-sentinel\"/></Fragment>";

    private static ColecticaItem colecticaItem(
            String itemType,
            String agency,
            int version,
            String identifier,
            Map<String, String> itemName,
            Map<String, String> label,
            String versionDate,
            boolean isPublished,
            long transactionId) {
        return new ColecticaItem(
                null, // summary
                itemName, // itemName
                label, // label
                null, // description
                null, // versionRationale
                0, // metadataRank
                "test-repo", // repositoryName
                true, // isAuthoritative
                List.of(), // tags
                itemType, // itemType
                agency, // agencyId
                version, // version
                identifier, // identifier
                null, // item
                null, // notes
                versionDate, // versionDate
                null, // versionResponsibility
                isPublished, // isPublished
                false, // isDeprecated
                false, // isProvisional
                "DDI", // itemFormat
                transactionId, // transactionId
                0 // versionCreationType
                );
    }

    /** A published item (version 1) whose itemName and label carry the same French text. */
    private static ColecticaItem labelledItem(
            String itemType, String agency, String identifier, String labelFr, String versionDate, long transactionId) {
        Map<String, String> text = Map.of("fr-FR", labelFr);
        return colecticaItem(itemType, agency, 1, identifier, text, text, versionDate, true, transactionId);
    }

    private static ColecticaResponse queryResponse(ColecticaItem... items) {
        return new ColecticaResponse(List.of(items), items.length, items.length, null, null, null);
    }

    /** {@code <Fragment xmlns="ddi:instance:3_3"><element/></Fragment>} */
    private static String fragment(String element) {
        return "<Fragment xmlns=\"ddi:instance:3_3\"><" + element + "/></Fragment>";
    }

    /** An item description as returned by item/_getList, without version date nor format. */
    private static ColecticaItemResponse description(
            String itemType, String agency, int version, String identifier, String xml) {
        return new ColecticaItemResponse(
                itemType, agency, version, identifier, xml, null, null, false, false, false, null);
    }

    /** A StudyUnit fetched to be updated with a new PhysicalInstanceReference. */
    private static ColecticaItemResponse studyUnitToUpdate(String agency, int version, String identifier, String xml) {
        return new ColecticaItemResponse(
                STUDY_UNIT_TYPE,
                agency,
                version,
                identifier,
                xml,
                "2025-01-01T00:00:00",
                null,
                false,
                false,
                false,
                null);
    }

    /** An item already stored in Colectica, as fetched while filing items into their schemes. */
    private static ColecticaItemResponse storedItem(String itemType, String identifier, String xml) {
        return new ColecticaItemResponse(
                itemType, "fr.insee", 1, identifier, xml, "2026-01-01T00:00:00", "resp", false, false, false, "fmt");
    }

    private static Ddi3Response.Ddi3Item ddi3Item(String itemType, String identifier, String xml) {
        return new Ddi3Response.Ddi3Item(
                itemType, "fr.insee", "1", identifier, xml, "2026-01-01T00:00:00", "resp", false, false, false, "fmt");
    }

    private static Ddi3Response.Ddi3Item legacyDdi3Item(
            String itemType, String agency, String version, String identifier, String xml, boolean isPublished) {
        return new Ddi3Response.Ddi3Item(
                itemType,
                agency,
                version,
                identifier,
                xml,
                "2025-01-01T00:00:00",
                null,
                isPublished,
                false,
                false,
                "DDI");
    }

    private static String utcSeconds(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private static Map<String, String> creationItemTypes() {
        return Map.of(
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "DataRelationship", DATA_RELATIONSHIP_TYPE);
    }

    private static Map<String, String> creationItemTypesWithStudyUnit() {
        return Map.of(
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "DataRelationship", DATA_RELATIONSHIP_TYPE,
                "StudyUnit", STUDY_UNIT_TYPE);
    }

    private static Map<String, String> physicalInstanceItemTypesWithCodeLists() {
        return Map.of(
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "DataRelationship", DATA_RELATIONSHIP_TYPE,
                "Variable", VARIABLE_TYPE,
                "CodeList", CODE_LIST_TYPE,
                "Category", CATEGORY_TYPE);
    }

    private static Map<String, String> dataRelationshipItemTypesWithSentinels(String mmvrType) {
        return Map.of(
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "DataRelationship", DATA_RELATIONSHIP_TYPE,
                "Variable", VARIABLE_TYPE,
                "CodeList", CODE_LIST_TYPE,
                "Category", CATEGORY_TYPE,
                "ManagedMissingValuesRepresentation", mmvrType);
    }

    private static Map<String, String> usageItemTypes(String dataRelationshipType) {
        return Map.of(
                "Variable", VARIABLE_TYPE,
                "DataRelationship", dataRelationshipType,
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "StudyUnit", STUDY_UNIT_ITEM_TYPE);
    }

    private static Map<String, String> categoryUsageItemTypes() {
        return Map.of(
                "CodeList", CODE_LIST_TYPE,
                "Variable", VARIABLE_TYPE,
                "DataRelationship", DATA_RELATIONSHIP_TYPE,
                "PhysicalInstance", PHYSICAL_INSTANCE_TYPE,
                "StudyUnit", STUDY_UNIT_ITEM_TYPE);
    }

    private static Ddi4Response physicalInstanceResponse(
            String agencyId, String instanceId, Ddi4PhysicalInstance physicalInstance) {
        return new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(physicalInstance),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null);
    }

    private static Ddi4Response physicalInstanceResponse(
            String agencyId,
            String instanceId,
            Ddi4PhysicalInstance physicalInstance,
            Ddi4DataRelationship dataRelationship) {
        return new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, instanceId, "1", "PhysicalInstance")),
                List.of(physicalInstance),
                List.of(dataRelationship),
                List.of(),
                List.of(),
                List.of(),
                null);
    }

    /** The PhysicalInstance "Old Label" as read before an update, referencing DataRelationship dr-123. */
    private static Ddi4PhysicalInstance existingPhysicalInstance(String agencyId, String instanceId) {
        return new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + instanceId + ":1",
                agencyId,
                instanceId,
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Old Label")),
                List.of(Reference.of(agencyId, "dr-123", "1", "DataRelationship")));
    }

    /** The DataRelationship dr-123 as read before an update, holding LogicalRecord lr-123. */
    private static Ddi4DataRelationship existingDataRelationship(
            String agencyId, List<LangString> dataRelationshipLabel, List<LangString> logicalRecordLabel) {
        return new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-01-01T00:00:00"),
                "urn:ddi:fr.insee:dr-123:1",
                agencyId,
                "dr-123",
                "1",
                null,
                dataRelationshipLabel,
                List.of(new LogicalRecord(
                        LogicalRecord.TYPE,
                        "urn:ddi:fr.insee:lr-123:1",
                        agencyId,
                        "lr-123",
                        "1",
                        logicalRecordLabel,
                        null)));
    }

    private static Ddi4Response codeListResponse(
            String agencyId, String codeListId, String categoryId, String version) {
        Ddi4CodeList mockCodeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2024-10-31T10:43:38"),
                "urn:ddi:fr.insee:" + codeListId + ":" + version,
                agencyId,
                codeListId,
                version,
                LangStrings.of("fr-FR", "NAF rév. 2"),
                null,
                List.of());
        Ddi4Category mockCategory = new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime("2024-10-31T10:43:38"),
                "urn:ddi:fr.insee:" + categoryId + ":" + version,
                agencyId,
                categoryId,
                version,
                LangStrings.of("fr-FR", "Agriculture"));
        return new Ddi4Response(
                "ddi:4.0",
                List.of(Reference.of(agencyId, codeListId, version, "CodeList")),
                List.of(),
                List.of(),
                List.of(),
                List.of(mockCodeList),
                List.of(mockCategory),
                null);
    }

    private static Ddi4CodeList codeList(String id, String label) {
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + id + ":1",
                "fr.insee",
                id,
                "1",
                LangStrings.of("fr-FR", label),
                null,
                null);
    }

    private static Ddi4Category category(String id, String label) {
        return new Ddi4Category(
                Ddi4Category.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + id + ":1",
                "fr.insee",
                id,
                "1",
                LangStrings.of("fr-FR", label));
    }

    private static Ddi4Variable variable(String id) {
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:" + id + ":1",
                "fr.insee",
                id,
                "1",
                null,
                null,
                null,
                null,
                null,
                null);
    }

    /** A payload carrying only the new ManagedMissingValuesRepresentation MMVR_NEW. */
    private static Ddi4Response newMissingValuesRepresentationPayload() {
        Ddi4ManagedMissingValuesRepresentation mmvrNew = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:MMVR_NEW:1",
                "fr.insee",
                "MMVR_NEW",
                "1",
                LangStrings.of("fr-FR", "Valeurs sentinelles NSP/REF"),
                null);
        return new Ddi4Response("schema", null, null, null, null, null, null, List.of(mmvrNew));
    }

    /**
     * The Variable of a PhysicalInstance set followed by its sentinel values items: the MMVR, the
     * sentinel CodeList and its Category, plus a representation CodeList.
     */
    private static List<ColecticaItemResponse> variableWithSentinelValuesDescriptions(
            String agencyId, String mmvrType) {
        return List.of(
                description(VARIABLE_TYPE, agencyId, 1, "var-1", fragment("Variable")),
                description(mmvrType, agencyId, 1, "mmvr-1", SENTINEL_MMVR_XML),
                description(CODE_LIST_TYPE, agencyId, 1, "cl-sentinel", SENTINEL_CODE_LIST_XML),
                description(CATEGORY_TYPE, agencyId, 1, "cat-sentinel", fragment("Category ID=\"cat-sentinel\"")),
                description(
                        CODE_LIST_TYPE,
                        agencyId,
                        1,
                        "cl-representation",
                        fragment("CodeList ID=\"cl-representation\"")));
    }

    private void stubSet(ColecticaSetItem... items) {
        when(colecticaClient.getSet(anyString(), anyString(), any())).thenReturn(items);
    }

    private void stubDescriptions(ColecticaItemResponse... items) {
        when(colecticaClient.getDescriptions(anyList())).thenReturn(items);
    }

    private void stubDdi3ToDdi4Conversion(Ddi4Response response) {
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(any(Ddi3Response.class), eq("ddi:4.0")))
                .thenReturn(response);
    }

    /** The set is a single root item of the given type, with the standard item types configured. */
    private void stubStandardSetWithSingleRoot(String agencyId, String id, String itemType, String element) {
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
        stubSet(new ColecticaSetItem(id, 1, agencyId));
        stubDescriptions(description(itemType, agencyId, 1, id, fragment(element)));
    }

    /** A code list set holding the code list and one category, both at the given version. */
    private void stubCodeListSet(
            String agencyId,
            String codeListId,
            String categoryId,
            int version,
            String codeListXml,
            String categoryXml) {
        stubSet(
                new ColecticaSetItem(codeListId, version, agencyId),
                new ColecticaSetItem(categoryId, version, agencyId));
        stubDescriptions(
                description(CODE_LIST_TYPE, agencyId, version, codeListId, codeListXml),
                description("fa1d4dca-f6dc-4d80-8b94-de1063a64d6d", agencyId, version, categoryId, categoryXml));
    }

    /** Sentinel values: parsing of the MMVR and of the sentinel CodeList it references. */
    private void stubSentinelValuesConversion(String agencyId) {
        when(ddi3ToDdi4Converter.toManagedMissingValuesRepresentation(SENTINEL_MMVR_XML))
                .thenReturn(new Ddi4ManagedMissingValuesRepresentation(
                        Ddi4ManagedMissingValuesRepresentation.TYPE,
                        null,
                        null,
                        agencyId,
                        "mmvr-1",
                        "1",
                        null,
                        List.of(new CodeRepresentation(
                                CodeRepresentation.TYPE,
                                false,
                                Reference.of(agencyId, "cl-sentinel", "1", "CodeList")))));
        when(ddi3ToDdi4Converter.toCodeList(SENTINEL_CODE_LIST_XML))
                .thenReturn(new Ddi4CodeList(
                        Ddi4CodeList.TYPE,
                        null,
                        null,
                        agencyId,
                        "cl-sentinel",
                        "1",
                        null,
                        null,
                        List.of(new Code(
                                Code.TYPE,
                                null,
                                agencyId,
                                "code-1",
                                "1",
                                Reference.of(agencyId, "cat-sentinel", "1", "Category"),
                                null,
                                null))));
    }

    private void stubCreationConfiguration(Map<String, String> itemTypes) {
        when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");
        when(instanceConfiguration.itemTypes()).thenReturn(itemTypes);
        when(instanceConfiguration.itemFormat()).thenReturn("dc337820-af3a-4c0b-82f9-cf02535cde83");
    }

    /** The items are saved, and the PhysicalInstance read back afterwards has an empty set. */
    private void stubSaveWithEmptyReload() {
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");
        stubSet();
    }

    /** getPhysicalInstance reads the PhysicalInstance and its DataRelationship dr-123, without labels. */
    private void stubExistingPhysicalInstanceWithDataRelationship(String agencyId, String instanceId) {
        stubSet(new ColecticaSetItem(instanceId, 1, agencyId), new ColecticaSetItem("dr-123", 1, agencyId));
        stubDescriptions(
                description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")),
                description(DATA_RELATIONSHIP_TYPE, agencyId, 1, "dr-123", fragment("DataRelationship")));
        stubDdi3ToDdi4Conversion(physicalInstanceResponse(
                agencyId,
                instanceId,
                existingPhysicalInstance(agencyId, instanceId),
                existingDataRelationship(agencyId, null, null)));
    }

    /**
     * Stubs the read, conversion and save of a PhysicalInstance update whose DataRelationship and
     * LogicalRecord carry the given labels, and captures what is passed to the DDI4 to DDI3 converter.
     */
    private ArgumentCaptor<Ddi4Response> stubPhysicalInstanceLabelUpdate(
            String agencyId,
            String instanceId,
            List<LangString> dataRelationshipLabel,
            List<LangString> logicalRecordLabel) {
        stubSet(new ColecticaSetItem(instanceId, 1, agencyId));
        stubDescriptions(description(PHYSICAL_INSTANCE_TYPE, agencyId, 1, instanceId, fragment("PhysicalInstance")));
        stubDdi3ToDdi4Conversion(physicalInstanceResponse(
                agencyId,
                instanceId,
                existingPhysicalInstance(agencyId, instanceId),
                existingDataRelationship(agencyId, dataRelationshipLabel, logicalRecordLabel)));

        ArgumentCaptor<Ddi4Response> ddi4Captor = ArgumentCaptor.forClass(Ddi4Response.class);
        Ddi3Response.Ddi3Item mockItem = legacyDdi3Item(
                PHYSICAL_INSTANCE_TYPE, agencyId, "2", instanceId, "<PhysicalInstance></PhysicalInstance>", true);
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Captor.capture()))
                .thenReturn(new Ddi3Response(null, List.of(mockItem)));
        when(colecticaClient.createOrUpdateItems(any())).thenReturn("{}");
        return ddi4Captor;
    }

    private ColecticaCreateItemRequest sentRequest() {
        ArgumentCaptor<ColecticaCreateItemRequest> bodyCaptor =
                ArgumentCaptor.forClass(ColecticaCreateItemRequest.class);
        verify(colecticaClient).createOrUpdateItems(bodyCaptor.capture());
        return bodyCaptor.getValue();
    }

    private List<ColecticaItemResponse> sentItems() {
        return sentRequest().items();
    }

    /** A mutualized codes package is configured, along with the standard item types. */
    private void stubMutualizedPackage(String agencyId, String packageId) {
        when(colecticaConfiguration.mutualizedCodesPackage())
                .thenReturn(new ColecticaConfiguration.PackageRef(agencyId, packageId, 1));
        when(instanceConfiguration.itemTypes()).thenReturn(standardItemTypes());
    }

    /** package → scheme → group → code lists */
    private void stubPackageTree(
            String agencyId, String packageId, String schemeId, String groupId, ItemReference... codeLists) {
        stubChildren(agencyId, packageId, CODE_LIST_SCHEME_TYPE, new ItemReference(agencyId, schemeId));
        stubChildren(agencyId, schemeId, CODE_LIST_GROUP_TYPE, new ItemReference(agencyId, groupId));
        stubChildren(agencyId, groupId, CODE_LIST_TYPE, codeLists);
    }

    /** Mutualized package PKG: walking it top-down (scheme → group → code list) reaches CL_MUT only. */
    private void stubMutualizedPackageReachingOnlyClMut() {
        when(colecticaConfiguration.mutualizedCodesPackage())
                .thenReturn(new ColecticaConfiguration.PackageRef("fr.insee", "PKG", 1));
        stubPackageTree("fr.insee", "PKG", "SCHEME_M", "GROUP_M", new ItemReference("fr.insee", "CL_MUT"));
    }

    /** Group → LogicalProduct lp-1 → ManagedRepresentationScheme mrs-1 → the given MMVRs. */
    private void stubManagedMissingValuesChain(String agencyId, String groupId, ItemReference... representations) {
        stubChildren(agencyId, groupId, LOGICAL_PRODUCT_TYPE, new ItemReference(agencyId, "lp-1"));
        stubChildren(agencyId, "lp-1", MANAGED_REPRESENTATION_SCHEME_TYPE, new ItemReference(agencyId, "mrs-1"));
        stubChildren(agencyId, "mrs-1", MANAGED_MISSING_VALUES_REPRESENTATION_TYPE, representations);
    }

    /** child → parents, walked byobject and filtered by the parent type. */
    private void stubParents(String agencyId, String childId, String parentType, ItemReference... parents) {
        when(colecticaClient.findRelatedDescriptions(
                        RelationshipDirection.BY_OBJECT, new ItemReference(agencyId, childId), List.of(parentType)))
                .thenReturn(List.of(parents));
    }

    /** Items of the given type referencing the object, with their labels (byobject descriptions). */
    private void stubRelatedItems(String agencyId, String objectId, String itemType, ColecticaItem... items) {
        when(colecticaClient.findRelatedItems(
                        RelationshipDirection.BY_OBJECT, new ItemReference(agencyId, objectId), List.of(itemType)))
                .thenReturn(List.of(items));
    }

    /** var-1 ← DataRelationship dr-1 ← PhysicalInstance pi-1 ← StudyUnit su-1, with their labels. */
    private void stubVariableUsageParents(String agencyId) {
        stubParents(agencyId, "var-1", DATA_RELATIONSHIP_TYPE, new ItemReference(agencyId, "dr-1"));
        stubRelatedItems(
                agencyId,
                "dr-1",
                PHYSICAL_INSTANCE_TYPE,
                labelItem(PHYSICAL_INSTANCE_TYPE, agencyId, "pi-1", "Fichier détail"));
        stubRelatedItems(
                agencyId,
                "pi-1",
                STUDY_UNIT_ITEM_TYPE,
                labelItem(STUDY_UNIT_ITEM_TYPE, agencyId, "su-1", "Recensement 2024"));
    }

    /** The PhysicalInstance advanced query returns a single, unpublished PhysicalInstance. */
    private void stubAdvancedPhysicalInstance(String agency, String id, String label) {
        when(instanceConfiguration.itemTypes()).thenReturn(Map.of("PhysicalInstance", PHYSICAL_INSTANCE_TYPE));
        ColecticaAdvancedItem pi = new ColecticaAdvancedItem(
                agency,
                id,
                1,
                PHYSICAL_INSTANCE_TYPE,
                false,
                Map.of("label", List.of(new LocalizedText(label, "fr-FR"))),
                Map.of(),
                Map.of("isPublished", false));
        when(colecticaClient.queryAdvanced(anyList())).thenReturn(new ColecticaAdvancedResponse(List.of(pi), 1, null));
    }

    /** The repository holds a single Group g1 "Groupe BPE". */
    private void stubSingleGroup(String agency) {
        when(colecticaClient.query(List.of(GROUP_ITEM_TYPE)))
                .thenReturn(queryResponse(labelItem(GROUP_ITEM_TYPE, agency, "g1", "Groupe BPE")));
        when(colecticaClient.getDescriptions(anyList())).thenReturn(null);
    }

    private void stubStudyUnitsOfGroup(String agency, ColecticaItem... studyUnits) {
        when(colecticaClient.findRelatedItems(
                        RelationshipDirection.BY_SUBJECT,
                        new ItemReference(agency, "g1"),
                        List.of(STUDY_UNIT_ITEM_TYPE)))
                .thenReturn(List.of(studyUnits));
    }

    private void stubStudyUnitDescription(String agency, String id, String xml) {
        when(colecticaClient.query(anyList())).thenReturn(studyUnitQueryResponse(agency, id));
        when(colecticaClient.getDescriptions(List.of(new GetDescriptionsRequest.IdentifierRef(agency, id, 1))))
                .thenReturn(new ColecticaItemResponse[] {studyUnitItemResponse(agency, id, xml)});
    }

    /** The only StudyUnit of the repository belongs to another operation. */
    private void stubStudyUnitOfAnotherOperation(String agency, String id) {
        when(colecticaClient.query(anyList())).thenReturn(studyUnitQueryResponse(agency, id));
        when(colecticaClient.getDescriptions(anyList())).thenReturn(new ColecticaItemResponse[] {
            studyUnitItemResponse(agency, id, studyUnitFragment("http://id.insee.fr/operations/operation/other", ""))
        });
    }

    /** The converted PhysicalInstance produces at least one item so the save proceeds. */
    private void stubConvertedPhysicalInstance() {
        Ddi3Response.Ddi3Item piItem = ddi3Item("pi-type", "pi-1", "<pi/>");
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(any()))
                .thenReturn(
                        new Ddi3Response(new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")), List.of(piItem)));
    }

    /** Parents: PI pi-1 -> StudyUnit su-1 -> Group group-1 */
    private void stubPhysicalInstanceParents() {
        stubParents("fr.insee", "pi-1", STUDY_UNIT_ITEM_TYPE, new ItemReference("fr.insee", "su-1"));
        stubParents("fr.insee", "su-1", GROUP_ITEM_TYPE, new ItemReference("fr.insee", "group-1"));
    }

    /**
     * Saving MMVRs: the PhysicalInstance converts, its Group group-1 is resolved through its parents,
     * and exposes the LogicalProduct lp-1.
     */
    private void stubGroupLogicalProductForManagedRepresentations() {
        stubConvertedPhysicalInstance();
        when(instanceConfiguration.itemTypes())
                .thenReturn(Map.of(
                        "LogicalProduct",
                        "lp-type",
                        "ManagedRepresentationScheme",
                        MANAGED_REPRESENTATION_SCHEME_TYPE));
        // Parents: PI -> StudyUnit -> Group
        stubPhysicalInstanceParents();
        stubChildren("fr.insee", "group-1", "lp-type", new ItemReference("fr.insee", "lp-1"));
    }

    /** The existing Group group-1 can be fetched and parsed. */
    private void stubExistingGroup() {
        when(colecticaClient.getItem("fr.insee", "group-1", null))
                .thenReturn(storedItem("group-type", "group-1", "<group/>"));
        Ddi4Group parsedGroup = new Ddi4Group(
                Ddi4Group.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00"),
                "urn:ddi:fr.insee:group-1:1",
                "fr.insee",
                "group-1",
                "1",
                "resp",
                new Citation(LangStrings.of("fr-FR", "Group")),
                List.of(Reference.of("fr.insee", "su-1", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries");
        when(ddi3ToDdi4Converter.toGroup("<group/>")).thenReturn(parsedGroup);
    }

    private static void assertFirstOfTwoPhysicalInstances(List<PartialPhysicalInstance> result, String versionDate) {
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("pi-1", result.get(0).id());
        assertEquals("Instance Physique 1", result.get(0).label());
        assertEquals("agency1", result.get(0).agency());
        assertEquals(versionDate, utcSeconds(result.get(0).versionDate()));
    }

    /** Set items, all at version 1. */
    private static ColecticaSetItem[] setItems(String agencyId, String... identifiers) {
        ColecticaSetItem[] items = new ColecticaSetItem[identifiers.length];
        for (int i = 0; i < identifiers.length; i++) {
            items[i] = new ColecticaSetItem(identifiers[i], 1, agencyId);
        }
        return items;
    }

    private ArgumentCaptor<Ddi3Response> captureDdi3ToDdi4Conversion(Ddi4Response response) {
        ArgumentCaptor<Ddi3Response> captor = ArgumentCaptor.forClass(Ddi3Response.class);
        when(ddi3ToDdi4Converter.convertDdi3ToDdi4(captor.capture(), eq("ddi:4.0")))
                .thenReturn(response);
        return captor;
    }

    /** Mutualized package pkg-1 → scheme-1 → group-1 → the single given code list. */
    private void stubMutualizedPackageWithSingleCodeList(String agencyId, String codeListId) {
        stubMutualizedPackage(agencyId, "pkg-1");
        stubPackageTree(agencyId, "pkg-1", "scheme-1", "group-1", new ItemReference(agencyId, codeListId));
    }

    /** Saves the PhysicalInstance pi-1 with a payload carrying only the new Category CAT_NEW. */
    private void updateFullPhysicalInstanceWithNewCategory() {
        Ddi4Response ddi4 =
                new Ddi4Response("schema", null, null, null, null, null, List.of(category("CAT_NEW", "cat")), null);
        ddiRepository.updateFullPhysicalInstance("fr.insee", "pi-1", ddi4);
    }
}
