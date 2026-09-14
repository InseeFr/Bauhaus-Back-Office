package fr.insee.rmes.modules.commons.configuration.swagger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Swagger activé sur un environnement sans Keycloak déclaré : la documentation reste servie, mais
 * sans flow OAuth2 — seul le collage manuel d'un jeton (schéma {@code bearerAuth}) est possible.
 */
@SpringBootTest(classes = SwaggerWithoutIssuerIntegrationTest.TestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.swagger.enabled=true",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri="
        })
class SwaggerWithoutIssuerIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(OpenApiConfiguration.class)
    static class TestConfiguration {}

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    @Test
    void should_still_document_the_api_with_the_bearer_scheme_only() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.oauth2").doesNotExist())
                .andExpect(jsonPath("$.security[?(@.oauth2)]").doesNotExist());
    }
}
