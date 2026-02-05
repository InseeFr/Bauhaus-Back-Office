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
        "fr.insee.rmes.bauhaus.colectica.server.baseUrl=http://localhost:8082",
        "fr.insee.rmes.bauhaus.colectica.server.apiPath=/api/v1/",
        "fr.insee.rmes.bauhaus.colectica.server.username=test-user",
        "fr.insee.rmes.bauhaus.colectica.server.password=test-password",
})
class ColecticaConfigurationTest {

    @Autowired
    private ColecticaConfiguration colecticaConfiguration;

    @Test
    void shouldLoadConfigurationPropertiesForPrimaryInstance() {

        // Verify primary instance configuration
        assertEquals("http://localhost:8082", colecticaConfiguration.server().baseUrl());
        assertEquals("/api/v1/", colecticaConfiguration.server().apiPath());
        assertEquals("http://localhost:8082", colecticaConfiguration.server().baseServerUrl());
        assertEquals("http://localhost:8082/api/v1/", colecticaConfiguration.server().baseApiUrl());
        assertEquals("test-user", colecticaConfiguration.server().username());
        assertEquals("test-password", colecticaConfiguration.server().password());
    }

    @EnableConfigurationProperties(ColecticaConfiguration.class)
    static class TestConfiguration {
    }
}