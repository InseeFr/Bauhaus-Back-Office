package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat HTTP des DELETE de listes de codes : un identifiant inconnu doit donner 404, pas 500.
 */
@WebMvcTest(
        value = { CodesListsResources.class, PartialCodeListsResources.class },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class CodesListsResourcesDeleteTest {

    @MockitoBean
    private CodeListService codeListService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void deleteCodeList_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new RmesNotFoundException("CodeList not found", "unknown"))
                .when(codeListService).deleteCodeList("unknown", CodeListKind.FULL);

        mockMvc.perform(delete("/codeList/{id}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePartialCodeList_whenCodeListDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new RmesNotFoundException("CodeList not found", "unknown"))
                .when(codeListService).deleteCodeList("unknown", CodeListKind.PARTIAL);

        mockMvc.perform(delete("/codeList/partial/{id}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCodeForCodeList_whenCodeDoesNotExist_shouldReturnNotFound() throws Exception {
        when(codeListService.deleteCodeFromCodeList("notation", "unknown"))
                .thenThrow(new RmesNotFoundException("Code not found in this code list", "unknown"));

        mockMvc.perform(delete("/codeList/detailed/{notation}/codes/{code}", "notation", "unknown"))
                .andExpect(status().isNotFound());
    }
}
