package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import org.junit.jupiter.api.Test;

/**
 * Contrat HTTP des publications de listes de codes : republier une liste déjà publiée doit donner
 * 400 <em>avec</em> le code d'erreur dans le corps, celui dont le front a besoin pour traduire le
 * message. Un test unitaire de contrôleur ne le verrait pas : c'est le {@code RmesExceptionHandler},
 * et donc l'appartenance du contrôleur à sa liste {@code assignableTypes}, qui est en jeu ici.
 */
class CodesListsResourcesPublishTest extends AbstractCodesListsResourcesWebMvcTest {

    @Test
    void publishCodeList_whenTheCodeListIsAlreadyPublished_shouldReturnBadRequestWithTheErrorCode() throws Exception {
        doThrow(alreadyPublished("CL_TEST")).when(codeListService).publishCodeList("CL_TEST", CodeListKind.FULL);

        mockMvc.perform(put("/codeList/{id}/validate", "CL_TEST"))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string(org.hamcrest.Matchers.containsString("\"code\":" + ErrorCodes.ALREADY_PUBLISHED)));
    }

    @Test
    void publishPartialCodeList_whenTheCodeListIsAlreadyPublished_shouldReturnBadRequestWithTheErrorCode()
            throws Exception {
        doThrow(alreadyPublished("CL_PARTIAL"))
                .when(codeListService)
                .publishCodeList("CL_PARTIAL", CodeListKind.PARTIAL);

        mockMvc.perform(put("/codeList/partial/{id}/validate", "CL_PARTIAL"))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string(org.hamcrest.Matchers.containsString("\"code\":" + ErrorCodes.ALREADY_PUBLISHED)));
    }

    private static RmesBadRequestException alreadyPublished(String id) {
        return new RmesBadRequestException(
                ErrorCodes.ALREADY_PUBLISHED, "This codes list is already published", "Codes list: " + id);
    }
}
