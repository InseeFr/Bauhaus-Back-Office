package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphDBConceptTest {

    @Test
    void should_convert_to_domain_with_all_fields() {
        var concept = new GraphDBConcept("c00001", "Label FR", "Label EN");

        CollectionMember domain = concept.toDomain();

        assertNotNull(domain);
        assertEquals("c00001", domain.id());
        assertEquals("Label FR", domain.prefLabelLg1());
        assertEquals("Label EN", domain.prefLabelLg2());
    }

    @Test
    void should_convert_to_domain_without_second_label() {
        var concept = new GraphDBConcept("c00001", "Label FR", null);

        CollectionMember domain = concept.toDomain();

        assertNotNull(domain);
        assertEquals("c00001", domain.id());
        assertEquals("Label FR", domain.prefLabelLg1());
        assertNull(domain.prefLabelLg2());
    }
}
