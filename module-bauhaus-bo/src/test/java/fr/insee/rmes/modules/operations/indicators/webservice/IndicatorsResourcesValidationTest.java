package fr.insee.rmes.modules.operations.indicators.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorLink;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.IndicatorCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
 * Contrat du corps de requête d'un indicateur : un corps illisible ou sans libellé principal est
 * refusé en 400, avant que le service n'écrive quoi que ce soit. Sans cette validation, un corps
 * invalide était avalé par le dépôt et l'indicateur existant réécrit avec des champs vides.
 */
@WebMvcTest(
        value = IndicatorsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class IndicatorsResourcesValidationTest {

    private static final String VALID_BODY = """
            {"prefLabelLg1":"Indicateur","prefLabelLg2":"Indicator","idSims":"sims-1","created":"2026-09-23",
             "wasGeneratedBy":[{"id":"s1","type":"series"}]}""";

    @MockitoBean
    private OperationsService operationsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void putIndicator_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/operations/indicator/{id}", "p1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value("prefLabelLg1"));

        verify(operationsService, never()).setIndicator(any(), any());
    }

    @Test
    void putIndicator_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/operations/indicator/{id}", "p1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(operationsService, never()).setIndicator(any(), any());
    }

    @Test
    void postIndicator_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/operations/indicator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(operationsService, never()).setIndicator(any());
    }

    @Test
    void putIndicator_whenTheBodyIsComplete_shouldForwardEveryFieldToTheService() throws Exception {
        mockMvc.perform(put("/operations/indicator/{id}", "p1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        // La commande transmise doit rester un miroir fidèle : un champ omis ici serait effacé de
        // l'indicateur, puisque le dépôt réécrit l'objet entier.
        ArgumentCaptor<IndicatorCommand> forwarded = ArgumentCaptor.forClass(IndicatorCommand.class);
        verify(operationsService).setIndicator(eq("p1001"), forwarded.capture());
        IndicatorCommand command = forwarded.getValue();
        assertThat(command.prefLabelLg1()).isEqualTo("Indicateur");
        assertThat(command.prefLabelLg2()).isEqualTo("Indicator");
        assertThat(command.idSims()).isEqualTo("sims-1");
        assertThat(command.created()).isEqualTo("2026-09-23");
        assertThat(command.wasGeneratedBy()).containsExactly(new IndicatorLink("s1", "series"));
    }
}
