package fr.insee.rmes.modules.operations.operations.webservice;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.operations.operations.domain.model.commands.OperationCommand;
import org.junit.jupiter.api.Test;
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
 * Contrat du corps de requête d'une opération : un corps illisible, sans libellé principal ou, en
 * création, sans série est refusé en 400 nommant le champ, avant que le service n'écrive quoi que
 * ce soit.
 */
@WebMvcTest(
        value = OperationsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class OperationsResourcesValidationTest {

    @MockitoBean
    private OperationsService operationsService;

    @MockitoBean
    private OperationsDocumentationsService documentationsService;

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private BauhausUriBuilder bauhausUriBuilder;

    @Autowired
    MockMvc mockMvc;

    @Test
    void putOperation_whenTheBodyIsEmpty_shouldReturnBadRequestNamingPrefLabelLg1() throws Exception {
        mockMvc.perform(put("/operations/operation/{id}", "s1500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value("prefLabelLg1"));

        verify(operationsService, never()).setOperation(any(), any());
    }

    @Test
    void putOperation_whenTheBodyIsNotValidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/operations/operation/{id}", "s1500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());

        verify(operationsService, never()).setOperation(any(), any());
    }

    @Test
    void putOperation_whenTheYearIsNotANumber_shouldReturnBadRequestNamingYear() throws Exception {
        mockMvc.perform(put("/operations/operation/{id}", "s1500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prefLabelLg1":"Enquête","year":"deux mille"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value("year"));

        verify(operationsService, never()).setOperation(any(), any());
    }

    @Test
    void postOperation_whenTheBodyIsEmpty_shouldReturnBadRequestNamingPrefLabelLg1AndSeries() throws Exception {
        mockMvc.perform(post("/operations/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("prefLabelLg1", "series")));

        verify(operationsService, never()).createOperation(any());
    }

    @Test
    void postOperation_whenTheSeriesHasNoId_shouldReturnBadRequestNamingSeriesId() throws Exception {
        mockMvc.perform(post("/operations/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prefLabelLg1":"Enquête","series":{"id":" "}}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value("series.id"));

        verify(operationsService, never()).createOperation(any());
    }

    @Test
    void putOperation_whenTheBodyIsTheOneTheFrontSends_shouldForwardEveryWrittenFieldToTheService() throws Exception {
        // Lecture de l'opération renvoyée par le front, année saisie comme texte.
        mockMvc.perform(put("/operations/operation/{id}", "s1500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"ignored","prefLabelLg1":"Enquête emploi","prefLabelLg2":"Labour survey",
                                 "altLabelLg1":"EEC","altLabelLg2":"LFS","year":"2024","idSims":"1234",
                                 "created":"2026-01-01T00:00:00","modified":"2026-02-01","validationState":"Modified",
                                 "series":{"id":"s1001","labelLg1":"Série","labelLg2":"Series"}}"""))
                .andExpect(status().isNoContent());

        // La commande transmise doit rester un miroir fidèle : un champ omis ici serait effacé de
        // l'opération, puisque le dépôt réécrit l'objet entier.
        verify(operationsService)
                .setOperation(
                        "s1500",
                        new OperationCommand(
                                "Enquête emploi",
                                "Labour survey",
                                "EEC",
                                "LFS",
                                "s1001",
                                2024,
                                "1234",
                                "2026-01-01T00:00:00"));
    }

    @Test
    void putOperation_whenTheYearIsCleared_shouldForwardNoYear() throws Exception {
        mockMvc.perform(put("/operations/operation/{id}", "s1500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prefLabelLg1":"Enquête emploi","year":""}"""))
                .andExpect(status().isNoContent());

        verify(operationsService)
                .setOperation(
                        "s1500", new OperationCommand("Enquête emploi", null, null, null, null, null, null, null));
    }

    @Test
    void postOperation_whenTheBodyIsComplete_shouldCreateTheOperationInItsSeries() throws Exception {
        mockMvc.perform(post("/operations/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"prefLabelLg1":"Enquête emploi","prefLabelLg2":"Labour survey","year":"2024",
                                 "series":{"id":"s1001"}}"""))
                .andExpect(status().isOk());

        verify(operationsService)
                .createOperation(
                        new OperationCommand("Enquête emploi", "Labour survey", null, null, "s1001", 2024, null, null));
    }
}
