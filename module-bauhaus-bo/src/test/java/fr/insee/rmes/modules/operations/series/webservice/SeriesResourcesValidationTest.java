package fr.insee.rmes.modules.operations.series.webservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.operations.series.domain.model.SeriesLink;
import fr.insee.rmes.modules.operations.series.domain.model.commands.SeriesCommand;
import java.util.List;
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
 * Contrat du corps de requête d'une série : un corps illisible ou sans libellé principal est refusé
 * en 400, avant que le service n'écrive quoi que ce soit. Sans cette validation, un corps sans
 * libellé atteignait le dépôt, qui réécrivait la série avec des champs vides.
 */
@WebMvcTest(
        value = SeriesResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class SeriesResourcesValidationTest {

    /** Corps tel que le front le renvoie : sa lecture de la série, plus les champs modifiés. */
    private static final String VALID_BODY = """
            {"id":"ignored","prefLabelLg1":"Série","prefLabelLg2":"Series","altLabelLg1":"S","altLabelLg2":"S EN",
             "abstractLg1":"Résumé","abstractLg2":"Abstract","historyNoteLg1":"Note","historyNoteLg2":"Note EN",
             "family":{"id":"s60","labelLg1":"Famille"},"typeCode":"S","typeList":"CL_SOURCE_CATEGORY",
             "accrualPeriodicityCode":"A","accrualPeriodicityList":"CL_FREQ",
             "publishers":[{"id":"DG75-A001","labelLg1":"Insee"}],"contributors":[{"id":"DG75-B002"}],
             "dataCollectors":[{"id":"DG75-C003"}],"creators":"DG75-D004",
             "seeAlso":[{"id":"p1","type":"indicator","labelLg1":"Indicateur"}],
             "replaces":[{"id":"s1","type":"series"}],"isReplacedBy":[{"id":"s3","type":"series"}],
             "themes":["http://bauhaus/themes/th1"],"idSims":"1234","created":"2026-09-29T10:00:00",
             "updated":"2026-09-30","validationState":"Modified","operations":[{"id":"s1234"}],
             "generate":[{"id":"p2","type":"indicator"}]}""";

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService documentationsService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void putSeries_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/operations/series/{id}", "s1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value("prefLabelLg1"));

        verify(operationsService, never()).setSeries(any(), any());
    }

    @Test
    void putSeries_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/operations/series/{id}", "s1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(operationsService, never()).setSeries(any(), any());
    }

    @Test
    void postSeries_whenTheBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/operations/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value("prefLabelLg1"));

        verify(operationsService, never()).createSeries(any());
    }

    @Test
    void putSeries_whenTheBodyIsComplete_shouldForwardEveryWrittenFieldToTheService() throws Exception {
        mockMvc.perform(put("/operations/series/{id}", "s1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        // La commande transmise doit rester un miroir fidèle : un champ omis ici serait effacé de
        // la série, puisque le dépôt réécrit l'objet entier.
        ArgumentCaptor<SeriesCommand> forwarded = ArgumentCaptor.forClass(SeriesCommand.class);
        verify(operationsService).setSeries(eq("s1001"), forwarded.capture());
        assertThat(forwarded.getValue())
                .isEqualTo(new SeriesCommand(
                        "Série",
                        "Series",
                        "S",
                        "S EN",
                        "Résumé",
                        "Abstract",
                        "Note",
                        "Note EN",
                        "s60",
                        "S",
                        "CL_SOURCE_CATEGORY",
                        "A",
                        "CL_FREQ",
                        List.of(new SeriesLink("DG75-A001", null)),
                        List.of(new SeriesLink("DG75-B002", null)),
                        List.of(new SeriesLink("DG75-C003", null)),
                        List.of("DG75-D004"),
                        List.of(new SeriesLink("p1", "indicator")),
                        List.of(new SeriesLink("s1", "series")),
                        List.of(new SeriesLink("s3", "series")),
                        List.of("http://bauhaus/themes/th1"),
                        "1234",
                        "2026-09-29T10:00:00"));
    }
}
