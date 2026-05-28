package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
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
                ValidationStatus.VALIDATED,
                "creator1",
                5
        );

        var response = CollectionDashboardItemResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c1000", response.id());
        assertEquals("Label FR", response.label());
        assertEquals("2024-01-01T10:00:00", response.created());
        assertEquals("2024-06-01T15:30:00", response.modified());
        assertEquals("Validated", response.validationState());
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
                ValidationStatus.UNPUBLISHED,
                null,
                0
        );

        var response = CollectionDashboardItemResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c2000", response.id());
        assertEquals("Label FR", response.label());
        assertEquals("2024-01-01T10:00:00", response.created());
        assertNull(response.modified());
        assertEquals("Unpublished", response.validationState());
        assertNull(response.creator());
        assertEquals(0, response.nbMembers());
    }
}