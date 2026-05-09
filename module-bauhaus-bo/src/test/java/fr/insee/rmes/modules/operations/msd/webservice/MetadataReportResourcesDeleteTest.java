package fr.insee.rmes.modules.operations.msd.webservice;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = MetadataReportResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = {
                "fr.insee.rmes.bauhaus.modules[0].identifier=operations"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class MetadataReportResourcesDeleteTest {

    @MockitoBean
    protected OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;

    @MockitoBean
    protected DocumentationService documentationService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void deleteMetadataReport_withAcceptJson_shouldReturnSuccess() throws Exception {
        when(documentationsService.deleteMetadataReport("42")).thenReturn(HttpStatus.NO_CONTENT);

        mockMvc.perform(delete("/operations/metadataReport/delete/{id}", "42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMetadataReport_withoutAccept_shouldReturnSuccess() throws Exception {
        when(documentationsService.deleteMetadataReport("42")).thenReturn(HttpStatus.NO_CONTENT);

        mockMvc.perform(delete("/operations/metadataReport/delete/{id}", "42"))
                .andExpect(status().isNoContent());
    }
}
