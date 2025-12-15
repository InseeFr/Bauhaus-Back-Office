package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = DocumentsResources.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class DocumentsResourcesIntegrationTest {

    @MockitoBean
    private DocumentsService documentsService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldReturnNotFoundException() throws Exception {
    	when(documentsService.downloadDocument(anyString())).thenThrow(new RmesNotFoundException(HttpStatus.NOT_FOUND.value(), "id not found", "id not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/documents/document/id/file"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("id not found")));
    }
}
