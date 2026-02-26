package fr.insee.rmes.modules.operations.msd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "fr.insee.rmes.bauhaus.documentation.geographie.baseURI=qualite/territoire",
        "fr.insee.rmes.bauhaus.documentation.titlePrefixLg1=Rapport qualité :",
        "fr.insee.rmes.bauhaus.documentation.titlePrefixLg2=Quality report:"
})
class DocumentationConfigurationTest {

    @Configuration
    @EnableConfigurationProperties(DocumentationConfiguration.class)
    static class TestConfig {}

    @Autowired
    private DocumentationConfiguration documentationConfiguration;


    @Test
    void shouldBindGeographieBaseUri() {
        assertEquals("qualite/territoire", documentationConfiguration.geographie().baseUri());
    }

    @Test
    void shouldBindTitlePrefixLg1() {
        assertEquals("Rapport qualité :", documentationConfiguration.titlePrefixLg1());
    }

    @Test
    void shouldBindTitlePrefixLg2() {
        assertEquals("Quality report:", documentationConfiguration.titlePrefixLg2());
    }
}
