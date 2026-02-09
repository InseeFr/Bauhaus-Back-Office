package fr.insee.rmes.modules.ddi.physical_instances.webservice;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialGroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialPhysicalInstanceResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DdiResourcesTest {

    @Mock
    private DDIService ddiService;

    @Mock
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @Mock
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    private DdiResources ddiResources;

    @BeforeEach
    void setUp() {
        ddiResources = new DdiResources(ddiService, ddi4toDdi3ConverterService, ddi3toDdi4ConverterService);

        // Setup mock request context for ServletUriComponentsBuilder
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @AfterEach
    void tearDown() {
        // Clean up request context
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldGetPhysicalInstances() {
        List<PartialPhysicalInstance> expectedInstances = new ArrayList<>();
        expectedInstances.add(new PartialPhysicalInstance("pi-1", "Physical Instance 1", new Date(), "fr.insee"));
        expectedInstances.add(new PartialPhysicalInstance("pi-2", "Physical Instance 2", new Date(), "fr.insee"));
        when(ddiService.getPhysicalInstances()).thenReturn(expectedInstances);

        ResponseEntity<List<PartialPhysicalInstanceResponse>> response = ddiResources.getPhysicalInstances();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialPhysicalInstanceResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first instance data and links
        assertEquals("pi-1", result.getFirst().getId());
        assertEquals("Physical Instance 1", result.getFirst().getLabel());
        assertNotNull(result.getFirst().getLinks());
        assertEquals(1, result.getFirst().getLinks().toList().size());
        assertEquals("http://localhost:8080/ddi/physical-instance/fr.insee/pi-1", result.getFirst().getRequiredLink("self").getHref());

        // Verify second instance data and links
        assertEquals("pi-2", result.get(1).getId());
        assertEquals("Physical Instance 2", result.get(1).getLabel());
        assertNotNull(result.get(1).getLinks());
        assertEquals(1, result.get(1).getLinks().toList().size());
        assertEquals("http://localhost:8080/ddi/physical-instance/fr.insee/pi-2", result.get(1).getRequiredLink("self").getHref());
        
        verify(ddiService).getPhysicalInstances();
    }

    @Test
    void shouldGetGroups() {
        List<PartialGroup> expectedGroups = new ArrayList<>();
        expectedGroups.add(new PartialGroup("group-1", "Base permanente des équipements", new Date(), "fr.insee"));
        expectedGroups.add(new PartialGroup("group-2", "Recensement de la population", new Date(), "fr.insee"));
        when(ddiService.getGroups()).thenReturn(expectedGroups);

        ResponseEntity<List<PartialGroupResponse>> response = ddiResources.getGroups();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialGroupResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first group data and links
        assertEquals("group-1", result.getFirst().getId());
        assertEquals("Base permanente des équipements", result.getFirst().getLabel());
        assertNotNull(result.getFirst().getLinks());
        assertEquals(1, result.getFirst().getLinks().toList().size());
        assertEquals("http://localhost:8080/ddi/group/fr.insee/group-1", result.getFirst().getRequiredLink("self").getHref());

        // Verify second group data and links
        assertEquals("group-2", result.get(1).getId());
        assertEquals("Recensement de la population", result.get(1).getLabel());
        assertNotNull(result.get(1).getLinks());
        assertEquals(1, result.get(1).getLinks().toList().size());
        assertEquals("http://localhost:8080/ddi/group/fr.insee/group-2", result.get(1).getRequiredLink("self").getHref());

        verify(ddiService).getGroups();
    }

    @Test
    void shouldGetDdi4Group() {
        // Given
        String agencyId = "fr.insee";
        String id = "10a689ce-7006-429b-8e84-036b7787b422";
        Ddi4GroupResponse expectedResponse = createMockDdi4GroupResponse();
        when(ddiService.getDdi4Group(agencyId, id)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4GroupResponse> result = ddiResources.getDdi4Group(agencyId, id);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4GroupResponse responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());
        assertEquals(1, responseBody.group().size());
        assertEquals(2, responseBody.studyUnit().size());
        assertEquals("10a689ce-7006-429b-8e84-036b7787b422", responseBody.group().get(0).id());

        verify(ddiService).getDdi4Group(agencyId, id);
    }

    @Test
    void shouldGetDdi4PhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String id = "9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd";
        Ddi4Response expectedResponse = createMockDdi4Response();
        when(ddiService.getDdi4PhysicalInstance(agencyId, id)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.getDdi4PhysicalInstance(agencyId, id);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());
        assertEquals(1, responseBody.physicalInstance().size());
        assertEquals(1, responseBody.dataRelationship().size());
        assertEquals("9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd", responseBody.physicalInstance().get(0).id());

        verify(ddiService).getDdi4PhysicalInstance(agencyId, id);
    }

    @Test
    void shouldUpdatePhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "test-id";
        UpdatePhysicalInstanceRequest request = new UpdatePhysicalInstanceRequest(
            "Updated Physical Instance Label",
            "Updated DataRelationship Label",
            "Updated LogicalRecord Label"
        );
        Ddi4Response expectedResponse = createMockDdi4Response();
        when(ddiService.updatePhysicalInstance(agencyId, instanceId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.updatePhysicalInstance(agencyId, instanceId, request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());

        verify(ddiService).updatePhysicalInstance(agencyId, instanceId, request);
    }

    @Test
    void shouldUpdatePhysicalInstanceWithPartialData() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "test-id";
        UpdatePhysicalInstanceRequest request = new UpdatePhysicalInstanceRequest(
            "Updated Label Only",
            null,
            null
        );
        Ddi4Response expectedResponse = createMockDdi4Response();
        when(ddiService.updatePhysicalInstance(agencyId, instanceId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.updatePhysicalInstance(agencyId, instanceId, request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());

        verify(ddiService).updatePhysicalInstance(agencyId, instanceId, request);
    }

    @Test
    void shouldReplacePhysicalInstance() {
        // Given
        String agencyId = "fr.insee";
        String instanceId = "test-id";
        Ddi4Response request = createMockDdi4Response(); // Use full Ddi4Response for PUT
        Ddi4Response expectedResponse = createMockDdi4Response();
        when(ddiService.updateFullPhysicalInstance(agencyId, instanceId, request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.replacePhysicalInstance(agencyId, instanceId, request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());

        verify(ddiService).updateFullPhysicalInstance(agencyId, instanceId, request);
    }

    @Test
    void shouldCreatePhysicalInstance() {
        // Given
        CreatePhysicalInstanceRequest request = new CreatePhysicalInstanceRequest(
            "New Physical Instance Label",
            "New DataRelationship Label",
            "New LogicalRecord Label"
        );
        Ddi4Response expectedResponse = createMockDdi4Response();
        when(ddiService.createPhysicalInstance(request)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.createPhysicalInstance(request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());
        assertNotNull(responseBody.physicalInstance());
        assertEquals(1, responseBody.physicalInstance().size());

        verify(ddiService).createPhysicalInstance(request);
    }

    @Test
    void shouldConvertDdi4ToDdi3() {
        // Given
        Ddi4Response ddi4Request = createMockDdi4Response();
        String expectedXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <ddi:FragmentInstance xmlns:r="ddi:reusable:3_3" xmlns:ddi="ddi:instance:3_3">
              <ddi:TopLevelReference>
                <r:Agency>fr.insee</r:Agency>
                <r:ID>test-id</r:ID>
                <r:Version>1</r:Version>
                <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
              </ddi:TopLevelReference>
              <ddi:Fragment xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2024-06-03T14:29:23.4049817Z">
                  <r:URN>urn:ddi:fr.insee:9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd:1</r:URN>
                  <r:Agency>fr.insee</r:Agency>
                  <r:ID>test-id</r:ID>
                  <r:Version>1</r:Version>
                </PhysicalInstance>
              </ddi:Fragment>
            </ddi:FragmentInstance>
            """;
        when(ddi4toDdi3ConverterService.convertDdi4ToDdi3Xml(ddi4Request)).thenReturn(expectedXml);

        // When
        ResponseEntity<String> result = ddiResources.convertDdi4ToDdi3(ddi4Request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, result.getHeaders().getContentType());

        String responseBody = result.getBody();
        assertNotNull(responseBody);

        // Verify XML structure
        assertTrue(responseBody.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"));
        assertTrue(responseBody.contains("<ddi:FragmentInstance"));
        assertTrue(responseBody.contains("<ddi:TopLevelReference>"));
        assertTrue(responseBody.contains("<r:Agency>fr.insee</r:Agency>"));
        assertTrue(responseBody.contains("<r:ID>test-id</r:ID>"));
        assertTrue(responseBody.contains("<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>"));
        assertTrue(responseBody.contains("<ddi:Fragment"));
        assertTrue(responseBody.contains("</ddi:FragmentInstance>"));

        verify(ddi4toDdi3ConverterService).convertDdi4ToDdi3Xml(ddi4Request);
    }

    @Test
    void shouldConvertDdi3ToDdi4() {
        // Given
        Ddi3Response ddi3Request = createMockDdi3Response();
        Ddi4Response expectedDdi4Response = createMockDdi4Response();
        when(ddi3toDdi4ConverterService.convertDdi3ToDdi4(eq(ddi3Request), anyString())).thenReturn(expectedDdi4Response);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.convertDdi3ToDdi4(ddi3Request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("test-schema", responseBody.schema());
        assertNotNull(responseBody.physicalInstance());
        assertEquals(1, responseBody.physicalInstance().size());

        Ddi4PhysicalInstance physicalInstance = responseBody.physicalInstance().get(0);
        assertEquals("9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd", physicalInstance.id());
        assertEquals("fr.insee", physicalInstance.agency());

        verify(ddi3toDdi4ConverterService).convertDdi3ToDdi4(eq(ddi3Request), anyString());
    }

    @Test
    void shouldConvertEmptyDdi4ToDdi3() {
        // Given
        Ddi4Response emptyDdi4 = new Ddi4Response(
            "file:/jsonSchema.json",
            null, null, null, null, null, null
        );
        String emptyXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<ddi:FragmentInstance xmlns:r=\"ddi:reusable:3_3\" xmlns:ddi=\"ddi:instance:3_3\"/>";
        when(ddi4toDdi3ConverterService.convertDdi4ToDdi3Xml(emptyDdi4)).thenReturn(emptyXml);

        // When
        ResponseEntity<String> result = ddiResources.convertDdi4ToDdi3(emptyDdi4);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, result.getHeaders().getContentType());

        String responseBody = result.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("<ddi:FragmentInstance"));

        verify(ddi4toDdi3ConverterService).convertDdi4ToDdi3Xml(emptyDdi4);
    }

    @Test
    void shouldConvertEmptyDdi3ToDdi4() {
        // Given
        Ddi3Response emptyDdi3 = new Ddi3Response(
            new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
            List.of()
        );
        Ddi4Response emptyDdi4 = new Ddi4Response(
            "file:/jsonSchema.json",
            null, null, null, null, null, null
        );
        when(ddi3toDdi4ConverterService.convertDdi3ToDdi4(eq(emptyDdi3), anyString())).thenReturn(emptyDdi4);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.convertDdi3ToDdi4(emptyDdi3);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        Ddi4Response responseBody = result.getBody();
        assertNotNull(responseBody);
        assertEquals("file:/jsonSchema.json", responseBody.schema());

        verify(ddi3toDdi4ConverterService).convertDdi3ToDdi4(eq(emptyDdi3), anyString());
    }

    private Ddi4Response createMockDdi4Response() {
        // Create mock objects
        StringValue titleStringValue = new StringValue("fr-FR", "Fichier thl-CASD");
        Title title = new Title(titleStringValue);
        Citation citation = new Citation(title);

        DataRelationshipReference dataRelRef = new DataRelationshipReference(
            "fr.insee", "d8283793-e88d-4cc7-a697-2951054e9a3a", "1", "DataRelationship"
        );

        Ddi4PhysicalInstance physicalInstance = new Ddi4PhysicalInstance(
            "true", "2024-06-03T14:29:23.4049817Z",
            "urn:ddi:fr.insee:9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd:1",
            "fr.insee", "9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd", "1",
            null, citation, dataRelRef
        );

        StringValue drNameStringValue = new StringValue("fr-FR", "Dessin de fichier thl-CASD");
        DataRelationshipName drName = new DataRelationshipName(drNameStringValue);

        Ddi4DataRelationship dataRelationship = new Ddi4DataRelationship(
            "true", "2024-06-03T14:29:23.4049817Z",
            "urn:ddi:fr.insee:d8283793-e88d-4cc7-a697-2951054e9a3a:1",
            "fr.insee", "d8283793-e88d-4cc7-a697-2951054e9a3a", "1",
            null, drName, null, null
        );

        TopLevelReference topLevelRef = new TopLevelReference(
            "fr.insee", "9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd", "1", "PhysicalInstance"
        );

        return new Ddi4Response(
            "test-schema",
            List.of(topLevelRef),
            List.of(physicalInstance),
            List.of(dataRelationship),
            List.of(),
            List.of(),
            List.of()
        );
    }

    private Ddi3Response createMockDdi3Response() {
        String xmlFragment = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3"
                    isUniversallyUnique="true"
                    versionDate="2024-06-03T14:29:23.4049817Z">
                    <r:URN>urn:ddi:fr.insee:9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>test-id</r:ID>
                    <r:Version>1</r:Version>
                    <r:Citation>
                        <r:Title>
                            <r:String xml:lang="fr-FR">Fichier thl-CASD</r:String>
                        </r:Title>
                    </r:Citation>
                </PhysicalInstance>
            </Fragment>
            """;

        Ddi3Response.Ddi3Item item = new Ddi3Response.Ddi3Item(
            "a51e85bb-6259-4488-8df2-f08cb43485f8",
            "fr.insee",
            "1",
            "test-id",
            xmlFragment,
            "2024-06-03T14:29:23.4049817Z",
            "abcde",
            false,
            false,
            false,
            "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
        );

        return new Ddi3Response(
            new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
            List.of(item)
        );
    }

    private Ddi4GroupResponse createMockDdi4GroupResponse() {
        // Create mock citation
        StringValue titleStringValue = new StringValue("fr-FR", "Base permanente des équipements");
        Title title = new Title(titleStringValue);
        Citation citation = new Citation(title);

        // Create StudyUnit references
        StudyUnitReference suRef1 = new StudyUnitReference("fr.insee", "89f5e04d-da22-485f-9c08-5fbe452b6c90", "1", "StudyUnit");
        StudyUnitReference suRef2 = new StudyUnitReference("fr.insee", "820a7c14-0ac4-42bc-a8c1-d39f60e304ee", "1", "StudyUnit");

        // Create Group
        Ddi4Group group = new Ddi4Group(
            "true", "2025-01-09T09:00:00.000000Z",
            "urn:ddi:fr.insee:10a689ce-7006-429b-8e84-036b7787b422:1",
            "fr.insee", "10a689ce-7006-429b-8e84-036b7787b422", "1",
            "abcde", citation, List.of(suRef1, suRef2)
        );

        // Create StudyUnits
        StringValue su1TitleStringValue = new StringValue("fr-FR", "BPE 2021");
        Title su1Title = new Title(su1TitleStringValue);
        Citation su1Citation = new Citation(su1Title);

        Ddi4StudyUnit studyUnit1 = new Ddi4StudyUnit(
            "true", "2025-01-09T09:00:00.000000Z",
            "urn:ddi:fr.insee:89f5e04d-da22-485f-9c08-5fbe452b6c90:1",
            "fr.insee", "89f5e04d-da22-485f-9c08-5fbe452b6c90", "1",
            su1Citation
        );

        StringValue su2TitleStringValue = new StringValue("fr-FR", "BPE 2022");
        Title su2Title = new Title(su2TitleStringValue);
        Citation su2Citation = new Citation(su2Title);

        Ddi4StudyUnit studyUnit2 = new Ddi4StudyUnit(
            "true", "2025-01-09T09:00:00.000000Z",
            "urn:ddi:fr.insee:820a7c14-0ac4-42bc-a8c1-d39f60e304ee:1",
            "fr.insee", "820a7c14-0ac4-42bc-a8c1-d39f60e304ee", "1",
            su2Citation
        );

        // Create TopLevelReference
        TopLevelReference topLevelRef = new TopLevelReference(
            "fr.insee", "10a689ce-7006-429b-8e84-036b7787b422", "1", "Group"
        );

        return new Ddi4GroupResponse(
            "test-schema",
            List.of(topLevelRef),
            List.of(group),
            List.of(studyUnit1, studyUnit2)
        );
    }

}