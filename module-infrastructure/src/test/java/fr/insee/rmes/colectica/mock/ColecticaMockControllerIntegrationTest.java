package fr.insee.rmes.colectica.mock;

import fr.insee.rmes.colectica.dto.QueryRequest;
import fr.insee.rmes.colectica.mock.webservice.ColecticaMockResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ColecticaMockControllerIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = {
    "fr.insee.rmes.bauhaus.colectica.mock-server-enabled=true",
    "fr.insee.rmes.bauhaus.colectica.baseURI=http://localhost:8082/api/colectica",
    "fr.insee.rmes.bauhaus.colectica.itemTypes=a51e85bb-6259-4488-8df2-f08cb43485f8"
})
class ColecticaMockControllerIntegrationTest {

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "fr.insee.rmes.colectica.mock")
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
        
        // Create request body with itemTypes
        QueryRequest queryRequest = new QueryRequest(List.of("a51e85bb-6259-4488-8df2-f08cb43485f8"));
        
        var response = controller.getPhysicalInstances(queryRequest);
        assertNotNull(response);
        assertNotNull(response.results());
        assertFalse(response.results().isEmpty());
        
        // Verify first result has identifier and other expected fields
        var firstItem = response.results().getFirst();
        assertNotNull(firstItem.identifier());
        assertNotNull(firstItem.itemName());
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