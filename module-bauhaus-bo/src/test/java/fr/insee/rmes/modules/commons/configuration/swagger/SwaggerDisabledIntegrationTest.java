package fr.insee.rmes.modules.commons.configuration.swagger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comportement par défaut (propriété absente) : ni documentation ni UI ne sont exposées.
 */
@SpringBootTest(classes = SwaggerDisabledIntegrationTest.TestConfiguration.class)
@AutoConfigureMockMvc(
        addFilters = false) // sans filtres : on veut voir le 404 de l'absence de route, pas le 401 de la sécurité
class SwaggerDisabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(OpenApiConfiguration.class)
    static class TestConfiguration {}

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void should_not_expose_the_openapi_document() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    @Test
    void should_not_expose_the_swagger_ui() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }

    @Test
    void should_not_register_the_openapi_bean() {
        assertThat(applicationContext.getBeanNamesForType(OpenAPI.class)).isEmpty();
    }
}
