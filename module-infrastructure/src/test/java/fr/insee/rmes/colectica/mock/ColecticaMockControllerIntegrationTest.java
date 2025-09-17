package fr.insee.rmes.colectica.mock;

import fr.insee.rmes.colectica.mock.webservice.ColecticaMockResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ColecticaMockControllerIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = "fr.insee.rmes.bauhaus.colectica.mock-server-enabled=true")
class ColecticaMockControllerIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(ColecticaMockResources.class)
    static class TestConfiguration {
    }

    @Autowired
    private ColecticaMockResources controller;

    @Test
    void shouldLoadControllerWhenEnabled() {
        assertNotNull(controller);
        String response = controller.getColectica();
        assertEquals("Mock Colectica Server Response", response);
    }
}