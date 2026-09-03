package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.codeslists.partialcodeslists.webservice.PartialCodeListsResources;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat HTTP des publications de listes de codes : republier une liste déjà publiée doit donner
 * 400 <em>avec</em> le code d'erreur dans le corps, celui dont le front a besoin pour traduire le
 * message. Un test unitaire de contrôleur ne le verrait pas : c'est le {@code RmesExceptionHandler},
 * et donc l'appartenance du contrôleur à sa liste {@code assignableTypes}, qui est en jeu ici.
 */
@WebMvcTest(
        value = { CodesListsResources.class, PartialCodeListsResources.class },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class CodesListsResourcesPublishTest {

    @MockitoBean
    private CodeListService codeListService;

    @MockitoBean
    private CodesListsService codesListsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void publishCodeList_whenTheCodeListIsAlreadyPublished_shouldReturnBadRequestWithTheErrorCode() throws Exception {
        doThrow(alreadyPublished("CL_TEST")).when(codeListService).publishCodeList("CL_TEST", CodeListKind.FULL);

        mockMvc.perform(put("/codeList/{id}/validate", "CL_TEST"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"code\":" + ErrorCodes.ALREADY_PUBLISHED)));
    }

    @Test
    void publishPartialCodeList_whenTheCodeListIsAlreadyPublished_shouldReturnBadRequestWithTheErrorCode() throws Exception {
        doThrow(alreadyPublished("CL_PARTIAL")).when(codeListService).publishCodeList("CL_PARTIAL", CodeListKind.PARTIAL);

        mockMvc.perform(put("/codeList/partial/{id}/validate", "CL_PARTIAL"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"code\":" + ErrorCodes.ALREADY_PUBLISHED)));
    }

    private static RmesBadRequestException alreadyPublished(String id) {
        return new RmesBadRequestException(ErrorCodes.ALREADY_PUBLISHED, "This codes list is already published", "Codes list: " + id);
    }
}
