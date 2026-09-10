package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PhysicalInstanceVariablesResourcesTest {

    @Mock
    private DDIService ddiService;

    @InjectMocks
    private PhysicalInstanceVariablesResources resources;

    private static final String AGENCY = "fr.insee";
    private static final String ID = "9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd";
    private static final String VERSION = "3";

    private static Ddi4Response emptyDdi4() {
        return new Ddi4Response("ddi:4.0", null, null, null, null, null, null, null);
    }

    @Test
    void getVariablesXml_returns200WithXml() {
        String xml = "<ddi:FragmentInstance><Fragment><DataRelationship/></Fragment></ddi:FragmentInstance>";
        when(ddiService.getDataRelationshipsXml(AGENCY, ID, null)).thenReturn(xml);

        ResponseEntity<String> response = resources.getVariablesXml(AGENCY, ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertEquals(xml, response.getBody());
        verify(ddiService).getDataRelationshipsXml(AGENCY, ID, null);
    }

    @Test
    void getVariablesXml_returns404WhenNull() {
        when(ddiService.getDataRelationshipsXml(AGENCY, "unknown", null)).thenReturn(null);

        ResponseEntity<String> response = resources.getVariablesXml(AGENCY, "unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getVariablesJson_returns200WithDdi4() {
        Ddi4Response ddi4 = emptyDdi4();
        when(ddiService.getDataRelationships(AGENCY, ID, null)).thenReturn(ddi4);

        ResponseEntity<Ddi4Response> response = resources.getVariablesJson(AGENCY, ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertSame(ddi4, response.getBody());
        verify(ddiService).getDataRelationships(AGENCY, ID, null);
    }

    @Test
    void getVariablesJson_returns404WhenNull() {
        when(ddiService.getDataRelationships(AGENCY, "unknown", null)).thenReturn(null);

        ResponseEntity<Ddi4Response> response = resources.getVariablesJson(AGENCY, "unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getVariablesXmlByVersion_returns200WithXml() {
        String xml = "<ddi:FragmentInstance/>";
        when(ddiService.getDataRelationshipsXml(AGENCY, ID, VERSION)).thenReturn(xml);

        ResponseEntity<String> response = resources.getVariablesXmlByVersion(AGENCY, ID, VERSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(xml, response.getBody());
        verify(ddiService).getDataRelationshipsXml(AGENCY, ID, VERSION);
    }

    @Test
    void getVariablesJsonByVersion_returns200WithDdi4() {
        Ddi4Response ddi4 = emptyDdi4();
        when(ddiService.getDataRelationships(AGENCY, ID, VERSION)).thenReturn(ddi4);

        ResponseEntity<Ddi4Response> response = resources.getVariablesJsonByVersion(AGENCY, ID, VERSION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(ddi4, response.getBody());
        verify(ddiService).getDataRelationships(AGENCY, ID, VERSION);
    }

    // #1144 : le endpoint est exposé sous /ddi/public/fichier/{agency}/{id} (et plus sous
    // /ddi/public/structures/{agency}/{id}/variables). Le préfixe /ddi/ reste imposé par la
    // redirection Gravitee.

    @Test
    void endpointMappedUnderDdiFichier() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(resources).build();
        when(ddiService.getDataRelationships(AGENCY, ID, null)).thenReturn(emptyDdi4());

        mockMvc.perform(get("/ddi/public/fichier/{agency}/{id}", AGENCY, ID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void endpointMappedUnderDdiFichier_withVersion() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(resources).build();
        when(ddiService.getDataRelationships(AGENCY, ID, VERSION)).thenReturn(emptyDdi4());

        mockMvc.perform(get("/ddi/public/fichier/{agency}/{id}/{version}", AGENCY, ID, VERSION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void endpointMappedUnderDdiFichier_xml() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(resources).build();
        when(ddiService.getDataRelationshipsXml(AGENCY, ID, null)).thenReturn("<ddi:FragmentInstance/>");

        mockMvc.perform(get("/ddi/public/fichier/{agency}/{id}", AGENCY, ID).accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test
    void oldStructuresVariablesPathNoLongerMapped() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(resources).build();
        // Réponse non nulle : si l'ancien path était encore mappé on aurait 200, pas 404.
        lenient().when(ddiService.getDataRelationships(AGENCY, ID, null)).thenReturn(emptyDdi4());

        mockMvc.perform(get("/ddi/public/structures/{agency}/{id}/variables", AGENCY, ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
