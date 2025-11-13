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
        "fr.insee.rmes.bauhaus.colectica.primary.baseUrl=http://localhost:8082",
        "fr.insee.rmes.bauhaus.colectica.primary.apiPath=/api/v1/",
        "fr.insee.rmes.bauhaus.colectica.primary.username=test-user",
        "fr.insee.rmes.bauhaus.colectica.primary.password=test-password",
        "fr.insee.rmes.bauhaus.colectica.secondary.baseUrl=http://localhost:8083",
        "fr.insee.rmes.bauhaus.colectica.secondary.apiPath=/api/v1/",
        "fr.insee.rmes.bauhaus.colectica.secondary.username=test-user-2",
        "fr.insee.rmes.bauhaus.colectica.secondary.password=test-password-2"
})
class ColecticaConfigurationTest {

    @Autowired
    private ColecticaConfiguration colecticaConfiguration;

    @Test
    void shouldLoadConfigurationPropertiesForPrimaryInstance() {
        assertTrue(colecticaConfiguration.mockServerEnabled());

        // Verify primary instance configuration
        assertEquals("http://localhost:8082", colecticaConfiguration.primary().baseUrl());
        assertEquals("/api/v1/", colecticaConfiguration.primary().apiPath());
        assertEquals("http://localhost:8082", colecticaConfiguration.primary().baseServerUrl());
        assertEquals("http://localhost:8082/api/v1/", colecticaConfiguration.primary().baseApiUrl());
        assertEquals("test-user", colecticaConfiguration.primary().username());
        assertEquals("test-password", colecticaConfiguration.primary().password());
    }

    @Test
    void shouldLoadConfigurationPropertiesForSecondaryInstance() {
        // Verify secondary instance configuration
        assertEquals("http://localhost:8083", colecticaConfiguration.secondary().baseUrl());
        assertEquals("/api/v1/", colecticaConfiguration.secondary().apiPath());
        assertEquals("http://localhost:8083", colecticaConfiguration.secondary().baseServerUrl());
        assertEquals("http://localhost:8083/api/v1/", colecticaConfiguration.secondary().baseApiUrl());
        assertEquals("test-user-2", colecticaConfiguration.secondary().username());
        assertEquals("test-password-2", colecticaConfiguration.secondary().password());
    }

    @EnableConfigurationProperties(ColecticaConfiguration.class)
    static class TestConfiguration {
    }
}