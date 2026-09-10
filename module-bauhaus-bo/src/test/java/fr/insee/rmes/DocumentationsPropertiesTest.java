package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DocumentationsProperties.class)
@TestPropertySource(
        properties = {
            "fr.insee.rmes.bauhaus.documentation.titlePrefixLg1=Rapport qualité :",
            "fr.insee.rmes.bauhaus.documentation.titlePrefixLg2=Quality report:",
            "fr.insee.rmes.bauhaus.concepts.scheme=concepts/definitions/scheme"
        })
class DocumentationsPropertiesTest {

    @Autowired
    private DocumentationsProperties documentations;

    @Test
    void shouldBindTitlePrefixes() {
        assertEquals("Rapport qualité :", documentations.titlePrefixLg1());
        assertEquals("Quality report:", documentations.titlePrefixLg2());
    }

    @Test
    void shouldBindConceptsScheme() {
        assertEquals("concepts/definitions/scheme", documentations.conceptsScheme());
    }
}
