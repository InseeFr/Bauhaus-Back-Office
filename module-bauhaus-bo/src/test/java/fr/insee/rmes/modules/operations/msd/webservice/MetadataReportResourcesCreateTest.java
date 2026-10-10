package fr.insee.rmes.modules.operations.msd.webservice;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationExportService;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = MetadataReportResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = {"fr.insee.rmes.bauhaus.modules.operations.enabled=true"})
@AutoConfigureMockMvc(addFilters = false)
class MetadataReportResourcesCreateTest {

    @MockitoBean
    protected OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;

    @MockitoBean
    protected DocumentationService documentationService;

    @MockitoBean
    protected DocumentationExportService documentationExportService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void createMetadataReport_shouldReturnTheNewId() throws Exception {
        when(documentationsService.createMetadataReport(anyString())).thenReturn("1234");

        mockMvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void createMetadataReport_whenNoIdIsReturned_shouldAnswer500WithMessageAndCode() throws Exception {
        when(documentationsService.createMetadataReport(anyString())).thenReturn(null);

        mockMvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("The report could not be created."))
                .andExpect(jsonPath("$.code").value("SIMS_CREATION_FAILED"));
    }
}
