package fr.insee.rmes.modules.datasets.distributions.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.bauhaus_services.distribution.DistributionService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.datasets.distributions.model.Distribution;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

/**
 * Contrat du corps des requêtes de création et de modification d'une distribution : un corps sans jeu
 * de données, sans libellés, ou avec une adresse d'accès ou de téléchargement qui n'est pas une URL est
 * refusé en 400 nommant le champ, avant que le service n'écrive quoi que ce soit. Avant, le service
 * refusait les champs absents avec un 400 sans champ, et acceptait les valeurs blanches et les
 * adresses mal formées.
 */
@WebMvcTest(
        value = DistributionResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.datasets.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class DistributionResourcesValidationTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final String REQUIRED_FIELDS_BODY = """
            {"idDataset": "jd1001",
             "labelLg1": "Fichier détail",
             "labelLg2": "Detailed file"}""";

    private static final String COMPLETE_BODY = """
            {"id": "d1001",
             "idDataset": "jd1001",
             "labelLg1": "Fichier détail",
             "labelLg2": "Detailed file",
             "descriptionLg1": "Description",
             "descriptionLg2": "Description EN",
             "language": "fr",
             "created": "2026-01-01T00:00:00",
             "updated": "2026-10-13T00:00:00",
             "format": "CSV",
             "mediaType": "text/csv",
             "compressFormat": "zip",
             "byteSize": "1024",
             "accessUrl": "https://www.insee.fr/fr/statistiques/1",
             "url": "https://www.insee.fr/fr/statistiques/fichier/1.zip",
             "validationState": "Modified"}""";

    @MockitoBean
    DistributionService distributionService;

    @MockitoBean
    DatasetService datasetService;

    @MockitoBean
    UserDecoder userDecoder;

    @Autowired
    MockMvc mockMvc;

    @Test
    void postDistribution_whenTheBodyIsEmpty_shouldReturnBadRequestNamingEveryRequiredField() throws Exception {
        mockMvc.perform(post("/distribution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("idDataset", "labelLg1", "labelLg2")));

        verify(distributionService, never()).create(any(Distribution.class));
    }

    @Test
    void putDistribution_whenALabelIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/distribution/{id}", "d1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace("\"Detailed file\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("labelLg2")));

        verify(distributionService, never()).update(anyString(), any(Distribution.class));
    }

    @Test
    void putDistribution_whenTheUrlsAreMalformed_shouldReturnBadRequestNamingThem() throws Exception {
        mockMvc.perform(put("/distribution/{id}", "d1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace(
                                "{", "{\"accessUrl\": \"pas une url\", \"url\": \"insee.fr/fichier\",")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.containsInAnyOrder("accessUrl", "url")));

        verify(distributionService, never()).update(anyString(), any(Distribution.class));
    }

    @Test
    void putDistribution_whenTheUrlsAreEmpty_shouldBeAccepted() throws Exception {
        // Le formulaire envoie une chaîne vide quand l'utilisateur efface une adresse.
        mockMvc.perform(put("/distribution/{id}", "d1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace("{", "{\"accessUrl\": \"\", \"url\": \"\",")))
                .andExpect(status().isOk());
    }

    @Test
    void putDistribution_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/distribution/{id}", "d1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(distributionService, never()).update(anyString(), any(Distribution.class));
    }

    @Test
    void postDistribution_whenTheBodyIsValid_shouldReturnTheCreatedId() throws Exception {
        when(distributionService.create(any(Distribution.class))).thenReturn("d1002");

        mockMvc.perform(post("/distribution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().string("d1002"));
    }

    @Test
    void putDistribution_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/distribution/{id}", "d1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isOk());

        // Le service réécrit la distribution à partir du corps : un champ perdu en route serait
        // effacé en base (dont la date de création, que le service ne relit pas).
        ArgumentCaptor<Distribution> forwarded = ArgumentCaptor.forClass(Distribution.class);
        verify(distributionService).update(eq("d1001"), forwarded.capture());
        JSONAssert.assertEquals(COMPLETE_BODY, MAPPER.writeValueAsString(forwarded.getValue()), JSONCompareMode.STRICT);
    }

    @Test
    void putDistribution_shouldEchoTheIdAsJsonRatherThanRawText() throws Exception {
        String scriptId = "<img src=x onerror=alert(1)>";
        when(distributionService.update(eq(scriptId), any(Distribution.class))).thenReturn(scriptId);

        mockMvc.perform(put("/distribution/{id}", scriptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.identifier").value(scriptId));
    }
}
