package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDBConceptDashboardItemTest {

    @Test
    void toDomain_maps_all_fields() {
        var row = new GraphDBConceptDashboardItem(
                "c00001",
                "Concept",
                "2026-01-01T10:00:00",
                "2026-01-02T11:00:00",
                "Validated",
                "HIE000000"
        );

        var domain = row.toDomain();

        assertThat(domain.id().value()).isEqualTo("c00001");
        assertThat(domain.label()).isEqualTo("Concept");
        assertThat(domain.created()).isEqualTo("2026-01-01T10:00:00");
        assertThat(domain.modified()).isEqualTo("2026-01-02T11:00:00");
        assertThat(domain.validationState()).isEqualTo(ValidationStatus.VALIDATED);
        assertThat(domain.creator()).isEqualTo("HIE000000");
    }

    @Test
    void toDomain_propagates_null_modified_and_creator() {
        var row = new GraphDBConceptDashboardItem(
                "c00001",
                "Concept",
                "2026-01-01T10:00:00",
                null,
                "Unpublished",
                null
        );

        var domain = row.toDomain();

        assertThat(domain.modified()).isNull();
        assertThat(domain.creator()).isNull();
        assertThat(domain.validationState()).isEqualTo(ValidationStatus.UNPUBLISHED);
    }
}
