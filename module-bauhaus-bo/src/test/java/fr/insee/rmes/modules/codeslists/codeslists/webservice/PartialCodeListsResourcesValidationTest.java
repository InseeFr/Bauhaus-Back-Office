package fr.insee.rmes.modules.codeslists.codeslists.webservice;

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

import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;

/**
 * Contrat du corps de requête d'une liste de codes partielle : un corps sans identifiant, libellés,
 * propriétaire, statut de diffusion ou liste parente, ou dont un code n'a pas d'IRI, est refusé en
 * 400 nommant le champ, avant que le service n'écrive quoi que ce soit. Avant, le service ne
 * vérifiait que la présence de l'identifiant et des libellés (400 sans champ, valeur blanche
 * acceptée), et échouait en cours d'écriture sur un code sans IRI.
 */
class PartialCodeListsResourcesValidationTest extends AbstractCodesListsResourcesWebMvcTest {

    private static final String REQUIRED_FIELDS_BODY = """
            {"id": "CL_PARTIAL",
             "labelLg1": "Liste partielle",
             "labelLg2": "Partial list",
             "creator": "DG75-L201",
             "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
             "iriParent": "http://bauhaus/codes/liste",
             "codes": {"A": {"iri": "http://bauhaus/codes/liste/A"}}}""";

    private static final String COMPLETE_BODY = """
            {"id": "CL_PARTIAL",
             "labelLg1": "Liste partielle",
             "labelLg2": "Partial list",
             "descriptionLg1": "Description",
             "descriptionLg2": "Description EN",
             "creator": "DG75-L201",
             "contributor": ["DG75-L201", "DG57-L201"],
             "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
             "validationState": "Validated",
             "iriParent": "http://bauhaus/codes/liste",
             "codes": {
               "A": {"iri": "http://bauhaus/codes/liste/A"},
               "B": {"iri": "http://bauhaus/codes/liste/B"}
             }}""";

    @Test
    void postPartialCodeList_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field")
                        .value(Matchers.containsInAnyOrder(
                                "id", "labelLg1", "labelLg2", "creator", "disseminationStatus", "iriParent")));

        verify(codeListService, never()).setCodesList(anyString(), any(CodeListKind.class));
    }

    @Test
    void putPartialCodeList_whenALabelIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/codeList/partial/{id}", "CL_PARTIAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace("\"Partial list\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("labelLg2")));

        verify(codeListService, never()).setCodesList(anyString(), anyString(), any(CodeListKind.class));
    }

    @Test
    void postPartialCodeList_whenACodeHasNoIri_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace(
                                "{\"iri\": \"http://bauhaus/codes/liste/A\"}", "{\"code\": \"A\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("codes[A].iri")));

        verify(codeListService, never()).setCodesList(anyString(), any(CodeListKind.class));
    }

    @Test
    void postPartialCodeList_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(codeListService, never()).setCodesList(anyString(), any(CodeListKind.class));
    }

    @Test
    void putPartialCodeList_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/codeList/partial/{id}", "CL_PARTIAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COMPLETE_BODY))
                .andExpect(status().isOk());

        // Le service réécrit la liste entière à partir du corps : un champ omis ici serait effacé en
        // base. validationState en fait partie : il décide entre « Modifiée » et « Provisoire ».
        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(codeListService).setCodesList(eq("CL_PARTIAL"), forwarded.capture(), eq(CodeListKind.PARTIAL));
        JSONAssert.assertEquals(COMPLETE_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postPartialCodeList_shouldForwardOnlyTheFieldsTheServiceWrites() throws Exception {
        when(codeListService.setCodesList(anyString(), any(CodeListKind.class))).thenReturn("CL_PARTIAL");

        // Le front renvoie la liste telle qu'il l'a lue et la complète de ce que l'écran manipule
        // (code parent, libellés et état des codes du picker...) : rien de cela n'est écrit.
        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id": "CL_PARTIAL",
                                 "labelLg1": "Liste partielle",
                                 "labelLg2": "Partial list",
                                 "creator": "DG75-L201",
                                 "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
                                 "iriParent": "http://bauhaus/codes/liste",
                                 "parentCode": "CL_PARENT",
                                 "created": "2026-10-08T10:00:00.000Z",
                                 "codes": {"A": {"iri": "http://bauhaus/codes/liste/A",
                                                 "code": "A", "labelLg1": "Code A", "isPartial": true}}}"""))
                .andExpect(status().isOk());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(codeListService).setCodesList(forwarded.capture(), eq(CodeListKind.PARTIAL));
        JSONAssert.assertEquals(REQUIRED_FIELDS_BODY, forwarded.getValue(), JSONCompareMode.STRICT);
    }

    @Test
    void postPartialCodeList_whenTheContributorIsASingleValue_shouldForwardAList() throws Exception {
        when(codeListService.setCodesList(anyString(), any(CodeListKind.class))).thenReturn("CL_PARTIAL");

        // À la création, le front pré-remplit le contributeur par défaut, sous forme de chaîne : le
        // service, qui le lit comme un tableau, échouait.
        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace(
                                "\"creator\": \"DG75-L201\",",
                                "\"creator\": \"DG75-L201\", \"contributor\": \"DG57-L201\",")))
                .andExpect(status().isOk());

        ArgumentCaptor<String> forwarded = ArgumentCaptor.forClass(String.class);
        verify(codeListService).setCodesList(forwarded.capture(), eq(CodeListKind.PARTIAL));
        JSONAssert.assertEquals("{\"contributor\": [\"DG57-L201\"]}", forwarded.getValue(), JSONCompareMode.LENIENT);
    }
}
