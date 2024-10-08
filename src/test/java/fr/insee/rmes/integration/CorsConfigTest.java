package fr.insee.rmes.integration;

import fr.insee.rmes.config.BaseConfigForMvcTests;
import fr.insee.rmes.config.auth.security.CommonSecurityConfiguration;
import fr.insee.rmes.external.services.authentication.stamps.StampsService;
import fr.insee.rmes.webservice.PublicResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicResources.class, properties = {
        "fr.insee.rmes.bauhaus.cors.allowedOrigin=http://localhost:80",
        "fr.insee.rmes.bauhaus.appHost=http://myfront:80",
        "logging.level.org.springframework.web=debug"})
@Import({BaseConfigForMvcTests.class, CommonSecurityConfiguration.class})
class CorsConfigTest {

    @Autowired
    // Perform mock request on http://localhost:80
    private MockMvc mockMvc;

    @MockBean
    private StampsService stampsService;

    @ValueSource(strings = {
            "http://bauhaus",
            "https://localhost:80",
            "http://localhost:8080"
    })
    @ParameterizedTest
    void whenPerformCorsRequest_shouldBeDenied(String origin) throws Exception {
        mockMvc.perform(get("/stamps")
                        .header("Accept", "application/json")
                        .header("Origin", origin)
                ).andExpect(status().isForbidden())
                .andExpect(content().string("Invalid CORS request"));
    }

    @Test
    void whenPerformNoCorsRequest_shouldBeOK() throws Exception {
        mockMvc.perform(get("/stamps")
                .header("Accept", "application/json")
        ).andExpect(status().isOk());
    }

    @Test
    void whenHeaderOriginIsOK_shouldBeOK() throws Exception {
        mockMvc.perform(get("/stamps")
                .header("Accept", "application/json")
                .header("Origin", "http://localhost:80")
        ).andExpect(status().isOk());
    }

}
