package fr.insee.rmes.bauhaus_services.concepts.collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionExportPatternsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/xslTransformerFiles/collection/collectionFrPatternContent.xml",
            "/xslTransformerFiles/collection/collectionEnPatternContent.xml",
            "/xslTransformerFiles/collection/collectionOdsPatternContent.xml"
    })
    void conceptLinksShouldTargetTheConceptsRoute(String pattern) throws IOException {
        String content = readResource(pattern);

        assertTrue(content.contains("${rmesUrl}/concepts/${collectionConcept/id}"),
                pattern + " should link concepts to the /concepts/ front-end route");
        assertFalse(content.contains("${rmesUrl}/concept/${collectionConcept/id}"),
                pattern + " should not link concepts to the unknown /concept/ route");
    }

    private String readResource(String path) throws IOException {
        try (InputStream is = CollectionExportPatternsTest.class.getResourceAsStream(path)) {
            assertNotNull(is, "Resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
