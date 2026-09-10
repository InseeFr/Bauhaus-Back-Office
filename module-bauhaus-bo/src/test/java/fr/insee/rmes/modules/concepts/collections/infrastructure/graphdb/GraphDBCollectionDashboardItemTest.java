package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.junit.jupiter.api.Test;

class GraphDBCollectionDashboardItemTest {

    @Test
    void should_convert_to_domain_with_all_fields() {
        var graphDBItem = new GraphDBCollectionDashboardItem(
                "c1000", "Label FR", "2024-01-01T10:00:00", "2024-06-01T15:30:00", "Validated", "creator1", "5");

        CollectionDashboardItem domain = graphDBItem.toDomain();

        assertNotNull(domain);
        assertEquals("c1000", domain.id().value());
        assertEquals("Label FR", domain.label());
        assertEquals("2024-01-01T10:00:00", domain.created());
        assertEquals("2024-06-01T15:30:00", domain.modified());
        assertEquals(ValidationStatus.VALIDATED, domain.validationState());
        assertEquals("creator1", domain.creator());
        assertEquals(5, domain.nbMembers());
    }

    @Test
    void should_convert_to_domain_with_null_optional_fields() {
        var graphDBItem = new GraphDBCollectionDashboardItem(
                "c2000", "Label FR", "2024-01-01T10:00:00", null, "Unpublished", null, "0");

        CollectionDashboardItem domain = graphDBItem.toDomain();

        assertNotNull(domain);
        assertEquals("c2000", domain.id().value());
        assertEquals("Label FR", domain.label());
        assertEquals("2024-01-01T10:00:00", domain.created());
        assertNull(domain.modified());
        assertEquals(ValidationStatus.UNPUBLISHED, domain.validationState());
        assertNull(domain.creator());
        assertEquals(0, domain.nbMembers());
    }
}
