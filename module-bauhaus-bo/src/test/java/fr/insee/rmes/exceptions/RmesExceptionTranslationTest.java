package fr.insee.rmes.exceptions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.commons.domain.exceptions.ThemeFetchException;
import fr.insee.rmes.modules.commons.domain.port.clientside.ThemeService;
import fr.insee.rmes.modules.commons.webservice.ThemeResources;
import fr.insee.rmes.modules.geographies.webservice.GeographyResources;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.webservice.DocumentDescriptionResources;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import fr.insee.rmes.modules.organisations.webservice.StampResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contrôleurs longtemps absents des {@code assignableTypes} de {@link RmesExceptionHandler} :
 * une {@link RmesException} qu'ils laissent sortir doit être traduite comme ailleurs, et non
 * partir en exception non gérée (ticket 1 de l'audit #1264).
 */
@WebMvcTest(
        controllers = {
            GeographyResources.class,
            DocumentDescriptionResources.class,
            StampResources.class,
            ThemeResources.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = "fr.insee.rmes.bauhaus.modules.operations.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class RmesExceptionTranslationTest {

    private static final String MESSAGE = "Unable to read the repository";

    @MockitoBean
    GeographyService geographyService;

    @MockitoBean
    DocumentDescriptionService documentDescriptionService;

    @MockitoBean
    OrganisationService organisationService;

    @MockitoBean
    ThemeService themeService;

    @Autowired
    MockMvc mockMvc;

    private static RmesException rmesException() {
        return new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), MESSAGE, "technical details");
    }

    @Test
    void geography_resources_translate_a_rmes_exception() throws Exception {
        when(geographyService.getGeoFeatures()).thenThrow(rmesException());

        mockMvc.perform(get("/geo/territories"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    @Test
    void document_description_resources_translate_a_rmes_exception() throws Exception {
        when(documentDescriptionService.getDescription(any(), any())).thenThrow(rmesException());

        mockMvc.perform(get("/operations/documents/{id}", "1001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    @Test
    void stamp_resources_translate_a_rmes_exception() throws Exception {
        when(organisationService.getStamps()).thenThrow(rmesException());

        mockMvc.perform(get("/stamps"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    /**
     * {@link ThemeResources} ne peut pas laisser sortir de {@link RmesException} (son service ne
     * lève que {@link ThemeFetchException}) : c'est sa {@code ResponseStatusException} qui passe
     * par le filet, sans exposer le message de l'exception d'origine.
     */
    @Test
    void theme_resources_answer_a_generic_message_on_failure() throws Exception {
        when(themeService.getThemes()).thenThrow(new ThemeFetchException(new IllegalStateException("sparql")));

        mockMvc.perform(get("/themes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.detail").doesNotExist())
                .andExpect(content().string(not(containsString("Failed to fetch themes"))));
    }
}
