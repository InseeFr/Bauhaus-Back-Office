package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = DocumentsResources.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
    properties = "fr.insee.rmes.bauhaus.extensions=pdf,odt"
)
@AutoConfigureMockMvc(addFilters = false)
class DocumentsResourcesIntegrationTest {

    @MockitoBean
    private DocumentsService documentsService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldServeTheDocumentAsJson() throws Exception {
        when(documentsService.getDocument("1000")).thenReturn(new JSONObject().put("labelLg1", "Note"));

        mockMvc.perform(MockMvcRequestBuilders.get("/documents/document/1000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"labelLg1\":\"Note\"}"));
    }

    @Test
    void shouldServeTheLinkAsJson() throws Exception {
        when(documentsService.getLink("1000")).thenReturn(new JSONObject().put("url", "https://www.insee.fr"));

        mockMvc.perform(MockMvcRequestBuilders.get("/documents/link/1000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"url\":\"https://www.insee.fr\"}"));
    }

    @Test
    void shouldServeTheDocumentsListAsJson() throws Exception {
        when(documentsService.getDocuments()).thenReturn("[{\"id\":\"1000\"}]");

        mockMvc.perform(MockMvcRequestBuilders.get("/documents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{\"id\":\"1000\"}]"));
    }

    @Test
    void shouldRejectAFileWhoseExtensionIsNotAllowed() throws Exception {
        // fr.insee.rmes.bauhaus.extensions liste les extensions déposables : tout le reste
        // est refusé avant même d'atteindre le service.
        MockMultipartFile file = new MockMultipartFile("file", "charge.exe", MediaType.APPLICATION_OCTET_STREAM_VALUE, "x".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/documents/document/1000/file").file(file))
                .andExpect(content().string(containsString("Invalid File Extension")));

        verifyNoInteractions(documentsService);
    }

    @Test
    void shouldRejectAFileWithoutAnyExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sansextension", MediaType.APPLICATION_OCTET_STREAM_VALUE, "x".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/documents/document/1000/file").file(file))
                .andExpect(content().string(containsString("Invalid File Extension")));

        verifyNoInteractions(documentsService);
    }

    @Test
    void shouldReturnNotFoundException() throws Exception {
    	when(documentsService.downloadDocument(anyString())).thenThrow(new RmesNotFoundException(HttpStatus.NOT_FOUND.value(), "id not found", "id not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/documents/document/id/file"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("id not found")));
    }
}
