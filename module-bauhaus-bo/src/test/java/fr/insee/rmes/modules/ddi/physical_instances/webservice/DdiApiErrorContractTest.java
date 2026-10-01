package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static fr.insee.rmes.modules.commons.webservice.ApiErrorContract.apiError;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.modules.commons.webservice.UnexpectedErrorHandler;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidSentinelValuesException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Réponses d'erreur de {@link DdiResources} au format de l'ADR-1264 (ticket 18) : un item public
 * inconnu ne répond plus un 404 au corps vide, un refus de valeurs sentinelles porte un code.
 */
@ExtendWith(MockitoExtension.class)
class DdiApiErrorContractTest {

    @Mock
    private DDIService ddiService;

    @Mock
    private Ddi4SchemaService ddi4SchemaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new DdiResources(ddiService, null, null, null, null, null, null, ddi4SchemaService))
                .setControllerAdvice(new DdiExceptionHandler(), new UnexpectedErrorHandler())
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void an_unknown_public_item_answers_404_with_a_message(String accept) throws Exception {
        mockMvc.perform(get("/ddi/public/item/{agency}/{id}", "fr.insee", "unknown")
                        .accept(accept))
                .andExpect(status().isNotFound())
                .andExpect(apiError());
    }

    @ParameterizedTest
    @ValueSource(strings = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    void an_unknown_public_code_list_answers_404_with_a_message(String accept) throws Exception {
        mockMvc.perform(get("/ddi/public/codelist/{agency}/{id}", "fr.insee", "unknown")
                        .accept(accept))
                .andExpect(status().isNotFound())
                .andExpect(apiError());
    }

    /** Chaque écart au schéma est une erreur sur le corps entier ; {@code message} dit l'essentiel. */
    @Test
    void a_ddi4_document_out_of_the_schema_answers_400_with_one_error_per_violation() throws Exception {
        when(ddi4SchemaService.validate(any()))
                .thenReturn(List.of("$.items[0]: required property 'ID' not found", "$.x: is not allowed"));

        mockMvc.perform(post("/ddi/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(apiError())
                .andExpect(jsonPath("$.code").value("DDI4_INVALID"))
                .andExpect(jsonPath("$.errors[0].field").value("body"))
                .andExpect(jsonPath("$.errors[0].message").value("$.items[0]: required property 'ID' not found"))
                .andExpect(jsonPath("$.errors[1].message").value("$.x: is not allowed"));
    }

    @Test
    void a_valid_ddi4_document_still_answers_200() throws Exception {
        when(ddi4SchemaService.validate(any())).thenReturn(List.of());

        mockMvc.perform(post("/ddi/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void a_sentinel_list_without_label_answers_400_with_a_translatable_code() throws Exception {
        when(ddiService.updateFullPhysicalInstance(eq("fr.insee"), eq("pi-1"), any()))
                .thenThrow(InvalidSentinelValuesException.missingRepresentationLabel("fr.insee", "mmvr-1"));

        mockMvc.perform(put("/ddi/physical-instance/{agencyId}/{id}", "fr.insee", "pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(apiError())
                .andExpect(jsonPath("$.code").value("DDI_SENTINEL_REPRESENTATION_LABEL_REQUIRED"))
                .andExpect(jsonPath("$.params.agency").value("fr.insee"))
                .andExpect(jsonPath("$.params.id").value("mmvr-1"));
    }
}
