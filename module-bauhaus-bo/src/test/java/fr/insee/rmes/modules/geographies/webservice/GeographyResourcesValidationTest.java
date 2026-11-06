package fr.insee.rmes.modules.geographies.webservice;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.hamcrest.Matchers;
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
 * Contrat du corps des requêtes de création et de modification d'un territoire géographique : un
 * corps sans libellés est refusé en 400 nommant le champ, avant que le service n'écrive quoi que ce
 * soit. Avant, le service refusait les libellés absents avec un 400 sans champ.
 */
@WebMvcTest(
        value = GeographyResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class GeographyResourcesValidationTest {

    private static final String REQUIRED_FIELDS_BODY = """
            {"labelLg1": "Zone d'emploi de Lille",
             "labelLg2": "Lille employment area"}""";

    @MockitoBean
    GeographyService geographyService;

    @MockitoBean
    UserDecoder userDecoder;

    @Autowired
    MockMvc mockMvc;

    @Test
    void postTerritory_whenTheBodyIsEmpty_shouldReturnBadRequestNamingBothLabels() throws Exception {
        mockMvc.perform(post("/geo/territory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.containsInAnyOrder("labelLg1", "labelLg2")));

        verifyNoInteractions(geographyService);
    }

    @Test
    void putTerritory_whenALabelIsBlank_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/geo/territory/{id}", "t1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUIRED_FIELDS_BODY.replace("\"Lille employment area\"", "\" \"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(Matchers.contains("labelLg2")));

        verifyNoInteractions(geographyService);
    }
}
