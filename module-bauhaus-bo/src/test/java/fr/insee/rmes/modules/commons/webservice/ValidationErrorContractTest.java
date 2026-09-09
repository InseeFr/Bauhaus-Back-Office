package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.bauhaus_services.datasets.DatasetService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.datasets.datasets.webservice.DatasetResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrat d'erreur de validation, commun à tous les modules : un corps de requête refusé
 * répond 400 avec {@code {"errors":[{"field","message"}]}}.
 * <p>
 * Le test est monté sur {@link DatasetResources}, qui figure dans les {@code assignableTypes}
 * de {@code RmesExceptionHandler} (@Order(2)) : c'est tout l'intérêt du test, il vérifie que
 * le handler de validation passe devant lui et impose le contrat, au lieu du corps
 * {@code ProblemDetail} par défaut de {@code ResponseEntityExceptionHandler}.
 */
@WebMvcTest(
        value = DatasetResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
        properties = {
                "fr.insee.rmes.bauhaus.modules.datasets.enabled=true"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ValidationErrorContractTest {

    @MockitoBean
    private DatasetService datasetService;

    @Autowired
    private MockMvc mockMvc;

    /** Contrainte Bean Validation violée : MethodArgumentNotValidException. */
    @Test
    void violated_constraint_should_name_the_faulty_field() throws Exception {
        mockMvc.perform(patch("/datasets/{id}", "d1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"numObservations": -1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("numObservations"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    /**
     * Corps illisible : HttpMessageNotReadableException. Sans cette branche, le typage des
     * champs (lot 11, {@code year}) rendrait un 400 nu, hors contrat.
     */
    @Test
    void unreadable_body_should_follow_the_same_contract() throws Exception {
        mockMvc.perform(patch("/datasets/{id}", "d1")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"numObservations": "pas-un-entier"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("numObservations"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }
}
