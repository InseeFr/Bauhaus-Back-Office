package fr.insee.rmes.modules.datasets.datasets.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.datasets.datasets.model.Dataset;
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
 * Contrat du corps des requêtes de création et de modification d'un jeu de données : un corps sans
 * libellés, sans propriétaire, sans gestionnaire, sans statut de diffusion ou avec un identifiant
 * alternatif mal formé est refusé en 400 nommant le champ, avant que le service n'écrive quoi que ce
 * soit. Avant, le service refusait ces corps avec un 400 sans champ, et acceptait les valeurs
 * blanches.
 */
@WebMvcTest(
        value = DatasetResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.datasets.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class DatasetResourcesValidationTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final String REQUIRED_FIELDS_BODY = """
            {"labelLg1": "Recensement de la population",
             "labelLg2": "Population census",
             "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
             "catalogRecord": {"creator": "DG75-L201", "contributor": ["DG75-L201"]}}""";

    private static final String COMPLETE_BODY = """
            {"id": "jd1001",
             "labelLg1": "Recensement de la population",
             "labelLg2": "Population census",
             "subTitleLg1": "Sous-titre",
             "subTitleLg2": "Subtitle",
             "accrualPeriodicity": "http://id.insee.fr/codes/frequence/A",
             "accessRights": "http://id.insee.fr/codes/accessRights/public",
             "confidentialityStatus": "http://id.insee.fr/codes/confidentialityStatus/public",
             "creators": ["http://id.insee.fr/organisations/insee"],
             "publisher": "http://id.insee.fr/organisations/insee",
             "landingPageLg1": "https://www.insee.fr/fr/rp",
             "landingPageLg2": "https://www.insee.fr/en/rp",
             "linkedDocuments": ["https://www.insee.fr/doc.pdf"],
             "keywords": {"lg1": ["population"], "lg2": ["population"]},
             "updated": "2026-10-09",
             "issued": "2026-01-01",
             "altIdentifier": "rp-2026",
             "processStep": "http://id.insee.fr/codes/processStep/1",
             "archiveUnit": "http://id.insee.fr/archives/1",
             "type": "http://id.insee.fr/codes/type/1",
             "statisticalUnit": ["http://id.insee.fr/codes/statisticalUnit/1"],
             "dataStructure": "http://id.insee.fr/structures/1",
             "temporalCoverageStartDate": "2020",
             "temporalCoverageEndDate": "2025",
             "temporalCoverageDataType": "http://www.w3.org/2001/XMLSchema#gYear",
             "observationNumber": 12,
             "timeSeriesNumber": 3,
             "spacialCoverage": "http://id.insee.fr/geo/pays/FR",
             "spacialTemporal": "2026-01-01",
             "temporalResolution": "http://id.insee.fr/codes/frequence/A",
             "spacialResolutions": ["http://id.insee.fr/geo/commune"],
             "descriptionLg1": "Description",
             "descriptionLg2": "Description EN",
             "abstractLg1": "Résumé",
             "abstractLg2": "Abstract",
             "cautionLg1": "Avertissement",
             "cautionLg2": "Caution",
             "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
             "wasGeneratedIRIs": ["http://bauhaus/operations/serie/s1001"],
             "wasDerivedFrom": {"datasets": ["jd1000"], "descriptionLg1": "Construit", "descriptionLg2": "Built"},
             "themes": ["http://id.insee.fr/themes/1"],
             "validationState": "Modified",
             "catalogRecord": {"creator": "DG75-L201", "contributor": ["DG75-L201"],
                               "created": "2026-01-01T00:00:00", "updated": "2026-10-09T00:00:00"}}""";

    @MockitoBean
    DatasetService datasetService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void postDataset_whenTheBodyIsEmpty_shouldReturnBadRequestNamingEveryRequiredField() throws Exception {
        mockMvc.perform(post("/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder(
                                "labelLg1", "labelLg2", "disseminationStatus", "catalogRecord")));

        verify(datasetService, never()).create(any(Dataset.class));
    }

    @Test
    void postDataset_whenTheCatalogRecordIsEmpty_shouldReturnBadRequestNamingItsFields() throws Exception {
        mockMvc.perform(post("/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace(
                                "{\"creator\": \"DG75-L201\", \"contributor\": [\"DG75-L201\"]}",
                                "{\"creator\": \" \", \"contributor\": []}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("catalogRecord.creator", "catalogRecord.contributor")));

        verify(datasetService, never()).create(any(Dataset.class));
    }

    @Test
    void putDataset_whenALabelIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/datasets/{id}", "jd1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace("\"Population census\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("labelLg2")));

        verify(datasetService, never()).update(anyString(), any(Dataset.class));
    }

    @Test
    void putDataset_whenTheAlternativeIdentifierHasForbiddenCharacters_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/datasets/{id}", "jd1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace("{", "{\"altIdentifier\": \"rp 2026\",")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("altIdentifier")));

        verify(datasetService, never()).update(anyString(), any(Dataset.class));
    }

    @Test
    void putDataset_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/datasets/{id}", "jd1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(datasetService, never()).update(anyString(), any(Dataset.class));
    }

    @Test
    void postDataset_whenTheBodyIsValid_shouldReturnTheCreatedId() throws Exception {
        when(datasetService.create(any(Dataset.class))).thenReturn("jd1002");

        mockMvc.perform(post("/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("jd1002"));
    }

    @Test
    void putDataset_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/datasets/{id}", "jd1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isOk());

        // Le service réécrit le jeu de données à partir du corps : un champ perdu en route serait
        // effacé en base.
        ArgumentCaptor<Dataset> forwarded = ArgumentCaptor.forClass(Dataset.class);
        verify(datasetService).update(eq("jd1001"), forwarded.capture());
        JSONAssert.assertEquals(COMPLETE_BODY, MAPPER.writeValueAsString(forwarded.getValue()), JSONCompareMode.STRICT);
    }
}
