package fr.insee.rmes.modules.operations.msd.webservice;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.CodedRmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.RepositoryUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = MetadataReportResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = {"fr.insee.rmes.bauhaus.modules.operations.enabled=true"})
@AutoConfigureMockMvc(addFilters = false)
class MetadataReportResourcesDeleteTest {

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
    void deleteMetadataReport_withAcceptJson_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/operations/metadataReport/{id}", "42").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMetadataReport_whenSimsDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new RmesNotFoundException("Documentation not found", "unknown"))
                .when(documentationsService)
                .deleteMetadataReport("unknown");

        mockMvc.perform(delete("/operations/metadataReport/{id}", "unknown")).andExpect(status().isNotFound());
    }

    @Test
    void deleteMetadataReport_whenRepositoryIsUnavailable_shouldAnswer503WithMessageAndCode() throws Exception {
        doThrow(new CodedRmesException(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        RepositoryUtils.RDF_REPOSITORY_UNAVAILABLE,
                        "The RDF repository is unavailable. Please try again later.",
                        null))
                .when(documentationsService)
                .deleteMetadataReport("42");

        mockMvc.perform(delete("/operations/metadataReport/{id}", "42"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("The RDF repository is unavailable. Please try again later."))
                .andExpect(jsonPath("$.code").value("RDF_REPOSITORY_UNAVAILABLE"));
    }

    @Test
    void deleteMetadataReport_withoutAccept_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/operations/metadataReport/{id}", "42")).andExpect(status().isOk());
    }
}
