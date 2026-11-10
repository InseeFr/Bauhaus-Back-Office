package fr.insee.rmes.modules.structures.components.webservice;

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

import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.bauhaus_services.structures.StructureService;
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
 * Contrat du corps de requête d'un composant de structure : un corps illisible, ou sans notation,
 * libellés ou type, est refusé en 400 nommant le champ, avant que le service n'écrive quoi que ce
 * soit. Avant, le service refusait ces corps par un 400 sans champ, et un corps sans
 * contributeurs échouait en cours d'écriture.
 */
@WebMvcTest(
        value = ComponentResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.structures.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class ComponentResourcesValidationTest {

    private static final String COMPLETE_BODY = """
            {
              "id": "a1000",
              "identifiant": "NOTATION",
              "labelLg1": "Attribut",
              "labelLg2": "Attribute",
              "altLabelLg1": "Attr",
              "altLabelLg2": "Attr EN",
              "descriptionLg1": "Description",
              "descriptionLg2": "Description EN",
              "type": "http://purl.org/linked-data/cube#AttributeProperty",
              "concept": "c1000",
              "codeList": "http://bauhaus/codes/liste",
              "fullCodeListValue": "http://bauhaus/codes/liste",
              "range": "http://www.w3.org/2001/XMLSchema#string",
              "created": "2026-09-23T10:00:00",
              "creator": "DG75-L201",
              "contributor": ["DG75-L201"],
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
              "minLength": "1",
              "maxLength": "10",
              "minInclusive": "0",
              "maxInclusive": "100",
              "pattern": "[A-Z]+",
              "attribute_0": "http://bauhaus/attribut/lien",
              "attributeValue_0": "http://bauhaus/valeur/cible"
            }""";

    private static final String MINIMAL_BODY = """
            {"identifiant": "NOTATION",
             "labelLg1": "Attribut",
             "labelLg2": "Attribute",
             "type": "http://purl.org/linked-data/cube#AttributeProperty",
             "contributor": []}""";

    @MockitoBean
    private StructureService structureService;

    @MockitoBean
    private StructureComponent structureComponentService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void postComponent_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/structures/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("identifiant", "labelLg1", "labelLg2", "type")));

        verify(structureComponentService, never()).createComponent(any());
    }

    @Test
    void putComponent_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/structures/components/{id}", "a1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("identifiant", "labelLg1", "labelLg2", "type")));

        verify(structureComponentService, never()).updateComponent(any(), any());
    }

    @Test
    void putComponent_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/structures/components/{id}", "a1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(structureComponentService, never()).updateComponent(any(), any());
    }

    @Test
    void putComponent_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/structures/components/{id}", "a1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isOk());

        // Le dépôt réécrit le composant entier : un champ omis ici, attributs libres compris,
        // serait effacé en base.
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(structureComponentService).updateComponent(eq("a1000"), forwarded.capture());
        JSONAssert.assertEquals(COMPLETE_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postComponent_shouldNotForwardTheFieldsTheServerComputesItself() throws Exception {
        when(structureComponentService.createComponent(anyString())).thenReturn("a1001");

        mockMvc.perform(post("/structures/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifiant": "NOTATION",
                                 "labelLg1": "Attribut",
                                 "labelLg2": "Attribute",
                                 "type": "http://purl.org/linked-data/cube#AttributeProperty",
                                 "contributor": [],
                                 "updated": "2020-01-01T00:00:00",
                                 "validationState": "Validated",
                                 "structures": [{"id": "dsd1000"}]}"""))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(structureComponentService).createComponent(forwarded.capture());
        JSONAssert.assertEquals(MINIMAL_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postComponent_withoutContributor_shouldForwardAnEmptyListOfContributors() throws Exception {
        when(structureComponentService.createComponent(anyString())).thenReturn("a1001");

        mockMvc.perform(post("/structures/components")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifiant": "NOTATION",
                                 "labelLg1": "Attribut",
                                 "labelLg2": "Attribute",
                                 "type": "http://purl.org/linked-data/cube#AttributeProperty"}"""))
                .andExpect(status().isCreated());

        // Le dépôt parcourt les contributeurs sans garde : absents, il échouait sur un NPE.
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(structureComponentService).createComponent(forwarded.capture());
        JSONAssert.assertEquals(MINIMAL_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }
}
