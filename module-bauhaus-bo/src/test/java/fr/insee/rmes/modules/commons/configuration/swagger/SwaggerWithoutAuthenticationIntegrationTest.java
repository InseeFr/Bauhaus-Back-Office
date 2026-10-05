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
 * Swagger activé hors PROD : l'API n'y est pas authentifiée (le {@code DevAuthenticationFilter}
 * authentifie chaque requête sans jeton), la documentation ne décrit donc aucun schéma de sécurité
 * — pas de bouton « Authorize » inutile dans l'UI, même si un Keycloak est déclaré.
 */
@SpringBootTest(classes = SwaggerWithoutAuthenticationIntegrationTest.TestConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.swagger.enabled=true",
            "fr.insee.rmes.bauhaus.env=local",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://auth.test/realms/bauhaus",
            "fr.insee.rmes.bauhaus.swagger.oauth.client-id=bauhaus-swagger"
        })
class SwaggerWithoutAuthenticationIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(OpenApiConfiguration.class)
    static class TestConfiguration {}

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    @Test
    void should_document_the_api_without_any_security_scheme() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value(OpenApiConfiguration.TITLE))
                .andExpect(jsonPath("$.components.securitySchemes").doesNotExist())
                .andExpect(jsonPath("$.security").doesNotExist());
    }
}
