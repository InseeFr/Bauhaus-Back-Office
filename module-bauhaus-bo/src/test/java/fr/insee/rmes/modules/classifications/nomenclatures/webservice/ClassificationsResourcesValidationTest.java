package fr.insee.rmes.modules.classifications.nomenclatures.webservice;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.classifications.ClassificationsService;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
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
 * Contrat du corps des requêtes de modification d'une nomenclature et d'un de ses postes : un corps
 * sans libellés, avec une URL invalide ou un libellé court sans IRI, est refusé en 400 nommant le
 * champ, avant que le service n'écrive quoi que ce soit. Avant, le service ne vérifiait que la
 * présence des libellés (400 sans champ, valeur blanche acceptée), et le libellé anglais d'une
 * nomenclature n'était pas contrôlé du tout.
 */
@WebMvcTest(
        value = ClassificationsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.classifications.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class ClassificationsResourcesValidationTest {

    private static final String CLASSIFICATION_REQUIRED_FIELDS_BODY = """
            {"prefLabelLg1": "Nomenclature d'activités française",
             "prefLabelLg2": "French classification of activities"}""";

    private static final String CLASSIFICATION_COMPLETE_BODY = """
            {"prefLabelLg1": "Nomenclature d'activités française",
             "prefLabelLg2": "French classification of activities",
             "altLabelLg1": "NAF",
             "altLabelLg2": "NAF EN",
             "descriptionLg1": "Description",
             "descriptionLg2": "Description EN",
             "changeNoteLg1": "Note de changement",
             "changeNoteLg2": "Change note",
             "changeNoteUriLg1": "http://bauhaus/codes/nafr2/changeNote/fr",
             "changeNoteUriLg2": "http://bauhaus/codes/nafr2/changeNote/en",
             "scopeNoteLg1": "Note de contenu",
             "scopeNoteLg2": "Scope note",
             "scopeNoteUriLg1": "http://bauhaus/codes/nafr2/scopeNote/fr",
             "scopeNoteUriLg2": "http://bauhaus/codes/nafr2/scopeNote/en",
             "idSeries": "nafr",
             "idBefore": "nafr1",
             "idAfter": "nafr3",
             "idVariant": "nafr2bis",
             "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
             "additionalMaterial": "http://bauhaus/documents/naf.pdf",
             "legalMaterial": "http://bauhaus/documents/decret.pdf",
             "homepage": "https://www.insee.fr/fr/information/2406147",
             "creator": "http://bauhaus/organisations/insee/DG75-L201",
             "contributor": "http://bauhaus/organisations/insee/DG57-L201",
             "validationState": "Validated"}""";

    private static final String ITEM_REQUIRED_FIELDS_BODY = """
            {"prefLabelLg1": "Culture de céréales",
             "prefLabelLg2": "Growing of cereals"}""";

    private static final String ITEM_COMPLETE_BODY = """
            {"prefLabelLg1": "Culture de céréales",
             "prefLabelLg2": "Growing of cereals",
             "altLabelLg1": "Céréales",
             "altLabelLg2": "Cereals",
             "broaderURI": "http://bauhaus/codes/nafr2/division/01",
             "altLabels": [{"length": "30",
                            "shortLabelLg1": "Céréales",
                            "shortLabelLg2": "Cereals",
                            "shortLabelUri": "http://bauhaus/codes/nafr2/sousClasse/01.11Z/libelleCourt30"}],
             "definitionLg1": "Définition", "definitionLg1Uri": "http://bauhaus/notes/definition/fr",
             "definitionLg2": "Definition", "definitionLg2Uri": "http://bauhaus/notes/definition/en",
             "scopeNoteLg1": "Note", "scopeNoteLg1Uri": "http://bauhaus/notes/scope/fr",
             "scopeNoteLg2": "Note", "scopeNoteLg2Uri": "http://bauhaus/notes/scope/en",
             "coreContentNoteLg1": "Comprend", "coreContentNoteLg1Uri": "http://bauhaus/notes/core/fr",
             "coreContentNoteLg2": "Includes", "coreContentNoteLg2Uri": "http://bauhaus/notes/core/en",
             "additionalContentNoteLg1": "Comprend aussi",
             "additionalContentNoteLg1Uri": "http://bauhaus/notes/additional/fr",
             "additionalContentNoteLg2": "Also includes",
             "additionalContentNoteLg2Uri": "http://bauhaus/notes/additional/en",
             "exclusionNoteLg1": "Ne comprend pas", "exclusionNoteLg1Uri": "http://bauhaus/notes/exclusion/fr",
             "exclusionNoteLg2": "Excludes", "exclusionNoteLg2Uri": "http://bauhaus/notes/exclusion/en",
             "changeNoteLg1": "Changement", "changeNoteLg1Uri": "http://bauhaus/notes/change/fr",
             "changeNoteLg2": "Change", "changeNoteLg2Uri": "http://bauhaus/notes/change/en"}""";

    @MockitoBean
    ClassificationsService classificationsService;

    @MockitoBean
    ClassificationItemService classificationItemService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void putClassification_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("prefLabelLg1", "prefLabelLg2")));

        verify(classificationsService, never()).updateClassification(anyString(), anyString());
    }

    @Test
    void putClassification_whenALabelIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLASSIFICATION_REQUIRED_FIELDS_BODY.replace(
                                "\"French classification of activities\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("prefLabelLg2")));

        verify(classificationsService, never()).updateClassification(anyString(), anyString());
    }

    @Test
    void putClassification_whenALinkIsNotAnUrl_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prefLabelLg1": "Nomenclature d'activités française",
                                 "prefLabelLg2": "French classification of activities",
                                 "additionalMaterial": "naf.pdf",
                                 "legalMaterial": "décret",
                                 "homepage": "insee.fr"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("additionalMaterial", "legalMaterial", "homepage")));

        verify(classificationsService, never()).updateClassification(anyString(), anyString());
    }

    @Test
    void putClassification_whenALinkIsEmpty_shouldForwardIt() throws Exception {
        // Un lien vidé à l'écran part en chaîne vide : le service ne l'écrit pas.
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLASSIFICATION_REQUIRED_FIELDS_BODY.replace("{", "{\"homepage\": \"\",")))
                .andExpect(status().isOk());

        verify(classificationsService).updateClassification(eq("nafr2"), anyString());
    }

    @Test
    void putClassification_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(classificationsService, never()).updateClassification(anyString(), anyString());
    }

    @Test
    void putClassification_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLASSIFICATION_COMPLETE_BODY))
                .andExpect(status().isOk());

        // Le service réécrit la nomenclature à partir du corps : un champ omis ici serait effacé en
        // base. validationState en fait partie : il décide entre « Modifiée » et « Provisoire ».
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(classificationsService).updateClassification(eq("nafr2"), forwarded.capture());
        JSONAssert.assertEquals(CLASSIFICATION_COMPLETE_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void putClassification_shouldForwardOnlyTheFieldsTheServiceWrites() throws Exception {
        // Le front renvoie la nomenclature telle qu'il l'a lue (identifiant, dates, libellés des
        // nomenclatures liées...) : rien de cela n'est écrit. L'identifiant vient du chemin.
        mockMvc.perform(put("/classifications/classification/{id}", "nafr2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLASSIFICATION_REQUIRED_FIELDS_BODY.replace("{", """
                                {"id": "autre", "created": "2026-10-09T10:00:00.000Z",
                                 "seriesLg1": "NAF", "idBeforeLg1": "NAF rév. 1",""")))
                .andExpect(status().isOk());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(classificationsService).updateClassification(eq("nafr2"), forwarded.capture());
        JSONAssert.assertEquals(CLASSIFICATION_REQUIRED_FIELDS_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void putClassificationItem_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}/item/{itemId}", "nafr2", "01.11Z")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder("prefLabelLg1", "prefLabelLg2")));

        verify(classificationItemService, never()).updateClassificationItem(anyString(), anyString(), anyString());
    }

    @Test
    void putClassificationItem_whenALabelIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}/item/{itemId}", "nafr2", "01.11Z")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ITEM_REQUIRED_FIELDS_BODY.replace("\"Culture de céréales\"", "\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("prefLabelLg1")));

        verify(classificationItemService, never()).updateClassificationItem(anyString(), anyString(), anyString());
    }

    @Test
    void putClassificationItem_whenAShortLabelHasNoIri_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}/item/{itemId}", "nafr2", "01.11Z")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ITEM_REQUIRED_FIELDS_BODY.replace(
                                "{", "{\"altLabels\": [{\"length\": \"30\", \"shortLabelLg1\": \"Céréales\"}],")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("altLabels[0].shortLabelUri")));

        verify(classificationItemService, never()).updateClassificationItem(anyString(), anyString(), anyString());
    }

    @Test
    void putClassificationItem_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/classifications/classification/{id}/item/{itemId}", "nafr2", "01.11Z")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ITEM_COMPLETE_BODY))
                .andExpect(status().isOk());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(classificationItemService).updateClassificationItem(eq("nafr2"), eq("01.11Z"), forwarded.capture());
        JSONAssert.assertEquals(ITEM_COMPLETE_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void putClassificationItem_shouldForwardOnlyTheFieldsTheServiceWrites() throws Exception {
        // Le front renvoie le poste tel qu'il l'a lu : identifiant, poste parent, postes enfants et
        // statut de publication (recalculé par le service à partir de l'état en base) ne sont pas
        // écrits. L'identifiant vient du chemin.
        mockMvc.perform(put("/classifications/classification/{id}/item/{itemId}", "nafr2", "01.11Z")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ITEM_REQUIRED_FIELDS_BODY.replace("{", """
                                {"id": "autre", "idBroader": "01", "classificationId": "nafr2",
                                 "narrowers": [{"id": "01.11Za"}], "validationState": "Validated",""")))
                .andExpect(status().isOk());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(classificationItemService).updateClassificationItem(eq("nafr2"), eq("01.11Z"), forwarded.capture());
        JSONAssert.assertEquals(ITEM_REQUIRED_FIELDS_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }
}
