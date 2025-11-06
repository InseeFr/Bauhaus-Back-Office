package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ColecticaConfigurationTest.TestConfiguration.class)
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.colectica.mock-server-enabled=true",
        "fr.insee.rmes.bauhaus.colectica.baseURI=http://localhost:8082/api/colectica"
})
class ColecticaConfigurationTest {

    @Autowired
    private ColecticaConfiguration colecticaConfiguration;

    @Test
    void shouldLoadConfigurationProperties() {
        assertTrue(colecticaConfiguration.mockServerEnabled());
        assertEquals("http://localhost:8082/api/colectica", colecticaConfiguration.baseURI());
    }

    @EnableConfigurationProperties(ColecticaConfiguration.class)
    static class TestConfiguration {
    }
}