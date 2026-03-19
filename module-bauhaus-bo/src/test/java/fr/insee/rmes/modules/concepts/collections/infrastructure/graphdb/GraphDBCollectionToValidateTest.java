package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphDBCollectionToValidateTest {

    @Test
    void should_convert_to_domain() {
        var graphDBItem = new GraphDBCollectionToValidate("c1000", "Label FR", "creator1");

        CollectionToValidate domain = graphDBItem.toDomain();

        assertNotNull(domain);
        assertEquals("c1000", domain.id().value());
        assertEquals("Label FR", domain.label());
        assertEquals("creator1", domain.creator());
    }
}