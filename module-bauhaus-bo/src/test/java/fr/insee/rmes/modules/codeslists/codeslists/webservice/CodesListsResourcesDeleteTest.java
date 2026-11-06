package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import org.junit.jupiter.api.Test;

/**
 * Contrat HTTP des DELETE de listes de codes : un identifiant inconnu doit donner 404, pas 500.
 */
class CodesListsResourcesDeleteTest extends AbstractCodesListsResourcesWebMvcTest {

    @Test
    void deleteCodeList_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new RmesNotFoundException("CodeList not found", "unknown"))
                .when(codeListService)
                .deleteCodeList("unknown", CodeListKind.FULL);

        mockMvc.perform(delete("/codeList/{id}", "unknown")).andExpect(status().isNotFound());
    }

    @Test
    void deletePartialCodeList_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new RmesNotFoundException("CodeList not found", "unknown"))
                .when(codeListService)
                .deleteCodeList("unknown", CodeListKind.PARTIAL);

        mockMvc.perform(delete("/codeList/partial/{id}", "unknown")).andExpect(status().isNotFound());
    }

    @Test
    void deleteCodeForCodeList_whenCodeDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.deleteCodeFromCodeList("notation", "unknown"))
                .thenThrow(new RmesNotFoundException("Code not found in this code list", "unknown"));

        mockMvc.perform(delete("/codeList/detailed/{notation}/codes/{code}", "notation", "unknown"))
                .andExpect(status().isNotFound());
    }
}
