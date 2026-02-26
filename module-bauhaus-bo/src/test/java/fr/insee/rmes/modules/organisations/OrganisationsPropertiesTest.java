package fr.insee.rmes.modules.organisations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {OrganisationsPropertiesTest.TestConfig.class})
@TestPropertySource(properties = {
        "fr.insee.rmes.bauhaus.organisations.graph=organisations"
})
class OrganisationsPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(OrganisationsProperties.class)
    static class TestConfig {}

    @Autowired
    private OrganisationsProperties organisationsProperties;

    @Test
    void shouldBindGraph() {
        assertEquals("organisations", organisationsProperties.graph());
    }
}
