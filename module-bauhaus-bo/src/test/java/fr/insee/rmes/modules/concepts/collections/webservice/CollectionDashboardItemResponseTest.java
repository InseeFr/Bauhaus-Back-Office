package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionDashboardItemResponseTest {

    @Test
    void should_convert_from_domain_with_all_fields() {
        var domain = new CollectionDashboardItem(
                new CollectionId("c1000"),
                "Label FR",
                "2024-01-01T10:00:00",
                "2024-06-01T15:30:00",
                true,
                "creator1",
                5
        );

        var response = CollectionDashboardItemResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c1000", response.id());
        assertEquals("Label FR", response.label());
        assertEquals("2024-01-01T10:00:00", response.created());
        assertEquals("2024-06-01T15:30:00", response.modified());
        assertTrue(response.isValidated());
        assertEquals("creator1", response.creator());
        assertEquals(5, response.nbMembers());
    }

    @Test
    void should_convert_from_domain_with_null_optional_fields() {
        var domain = new CollectionDashboardItem(
                new CollectionId("c2000"),
                "Label FR",
                "2024-01-01T10:00:00",
                null,
                false,
                null,
                0
        );

        var response = CollectionDashboardItemResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c2000", response.id());
        assertEquals("Label FR", response.label());
        assertEquals("2024-01-01T10:00:00", response.created());
        assertNull(response.modified());
        assertFalse(response.isValidated());
        assertNull(response.creator());
        assertEquals(0, response.nbMembers());
    }
}