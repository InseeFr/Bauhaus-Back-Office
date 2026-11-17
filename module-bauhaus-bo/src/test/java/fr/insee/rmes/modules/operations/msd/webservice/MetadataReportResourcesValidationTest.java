package fr.insee.rmes.modules.operations.msd.webservice;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationExportService;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import org.hamcrest.Matchers;
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

/**
 * Contrat du corps des requêtes de création et de modification d'un rapport SIMS : un libellé
 * absent, ou une rubrique sans identifiant ni type, est refusé en 400 nommant le champ, avant que
 * le service n'écrive quoi que ce soit. Le front s'appuie sur la forme {@code rubrics[i].<champ>}
 * pour nommer la rubrique fautive.
 */
@WebMvcTest(
        value = MetadataReportResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = {"fr.insee.rmes.bauhaus.modules.operations.enabled=true"})
@AutoConfigureMockMvc(addFilters = false)
class MetadataReportResourcesValidationTest {

    private static final String VALID_BODY = """
            {"id": "2079",
             "idSeries": "s2098",
             "labelLg1": "Rapport qualité : Enquête mensuelle",
             "labelLg2": "Quality report: Monthly survey",
             "rubrics": [
               {"idAttribute": "S.1.1", "rangeType": "ORGANIZATION", "value": "DG75-L201"},
               {"idAttribute": "S.3.1", "rangeType": "RICH_TEXT", "labelLg1": "Concepts"}
             ]}""";

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
    void postMetadataReport_whenTheLabelIsBlank_shouldReturnBadRequestNamingIt() throws Exception {
        mockMvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"Rapport qualité : Enquête mensuelle\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("labelLg1")));

        verifyNoInteractions(documentationsService);
    }

    @Test
    void putMetadataReport_whenARubricHasNoRangeType_shouldReturnBadRequestNamingItsIndex() throws Exception {
        mockMvc.perform(put("/operations/metadataReport/{id}", "2079")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace(", \"rangeType\": \"RICH_TEXT\"", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("rubrics[1].rangeType")));

        verifyNoInteractions(documentationsService);
    }

    @Test
    void putMetadataReport_whenARubricHasNoIdAttribute_shouldReturnBadRequestNamingItsIndex() throws Exception {
        mockMvc.perform(put("/operations/metadataReport/{id}", "2079")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"idAttribute\": \"S.1.1\", ", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("rubrics[0].idAttribute")));

        verifyNoInteractions(documentationsService);
    }

    @Test
    void postMetadataReport_whenTheBodyIsNotJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/operations/metadataReport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("bodyExample"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));

        verifyNoInteractions(documentationsService);
    }
}
