package fr.insee.rmes.modules.concepts.concept.webservice;

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

import fr.insee.rmes.bauhaus_services.ConceptsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
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

/**
 * Contrat du corps de requête d'un concept : un corps illisible, sans libellé principal ou sans
 * statut de diffusion est refusé en 400, avant que le service n'écrive quoi que ce soit. Sans
 * cette validation, le dépôt échouait en cours d'écriture (NPE sur le libellé, IRI vide pour le
 * statut de diffusion) et répondait par une erreur serveur.
 */
@WebMvcTest(
        value = ConceptsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.concepts.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class ConceptsResourcesValidationTest {

    private static final String COMPLETE_BODY = """
            {
              "prefLabelLg1": "Concept",
              "prefLabelLg2": "Concept EN",
              "altLabelLg1": ["Alt FR"],
              "altLabelLg2": ["Alt EN"],
              "creator": "DG75-L201",
              "contributor": "DG75-L201",
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
              "additionalMaterial": "http://example.org",
              "valid": "2027-01-01T00:00:00.000Z",
              "created": "2026-09-23T10:00:00",
              "versioning": true,
              "collections": ["col1"],
              "versionableNotes": [{"noteType": "scopeNoteLg1", "content": "<div>Portée</div>"}],
              "datableNotes": [{"noteType": "changeNoteLg1", "content": "<div>Changement</div>"}],
              "links": [
                {"typeOfLink": "broader", "ids": ["c1"]},
                {"typeOfLink": "closeMatch", "urn": ["urn:x"]}
              ]
            }""";

    @MockitoBean
    private ConceptsService legacyConceptsService;

    @MockitoBean
    private fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService conceptsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void putConcept_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/concepts/concept/{id}", "c1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("prefLabelLg1", "disseminationStatus")));

        verify(legacyConceptsService, never()).setConcept(any(), any());
    }

    @Test
    void postConcept_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/concepts/concept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("prefLabelLg1", "disseminationStatus")));

        verify(legacyConceptsService, never()).setConcept(any());
    }

    @Test
    void putConcept_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/concepts/concept/{id}", "c1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(legacyConceptsService, never()).setConcept(any(), any());
    }

    @Test
    void postConcept_whenALinkHasNoType_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/concepts/concept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prefLabelLg1": "Concept",
                                 "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
                                 "links": [{"ids": ["c1"]}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("links[0].typeOfLink"));

        verify(legacyConceptsService, never()).setConcept(any());
    }

    @Test
    void putConcept_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/concepts/concept/{id}", "c1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isNoContent());

        // Le dépôt réécrit le concept entier : un champ omis ici serait effacé en base.
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(legacyConceptsService).setConcept(eq("c1000"), forwarded.capture());
        JSONAssert.assertEquals(COMPLETE_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postConcept_shouldNotForwardTheFieldsTheServerSetsItself() throws Exception {
        when(legacyConceptsService.setConcept(anyString())).thenReturn("c1001");

        mockMvc.perform(post("/concepts/concept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "c42", "creation": false, "modified": "2020-01-01",
                                 "prefLabelLg1": "Concept",
                                 "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive"}"""))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(legacyConceptsService).setConcept(forwarded.capture());
        JSONAssert.assertEquals("""
                {"prefLabelLg1": "Concept",
                 "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive"}""", forwarded.getValue(), JSONCompareMode.STRICT);
    }
}
