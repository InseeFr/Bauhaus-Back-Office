package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.codeslists.partialcodeslists.webservice.PartialCodeListsResources;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat HTTP des lectures et des mises à jour de listes de codes : une ressource inconnue doit
 * donner 404 et une url incohérente avec le corps de la requête 400, jamais 200 ni 500.
 */
@WebMvcTest(
        value = { CodesListsResources.class, PartialCodeListsResources.class },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class CodesListsResourcesErrorMappingTest {

    @MockitoBean
    private CodeListService codeListService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getCodeListByNotation_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.getCodeListJson("unknown"))
                .thenThrow(new RmesNotFoundException("CodeList not found", "unknown"));

        mockMvc.perform(get("/codeList/{notation}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDetailedCodesListByNotation_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.getDetailedCodesList("unknown"))
                .thenThrow(new RmesNotFoundException("CodeList not found", "unknown"));

        mockMvc.perform(get("/codeList/detailed/{notation}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCodesList_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.setCodesList("unknown", "{}", CodeListKind.FULL))
                .thenThrow(new RmesNotFoundException("CodeList not found", "unknown"));

        mockMvc.perform(put("/codeList/{id}", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCodesList_whenTheBodyIdDoesNotMatchTheUrlId_shouldReturnBadRequest() throws Exception {
        when(codeListService.setCodesList("CL_TEST", "{}", CodeListKind.FULL))
                .thenThrow(new RmesBadRequestException("The id of the list should match the id of the url", "CL_TEST"));

        mockMvc.perform(put("/codeList/{id}", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCodeForCodeList_whenTheBodyCodeDoesNotMatchTheUrlCode_shouldReturnBadRequest() throws Exception {
        when(codeListService.updateCodeFromCodeList("CL_TEST", "toto", "{}"))
                .thenThrow(new RmesBadRequestException("The code of the body should match the code of the url", "toto"));

        mockMvc.perform(put("/codeList/detailed/{id}/codes/{code}", "CL_TEST", "toto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCodeByNotation_shouldNoLongerBeExposed() throws Exception {
        mockMvc.perform(get("/codeList/{notation}/code/{code}", "CL_TEST", "toto"))
                .andExpect(status().isNotFound());
    }
}
