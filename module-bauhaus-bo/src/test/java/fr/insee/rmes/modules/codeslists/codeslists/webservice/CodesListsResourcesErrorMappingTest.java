package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.errors.CodesListErrorCodes;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListIdMismatchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Contrat HTTP des lectures et des mises à jour de listes de codes : une ressource inconnue doit
 * donner 404 et une url incohérente avec le corps de la requête 400, jamais 200 ni 500.
 */
class CodesListsResourcesErrorMappingTest extends AbstractCodesListsResourcesWebMvcTest {

    @Test
    void getCodeListByNotation_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.getCodeListJson("unknown"))
                .thenThrow(new RmesNotFoundException("CodeList not found", "unknown"));

        mockMvc.perform(get("/codeList/{notation}", "unknown")).andExpect(status().isNotFound());
    }

    @Test
    void getDetailedCodesListByNotation_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.getDetailedCodesList("unknown"))
                .thenThrow(new RmesNotFoundException("CodeList not found", "unknown"));

        mockMvc.perform(get("/codeList/detailed/{notation}", "unknown")).andExpect(status().isNotFound());
    }

    /**
     * Corps complet : la validation Bean Validation passe avant le contrôleur, un corps placeholder
     * donnerait 400 quelle que soit la réponse du service.
     */
    private static final String CODES_LIST_BODY = """
            {
              "id": "%s",
              "labelLg1": "libellé",
              "labelLg2": "label",
              "creator": "http://bauhaus/HIE000000",
              "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/Public",
              "lastListUriSegment": "cl-test",
              "lastClassUriSegment": "ClTest",
              "lastCodeUriSegment": "cl-test-code"
            }""";

    @Test
    void updateCodesList_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codesListsService.update(any(), any())).thenThrow(new CodesListNotFoundException("CodeList not found"));

        mockMvc.perform(put("/codeList/{id}", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CODES_LIST_BODY.formatted("unknown")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCodesList_whenTheBodyIdDoesNotMatchTheUrlId_shouldReturnBadRequest() throws Exception {
        when(codesListsService.update(any(), any()))
                .thenThrow(new CodesListIdMismatchException("The id of the list should match the id of the url"));

        mockMvc.perform(put("/codeList/{id}", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CODES_LIST_BODY.formatted("CL_OTHER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCodeForCodeList_whenTheBodyCodeDoesNotMatchTheUrlCode_shouldReturnBadRequest() throws Exception {
        CodeRequest body = new CodeRequest("tutu", "libellé", "label", null, null);
        when(codeListService.updateCodeFromCodeList("CL_TEST", "toto", body))
                .thenThrow(
                        new RmesBadRequestException("The code of the body should match the code of the url", "toto"));

        mockMvc.perform(put("/codeList/detailed/{id}/codes/{code}", "CL_TEST", "toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"tutu","labelLg1":"libellé","labelLg2":"label"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCodeForCodeList_whenTheCodeAlreadyExists_shouldReturnBadRequest() throws Exception {
        CodeRequest body = new CodeRequest("A", "libellé", "label", null, null);
        when(codeListService.addCodeFromCodeList("CL_TEST", body))
                .thenThrow(new RmesBadRequestException(
                        CodesListErrorCodes.CODE_LIST_CODE_ALREADY_EXISTS,
                        "Code already exists in this code list",
                        "A"));

        mockMvc.perform(post("/codeList/detailed/{id}/codes", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"A","labelLg1":"libellé","labelLg2":"label"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCodeByNotation_shouldNoLongerBeExposed() throws Exception {
        mockMvc.perform(get("/codeList/{notation}/code/{code}", "CL_TEST", "toto"))
                .andExpect(status().isNotFound());
    }
}
