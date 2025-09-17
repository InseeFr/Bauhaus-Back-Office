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

    @Test
    void shouldGetPhysicalInstances() {
        assertNotNull(controller);
        var instances = controller.getPhysicalInstances();
        assertNotNull(instances);
        assertEquals(3, instances.size());
        assertEquals("pi-1", instances.get(0).get("id"));
        assertEquals("Physical Instance 1", instances.get(0).get("label"));
    }

    @Test
    void shouldGetPhysicalInstanceById() {
        assertNotNull(controller);
        var instance = controller.getPhysicalInstance("test-id");
        assertNotNull(instance);
        assertEquals("test-id", instance.get("id"));
        assertEquals("Physical Instance test-id", instance.get("label"));
    }
}