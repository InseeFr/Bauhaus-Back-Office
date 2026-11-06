package fr.insee.rmes.modules.commons.configuration.swagger;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le contrat OpenAPI décrit le format unique des réponses d'erreur (ADR-1264, ticket 18) : un
 * schéma {@code ApiError}, et une réponse {@code default} qui y renvoie sur chaque opération.
 */
@SpringBootTest(classes = OpenApiErrorContractIntegrationTest.TestConfiguration.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"fr.insee.rmes.bauhaus.swagger.enabled=true"})
class OpenApiErrorContractIntegrationTest {

    private static final String API_ERROR_REF = "#/components/schemas/ApiError";

    @Configuration
    @EnableAutoConfiguration
    @Import({OpenApiConfiguration.class, TestConfiguration.SampleResources.class})
    static class TestConfiguration {

        @RestController
        static class SampleResources {

            @GetMapping("/samples")
            String list() {
                return "[]";
            }

            @PostMapping("/samples")
            String create() {
                return "s1";
            }
        }

        @Bean
        SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(request -> request.anyRequest().permitAll())
                    .build();
        }
    }

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    @Test
    void should_describe_the_error_body_of_the_adr() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ApiError.required").value(containsInAnyOrder("message")))
                .andExpect(jsonPath("$.components.schemas.ApiError.properties.message.type")
                        .value("string"))
                .andExpect(jsonPath("$.components.schemas.ApiError.properties.code.type")
                        .value("string"))
                .andExpect(jsonPath("$.components.schemas.ApiError.properties.params.additionalProperties.type")
                        .value("string"))
                .andExpect(jsonPath("$.components.schemas.ApiError.properties.errors.items.$ref")
                        .value("#/components/schemas/FieldError"))
                .andExpect(jsonPath("$.components.schemas.FieldError.required")
                        .value(containsInAnyOrder("field", "message")));
    }

    @Test
    void should_document_every_operation_error_with_the_error_body() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./samples.get.responses.default.content['application/json'].schema.$ref")
                        .value(API_ERROR_REF))
                .andExpect(jsonPath("$.paths./samples.post.responses.default.content['application/json'].schema.$ref")
                        .value(API_ERROR_REF));
    }
}
