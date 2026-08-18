package fr.insee.rmes.modules.ddi.physical_instances.webservice;


import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.CodeListSummaryResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PartialPhysicalInstanceResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PhysicalInstanceParentsResponse;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.PhysicalInstanceSearchResponse;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.RBAC;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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

    @Mock
    private DDIItemConvertService ddiItemConvertService;

    @Mock
    private UserProvider userProvider;

    @Mock
    private RbacFetcher rbacFetcher;

    @Mock
    private BauhausUriBuilder bauhausUriBuilder;

    private DdiResources ddiResources;

    @BeforeEach
    void setUp() {
        ddiResources = new DdiResources(ddiService, ddi4toDdi3ConverterService, ddi3toDdi4ConverterService, ddiItemConvertService, userProvider, rbacFetcher, bauhausUriBuilder, mock(Ddi4SchemaService.class));

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
    void getPhysicalInstanceCodesLists_usesDedicatedServiceCallAndMapsLabel() {
        // Le endpoint /codeslists doit interroger une méthode dédiée du service
        // (la PI complète n'inclut plus les CodeList) puis transformer la liste en
        // CodeListSummaryResponse(agencyId, id, label).
        String agencyId = "fr.insee";
        String id = "pi-1";
        Ddi4CodeList codeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,
                "urn:ddi:fr.insee:cl-1:1",
                agencyId, "cl-1", "1",
                LangStrings.of("fr-FR", "ma cl"),
                null,
                List.of()
        );
        when(ddiService.getPhysicalInstanceCodeLists(agencyId, id))
                .thenReturn(List.of(codeList));

        ResponseEntity<List<CodeListSummaryResponse>> result =
                ddiResources.getPhysicalInstanceCodesLists(agencyId, id);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());
        List<CodeListSummaryResponse> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("cl-1", body.get(0).id());
        assertEquals(agencyId, body.get(0).agencyId());
        assertEquals("ma cl", body.get(0).label());

        verify(ddiService).getPhysicalInstanceCodeLists(agencyId, id);
        verify(ddiService, never()).getDdi4PhysicalInstance(anyString(), anyString());
    }

    @Test
    void shouldGetMutualizedCodesList() {
        // Given
        String agencyId = "fr.insee";
        String id = "fc65a527-a04b-4505-85de-0a181e54dbad";
        Ddi4Response expectedResponse = createMockDdi4Response();
        when(ddiService.getMutualizedCodesList(agencyId, id)).thenReturn(expectedResponse);

        // When
        ResponseEntity<Ddi4Response> result = ddiResources.getMutualizedCodesList(agencyId, id);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());
        assertEquals(expectedResponse, result.getBody());
        verify(ddiService).getMutualizedCodesList(agencyId, id);
    }

    @Test
    void getMutualizedCodesLists_withoutCacheControl_doesNotEvictCache() {
        when(ddiService.getMutualizedCodesLists())
                .thenReturn(List.of(new PartialCodesList("cl-1", "ma cl", new Date(), "fr.insee")));

        ResponseEntity<List<CodeListSummaryResponse>> response =
                ddiResources.getMutualizedCodesLists(null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CodeListSummaryResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("cl-1", body.get(0).id());

        verify(ddiService, never()).evictMutualizedCodesListsCache();
        verify(ddiService).getMutualizedCodesLists();
    }

    @Test
    void getMutualizedCodesLists_exposesTechnicalNameAlongsideLabel() {
        when(ddiService.getMutualizedCodesLists())
                .thenReturn(List.of(new PartialCodesList(
                        "cl-1", "Libellé lisible", new Date(), "fr.insee", "CL_NOM_TECHNIQUE")));

        ResponseEntity<List<CodeListSummaryResponse>> response =
                ddiResources.getMutualizedCodesLists(null);

        List<CodeListSummaryResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("Libellé lisible", body.get(0).label());
        assertEquals("CL_NOM_TECHNIQUE", body.get(0).name());
    }

    @Test
    void getMutualizedCodesLists_exposesVersionDate() {
        Date versionDate = new Date(1_750_000_000_000L);
        when(ddiService.getMutualizedCodesLists())
                .thenReturn(List.of(new PartialCodesList(
                        "cl-1", "ma cl", versionDate, "fr.insee", "CL_NOM")));

        ResponseEntity<List<CodeListSummaryResponse>> response =
                ddiResources.getMutualizedCodesLists(null);

        List<CodeListSummaryResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(versionDate, body.get(0).versionDate());
    }

    @Test
    void getMutualizedCodesLists_withCacheControlNoCache_evictsCacheThenServesFreshList() {
        when(ddiService.getMutualizedCodesLists())
                .thenReturn(List.of(new PartialCodesList("cl-1", "ma cl", new Date(), "fr.insee")));

        ResponseEntity<List<CodeListSummaryResponse>> response =
                ddiResources.getMutualizedCodesLists("no-cache");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<CodeListSummaryResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());

        // The cache must be cleared *before* the list is (re)computed, otherwise the stale entry is served.
        InOrder inOrder = inOrder(ddiService);
        inOrder.verify(ddiService).evictMutualizedCodesListsCache();
        inOrder.verify(ddiService).getMutualizedCodesLists();
    }

    @Test
    void getMutualizedCodesLists_withCacheControlNoCacheIgnoringCase_evictsCache() {
        when(ddiService.getMutualizedCodesLists()).thenReturn(List.of());

        ddiResources.getMutualizedCodesLists("No-Cache, no-store");

        verify(ddiService).evictMutualizedCodesListsCache();
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
            "New LogicalRecord Label",
            null, null, null, null
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
        , null);
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
        , null);
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

    @Test
    void getItemXmlByVersion_shouldReturn200WithXml_whenItemExists() {
        String agency = "fr.insee";
        String id = "c05c0443-fc56-4069-9bea-a9c7300ae0a0";
        String version = "1";
        String xml = "<Fragment><PhysicalInstance/></Fragment>";
        when(ddiService.getItemXml(agency, id, version)).thenReturn(xml);

        ResponseEntity<String> response = ddiResources.getItemXmlByVersion(agency, id, version);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getItemXml(agency, id, version);
    }

    @Test
    void getItemXmlByVersion_shouldReturn404_whenItemNotFound() {
        when(ddiService.getItemXml("fr.insee", "unknown-id", "1")).thenReturn(null);

        ResponseEntity<String> response = ddiResources.getItemXmlByVersion("fr.insee", "unknown-id", "1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getItemJsonByVersion_shouldReturn200WithJson_whenItemExists() {
        String agency = "fr.insee";
        String id = "c05c0443-fc56-4069-9bea-a9c7300ae0a0";
        String version = "1";
        String xml = "<Fragment><PhysicalInstance/></Fragment>";
        ObjectNode expectedJson = new ObjectMapper().createObjectNode().put("ID", id);
        when(ddiService.getItemXml(agency, id, version)).thenReturn(xml);
        when(ddiItemConvertService.convert(xml)).thenReturn(expectedJson);

        ResponseEntity<String> response = ddiResources.getItemJsonByVersion(agency, id, version);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedJson.toString(), response.getBody());
        verify(ddiItemConvertService).convert(xml);
    }

    @Test
    void getItemJsonByVersion_shouldReturn404_whenItemNotFound() {
        when(ddiService.getItemXml("fr.insee", "unknown-id", "1")).thenReturn(null);

        ResponseEntity<String> response = ddiResources.getItemJsonByVersion("fr.insee", "unknown-id", "1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getItemXml_shouldReturn200WithXml_whenItemExists() {
        String agency = "fr.insee";
        String id = "c05c0443-fc56-4069-9bea-a9c7300ae0a0";
        String xml = "<Fragment><PhysicalInstance/></Fragment>";
        when(ddiService.getItemXml(agency, id)).thenReturn(xml);

        ResponseEntity<String> response = ddiResources.getItemXml(agency, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getItemXml(agency, id);
    }

    @Test
    void getItemXml_shouldReturn404_whenItemNotFound() {
        when(ddiService.getItemXml("fr.insee", "unknown-id")).thenReturn(null);

        ResponseEntity<String> response = ddiResources.getItemXml("fr.insee", "unknown-id");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getItemJson_shouldReturn200WithJson_whenItemExists() {
        String agency = "fr.insee";
        String id = "c05c0443-fc56-4069-9bea-a9c7300ae0a0";
        String xml = "<Fragment><PhysicalInstance/></Fragment>";
        ObjectNode expectedJson = new ObjectMapper().createObjectNode().put("ID", id);
        when(ddiService.getItemXml(agency, id)).thenReturn(xml);
        when(ddiItemConvertService.convert(xml)).thenReturn(expectedJson);

        ResponseEntity<String> response = ddiResources.getItemJson(agency, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedJson.toString(), response.getBody());
        verify(ddiItemConvertService).convert(xml);
    }

    @Test
    void getItemJson_shouldReturn404_whenItemNotFound() {
        when(ddiService.getItemXml("fr.insee", "unknown-id")).thenReturn(null);

        ResponseEntity<String> response = ddiResources.getItemJson("fr.insee", "unknown-id");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldGetPhysicalInstancesFilteredByStamp() throws Exception, MissingUserInformationException {
        List<PartialPhysicalInstance> filteredInstances = List.of(
                new PartialPhysicalInstance("pi-1", "Physical Instance 1", new Date(), "fr.insee")
        );
        User stampUser = new User("user-1", List.of("role-stamp"), Set.of("stamp-A"));
        when(userProvider.findUser()).thenReturn(Optional.of(stampUser));
        when(rbacFetcher.getApplicationActionStrategyByRole(any(), eq(RBAC.Module.DDI_PHYSICALINSTANCE), eq(RBAC.Privilege.READ)))
                .thenReturn(RBAC.Strategy.STAMP);
        when(ddiService.getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"))).thenReturn(filteredInstances);

        ResponseEntity<List<PartialPhysicalInstanceResponse>> response = ddiResources.getPhysicalInstances();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        List<PartialPhysicalInstanceResponse> result = response.getBody();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("pi-1", result.getFirst().getId());

        verify(ddiService).getPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));
        verify(ddiService, never()).getPhysicalInstances();
    }

    @Test
    void searchPhysicalInstances_mapsRowsWithResolvedParentLabels() {
        PhysicalInstanceSearchRow row = new PhysicalInstanceSearchRow(
                "fr.insee", "pi-1", "Instance A", new Date(),
                "fr.insee", "su-1", "Study One",
                "fr.insee", "g1", "Group One");
        when(ddiService.searchPhysicalInstances()).thenReturn(List.of(row));

        ResponseEntity<List<PhysicalInstanceSearchResponse>> response =
                ddiResources.searchPhysicalInstances();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        List<PhysicalInstanceSearchResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("pi-1", body.getFirst().id());
        assertEquals("Instance A", body.getFirst().label());
        assertEquals("Study One", body.getFirst().studyUnitLabel());
        assertEquals("Group One", body.getFirst().groupLabel());
        verify(ddiService).searchPhysicalInstances();
    }

    @Test
    void searchPhysicalInstances_appliesStampStrategy() throws MissingUserInformationException, RmesException {
        PhysicalInstanceSearchRow row = new PhysicalInstanceSearchRow(
                "fr.insee", "pi-1", "Instance A", new Date(),
                "fr.insee", "su-1", "Study One",
                "fr.insee", "g1", "Group One");
        User stampUser = new User("user-1", List.of("role-stamp"), Set.of("stamp-A"));
        when(userProvider.findUser()).thenReturn(Optional.of(stampUser));
        when(rbacFetcher.getApplicationActionStrategyByRole(any(), eq(RBAC.Module.DDI_PHYSICALINSTANCE), eq(RBAC.Privilege.READ)))
                .thenReturn(RBAC.Strategy.STAMP);
        when(ddiService.searchPhysicalInstancesFilteredByStamp(Set.of("stamp-A"))).thenReturn(List.of(row));

        ResponseEntity<List<PhysicalInstanceSearchResponse>> response =
                ddiResources.searchPhysicalInstances();

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(ddiService).searchPhysicalInstancesFilteredByStamp(Set.of("stamp-A"));
        verify(ddiService, never()).searchPhysicalInstances();
    }

    @Test
    void getPhysicalInstanceParents_serializesStampsField() {
        when(ddiService.getPhysicalInstanceParents("fr.insee", "pi-1"))
                .thenReturn(new PhysicalInstanceParents(
                        "fr.insee", "su-1", "Mon étude", "fr.insee", "grp-1", "Mon groupe", List.of("stamp-A", "stamp-B")));

        ResponseEntity<PhysicalInstanceParentsResponse> response =
                ddiResources.getPhysicalInstanceParents("fr.insee", "pi-1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        PhysicalInstanceParentsResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(List.of("stamp-A", "stamp-B"), body.stamps());
        assertEquals("Mon groupe", body.group().label());

        JsonNode json = new ObjectMapper().valueToTree(body);
        assertTrue(json.has("stamps"));
        assertEquals("stamp-A", json.get("stamps").get(0).asText());
        assertEquals("stamp-B", json.get("stamps").get(1).asText());
        assertEquals("Mon groupe", json.get("group").get("label").asText());
    }

    private Ddi4Response createMockDdi4Response() {
        List<LangString> title = LangStrings.of("fr-FR", "Fichier thl-CASD");
        Citation citation = new Citation(title);

        Reference dataRelRef = Reference.of(
            "fr.insee", "d8283793-e88d-4cc7-a697-2951054e9a3a", "1", "DataRelationship"
        );

        Ddi4PhysicalInstance physicalInstance = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
            CogsDate.ofDateTime("2024-06-03T14:29:23.4049817Z"),
            "urn:ddi:fr.insee:9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd:1",
            "fr.insee", "9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd", "1",
            null, citation, List.of(dataRelRef)
        );

        List<LangString> drLabel = LangStrings.of("fr-FR", "Dessin de fichier thl-CASD");

        Ddi4DataRelationship dataRelationship = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
            CogsDate.ofDateTime("2024-06-03T14:29:23.4049817Z"),
            "urn:ddi:fr.insee:d8283793-e88d-4cc7-a697-2951054e9a3a:1",
            "fr.insee", "d8283793-e88d-4cc7-a697-2951054e9a3a", "1",
            null, drLabel, null
        );

        Reference topLevelRef = Reference.of(
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
        , null);
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

    // --- #485 : GET /ddi/codelist/{agency}/{id}[/{version}] ---

    private static final String CL_AGENCY = "fr.insee";
    private static final String CL_ID = "fc65a527-a04b-4505-85de-0a181e54dbad";
    private static final String CL_VERSION = "2";

    private static Ddi4Response emptyDdi4() {
        return new Ddi4Response("ddi:4.0", null, null, null, null, null, null, null);
    }

    @Test
    void getCodeListXml_returns200WithXml() {
        String xml = "<ddi:FragmentInstance><Fragment><CodeList/></Fragment></ddi:FragmentInstance>";
        when(ddiService.getCodeListXml(CL_AGENCY, CL_ID, null)).thenReturn(xml);

        ResponseEntity<String> response = ddiResources.getCodeListXml(CL_AGENCY, CL_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getCodeListXml(CL_AGENCY, CL_ID, null);
    }

    @Test
    void getCodeListXml_returns404WhenNull() {
        when(ddiService.getCodeListXml(CL_AGENCY, "unknown", null)).thenReturn(null);

        ResponseEntity<String> response = ddiResources.getCodeListXml(CL_AGENCY, "unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getCodeListJson_returns200WithDdi4() {
        Ddi4Response ddi4 = emptyDdi4();
        when(ddiService.getCodeList(CL_AGENCY, CL_ID, null)).thenReturn(ddi4);

        ResponseEntity<Ddi4Response> response = ddiResources.getCodeListJson(CL_AGENCY, CL_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(ddi4, response.getBody());
        verify(ddiService).getCodeList(CL_AGENCY, CL_ID, null);
    }

    @Test
    void getCodeListJson_returns404WhenNull() {
        when(ddiService.getCodeList(CL_AGENCY, "unknown", null)).thenReturn(null);

        ResponseEntity<Ddi4Response> response = ddiResources.getCodeListJson(CL_AGENCY, "unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getCodeListXmlByVersion_returns200WithXml() {
        String xml = "<ddi:FragmentInstance/>";
        when(ddiService.getCodeListXml(CL_AGENCY, CL_ID, CL_VERSION)).thenReturn(xml);

        ResponseEntity<String> response = ddiResources.getCodeListXmlByVersion(CL_AGENCY, CL_ID, CL_VERSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getCodeListXml(CL_AGENCY, CL_ID, CL_VERSION);
    }

    @Test
    void getCodeListJsonByVersion_returns200WithDdi4() {
        Ddi4Response ddi4 = emptyDdi4();
        when(ddiService.getCodeList(CL_AGENCY, CL_ID, CL_VERSION)).thenReturn(ddi4);

        ResponseEntity<Ddi4Response> response = ddiResources.getCodeListJsonByVersion(CL_AGENCY, CL_ID, CL_VERSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ddi4, response.getBody());
        verify(ddiService).getCodeList(CL_AGENCY, CL_ID, CL_VERSION);
    }

    // --- GET /ddi/operation/{id}/studyUnit (JSON, public) ---

    @Test
    void getOperationStudyUnitJson_returns200WithJson_whenStudyUnitExists() throws RmesException {
        String id = "op1";
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String xml = "<Fragment><StudyUnit/></Fragment>";
        ObjectNode expectedJson = new ObjectMapper().createObjectNode().put("ID", id);
        when(bauhausUriBuilder.getCompleteUriPublication("operation", id)).thenReturn(operationIri);
        when(ddiService.getStudyUnitXmlByOperationIri(operationIri)).thenReturn(Optional.of(xml));
        when(ddiItemConvertService.convert(xml)).thenReturn(expectedJson);

        ResponseEntity<String> response = ddiResources.getOperationStudyUnitJson(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals(expectedJson.toString(), response.getBody());
        verify(ddiItemConvertService).convert(xml);
    }

    @Test
    void getOperationStudyUnitJson_returns404_whenStudyUnitNotFound() throws RmesException {
        String id = "unknown";
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        when(bauhausUriBuilder.getCompleteUriPublication("operation", id)).thenReturn(operationIri);
        when(ddiService.getStudyUnitXmlByOperationIri(operationIri)).thenReturn(Optional.empty());

        ResponseEntity<String> response = ddiResources.getOperationStudyUnitJson(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // --- GET /ddi/operation/{id}/studyUnit (XML DDI 3.3, public) ---

    @Test
    void getOperationStudyUnitXml_returns200WithXml_whenStudyUnitExists() throws RmesException {
        String id = "op1";
        String operationIri = "http://id.insee.fr/operations/operation/op1";
        String xml = "<Fragment><StudyUnit/></Fragment>";
        when(bauhausUriBuilder.getCompleteUriPublication("operation", id)).thenReturn(operationIri);
        when(ddiService.getStudyUnitXmlByOperationIri(operationIri)).thenReturn(Optional.of(xml));

        ResponseEntity<String> response = ddiResources.getOperationStudyUnitXml(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiItemConvertService, never()).convert(any());
    }

    @Test
    void getOperationStudyUnitXml_returns404_whenStudyUnitNotFound() throws RmesException {
        String id = "unknown";
        String operationIri = "http://id.insee.fr/operations/operation/unknown";
        when(bauhausUriBuilder.getCompleteUriPublication("operation", id)).thenReturn(operationIri);
        when(ddiService.getStudyUnitXmlByOperationIri(operationIri)).thenReturn(Optional.empty());

        ResponseEntity<String> response = ddiResources.getOperationStudyUnitXml(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

}