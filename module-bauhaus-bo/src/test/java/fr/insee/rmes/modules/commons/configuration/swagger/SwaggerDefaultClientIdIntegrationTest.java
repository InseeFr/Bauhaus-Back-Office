package fr.insee.rmes.modules.commons.configuration.swagger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
 * Sans surcharge, Swagger UI s'authentifie avec le client public {@code bauhaus} (celui du front),
 * jamais avec le client du compte de service que le back utilise pour ses propres appels.
 */
@SpringBootTest(classes = SwaggerDefaultClientIdIntegrationTest.TestConfiguration.class)
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.swagger.enabled=true",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://auth.test/realms/bauhaus",
            "fr.insee.rmes.bauhaus.keycloak.defaultrealm.clientid=bauhaus-service"
        })
class SwaggerDefaultClientIdIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(OpenApiConfiguration.class)
    static class TestConfiguration {}

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mvc;

    @Test
    void should_use_the_public_bauhaus_client_rather_than_the_service_account() throws Exception {
        mvc.perform(get("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"bauhaus\"")))
                .andExpect(content().string(not(containsString("bauhaus-service"))));
    }
}
