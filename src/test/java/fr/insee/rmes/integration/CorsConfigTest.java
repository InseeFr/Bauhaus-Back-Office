package fr.insee.rmes.integration;

import fr.insee.rmes.config.BaseConfigForMvcTests;
import fr.insee.rmes.webservice.PublicResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PublicResources.class,properties = {"fr.insee.rmes.bauhaus.cors.allowedOrigin=","fr.insee.rmes.bauhaus.appHost="})
@Import(BaseConfigForMvcTests.class)
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenPerformCorsRequest_shouldBeDenied(){

    }

}
