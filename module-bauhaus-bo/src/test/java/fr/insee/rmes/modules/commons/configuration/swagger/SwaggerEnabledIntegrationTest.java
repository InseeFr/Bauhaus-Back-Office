package fr.insee.rmes.modules.commons.configuration.swagger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Swagger activé : la documentation et son UI sont servies, et restent joignables sans jeton
 * (le navigateur qui charge l'UI n'en présente aucun), alors que le reste de l'API demeure protégé.
 */
@SpringBootTest(classes = SwaggerEnabledIntegrationTest.TestConfiguration.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.swagger.enabled=true")
class SwaggerEnabledIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(OpenApiConfiguration.class)
    static class TestConfiguration {

        /**
         * Reproduit la chaîne applicative : tout ce qui n'est pas explicitement ouvert est authentifié.
         */
        @Bean
        SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(request -> request.anyRequest().authenticated())
                    .build();
        }
    }

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    @Test
    void should_describe_the_application_in_the_openapi_document() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Bauhaus"));
    }

    @Test
    void should_declare_a_bearer_security_scheme_so_the_ui_can_authenticate() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.security[0].bearerAuth").exists());
    }

    @Test
    void should_serve_the_swagger_ui_without_a_token() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void should_keep_the_rest_of_the_api_authenticated() throws Exception {
        mvc.perform(get("/any-other-endpoint")).andExpect(status().is4xxClientError());
    }
}
