package fr.insee.rmes.modules.structures.structures.webservice;

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
 * Contrat du corps de requête d'une structure : un corps illisible, sans notation ni libellés, ou
 * dont une définition de composant n'a pas de composant typé, est refusé en 400 nommant le champ,
 * avant que le service n'écrive quoi que ce soit. Avant, le service refusait les trois premiers par
 * un 400 sans champ, et échouait en cours d'écriture sur les autres (liste de composants ou de
 * contributeurs absente, composant sans type).
 */
@WebMvcTest(
        value = StructureResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.structures.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class StructureResourcesValidationTest {

    private static final String COMPLETE_BODY = """
            {
              "identifiant": "NOTATION",
              "labelLg1": "Structure",
              "labelLg2": "Structure EN",
              "descriptionLg1": "Description",
              "descriptionLg2": "Description EN",
              "created": "2026-09-23T10:00:00",
              "creator": "DG75-L201",
              "contributor": ["DG75-L201"],
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Prive",
              "componentDefinitions": [
                {
                  "id": "cs1000",
                  "created": "2026-09-23T10:00:00",
                  "modified": "2026-09-24T10:00:00",
                  "order": "1",
                  "attachment": ["http://purl.org/linked-data/cube#Observation"],
                  "required": true,
                  "notation": "CS_NOTATION",
                  "labelLg1": "Spécification",
                  "labelLg2": "Specification",
                  "component": {
                    "id": "a1000",
                    "identifiant": "ATTR",
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
                    "pattern": "[A-Z]+"
                  }
                }
              ]
            }""";

    private static final String MINIMAL_BODY = """
            {"identifiant": "NOTATION",
             "labelLg1": "Structure",
             "labelLg2": "Structure EN",
             "contributor": [],
             "componentDefinitions": []}""";

    @MockitoBean
    private StructureService structureService;

    @MockitoBean
    private StructureComponent structureComponentService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void postStructure_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/structures/structure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("identifiant", "labelLg1", "labelLg2")));

        verify(structureService, never()).setStructure(any());
    }

    @Test
    void putStructure_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/structures/structure/{id}", "dsd1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("identifiant", "labelLg1", "labelLg2")));

        verify(structureService, never()).setStructure(any(), any());
    }

    @Test
    void putStructure_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/structures/structure/{id}", "dsd1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(structureService, never()).setStructure(any(), any());
    }

    @Test
    void putStructure_whenAComponentDefinitionHasNoComponent_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/structures/structure/{id}", "dsd1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifiant": "NOTATION",
                                 "labelLg1": "Structure",
                                 "labelLg2": "Structure EN",
                                 "componentDefinitions": [{"order": "1"}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("componentDefinitions[0].component")));

        verify(structureService, never()).setStructure(any(), any());
    }

    @Test
    void postStructure_whenAComponentHasNoType_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/structures/structure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifiant": "NOTATION",
                                 "labelLg1": "Structure",
                                 "labelLg2": "Structure EN",
                                 "componentDefinitions": [{"order": "1", "component": {"id": "a1000"}}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.contains("componentDefinitions[0].component.type")));

        verify(structureService, never()).setStructure(any());
    }

    @Test
    void putStructure_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/structures/structure/{id}", "dsd1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isOk());

        // Le dépôt réécrit la structure entière, spécifications de composants comprises : un champ
        // omis ici serait effacé en base.
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(structureService).setStructure(eq("dsd1000"), forwarded.capture());
        JSONAssert.assertEquals(COMPLETE_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void putStructure_shouldNotForwardTheIdOfTheBody() throws Exception {
        mockMvc.perform(put("/structures/structure/{id}", "dsd1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "dsd2000",
                                 "identifiant": "NOTATION",
                                 "labelLg1": "Structure",
                                 "labelLg2": "Structure EN",
                                 "contributor": [],
                                 "componentDefinitions": [],
                                 "updated": "2020-01-01T00:00:00",
                                 "modified": "2020-01-01T00:00:00",
                                 "validationState": "Validated"}"""))
                .andExpect(status().isOk());

        // L'identifiant du corps l'emportait sur celui du chemin : la structure était réécrite
        // sous un autre identifiant que celui dont on venait de lire le statut.
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(structureService).setStructure(eq("dsd1000"), forwarded.capture());
        JSONAssert.assertEquals(MINIMAL_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postStructure_withoutContributorNorComponents_shouldForwardEmptyLists() throws Exception {
        when(structureService.setStructure(anyString())).thenReturn("dsd1001");

        mockMvc.perform(post("/structures/structure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifiant": "NOTATION",
                                 "labelLg1": "Structure",
                                 "labelLg2": "Structure EN"}"""))
                .andExpect(status().isOk());

        // Le dépôt parcourt les contributeurs et les composants sans garde : absents, il échouait
        // sur un NPE.
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(structureService).setStructure(forwarded.capture());
        JSONAssert.assertEquals(MINIMAL_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postStructure_whenTheOrderIsANumber_shouldAcceptIt() throws Exception {
        when(structureService.setStructure(anyString())).thenReturn("dsd1001");

        // Le front envoie l'ordre tantôt en texte, tantôt en nombre.
        mockMvc.perform(post("/structures/structure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifiant": "NOTATION",
                                 "labelLg1": "Structure",
                                 "labelLg2": "Structure EN",
                                 "componentDefinitions": [{"order": 1, "component": {
                                   "id": "m1000", "type": "http://purl.org/linked-data/cube#MeasureProperty"}}]}"""))
                .andExpect(status().isOk());

        verify(structureService).setStructure(anyString());
    }
}
