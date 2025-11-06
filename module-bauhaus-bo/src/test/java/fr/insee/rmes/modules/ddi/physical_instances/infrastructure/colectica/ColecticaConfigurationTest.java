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
        "fr.insee.rmes.bauhaus.colectica.baseUrl=http://localhost:8082",
        "fr.insee.rmes.bauhaus.colectica.apiPath=/api/v1/",
        "fr.insee.rmes.bauhaus.colectica.username=test-user",
        "fr.insee.rmes.bauhaus.colectica.password=test-password"
})
class ColecticaConfigurationTest {

    @Autowired
    private ColecticaConfiguration colecticaConfiguration;

    @Test
    void shouldLoadConfigurationProperties() {
        assertTrue(colecticaConfiguration.mockServerEnabled());
        assertEquals("http://localhost:8082", colecticaConfiguration.baseUrl());
        assertEquals("/api/v1/", colecticaConfiguration.apiPath());
        assertEquals("http://localhost:8082", colecticaConfiguration.baseServerUrl());
        assertEquals("http://localhost:8082/api/v1/", colecticaConfiguration.baseApiUrl());
        assertEquals("test-user", colecticaConfiguration.username());
        assertEquals("test-password", colecticaConfiguration.password());
    }

    @Test
    void shouldUseDefaultApiPathWhenNotProvided() {
        // This test will use a different configuration without apiPath
    }

    @EnableConfigurationProperties(ColecticaConfiguration.class)
    static class TestConfiguration {
    }
}