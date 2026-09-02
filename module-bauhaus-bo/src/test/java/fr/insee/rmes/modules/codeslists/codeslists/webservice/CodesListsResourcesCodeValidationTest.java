package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat du corps de requête d'un code : les champs obligatoires sont refusés en 400 par Bean
 * Validation, avant que le service ne touche au dépôt. Sans cette validation, un corps incomplet
 * remontait en 500 (JSONException sur le champ manquant).
 */
@WebMvcTest(
        value = CodesListsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class CodesListsResourcesCodeValidationTest {

    private static final String VALID_BODY = """
            {"code":"A","labelLg1":"libellé","labelLg2":"label"}""";

    @MockitoBean
    private CodeListService codeListService;

    @MockitoBean
    private CodesListsService codesListsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void addCodeForCodeList_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/codeList/detailed/{id}/codes", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(
                        containsInAnyOrder("code", "labelLg1", "labelLg2")));

        verify(codeListService, never()).addCodeFromCodeList(any(), any());
    }

    @Test
    void addCodeForCodeList_whenTheCodeIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/codeList/detailed/{id}/codes", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"  ","labelLg1":"libellé","labelLg2":"label"}"""))
                .andExpect(status().isBadRequest());

        verify(codeListService, never()).addCodeFromCodeList(any(), any());
    }

    @Test
    void addCodeForCodeList_whenTheBodyIsComplete_shouldReturnCreated() throws Exception {
        when(codeListService.addCodeFromCodeList(any(), any())).thenReturn("A");

        mockMvc.perform(post("/codeList/detailed/{id}/codes", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("A"));
    }

    @Test
    void updateCodeForCodeList_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/codeList/detailed/{id}/codes/{code}", "CL_TEST", "A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(codeListService, never()).updateCodeFromCodeList(any(), any(), any());
    }

    @Test
    void updateCodeForCodeList_whenTheBodyIsComplete_shouldReturnOk() throws Exception {
        when(codeListService.updateCodeFromCodeList(any(), any(), any())).thenReturn("A");

        mockMvc.perform(put("/codeList/detailed/{id}/codes/{code}", "CL_TEST", "A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A"));
    }
}
