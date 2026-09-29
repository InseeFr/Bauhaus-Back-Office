package fr.insee.rmes.modules.operations.operations.webservice;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
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
 * Le corps d'une mise à jour d'opération est lu par le dépôt, pas par Spring : c'est son refus qui
 * doit ressortir en 400, et non une réécriture de l'opération à partir d'un objet vide.
 */
@WebMvcTest(
        value = OperationsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class OperationsResourcesUpdateTest {

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
    void putOperation_whenTheBodyIsRejectedAsUnreadable_shouldReturnBadRequest() throws Exception {
        doThrow(new RmesBadRequestException("The submitted data is invalid"))
                .when(operationsService)
                .setOperation("o1500", "{not json");

        mockMvc.perform(put("/operations/operation/{id}", "o1500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The submitted data is invalid"));
    }
}
