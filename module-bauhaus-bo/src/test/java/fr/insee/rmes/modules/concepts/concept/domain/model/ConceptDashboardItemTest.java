package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptDashboardItemTest {

    @Test
    void exposes_all_dashboard_fields() {
        var item = new ConceptDashboardItem(
                new ConceptId("c00001"),
                "Mon concept",
                "2026-01-01T10:00:00",
                "2026-01-02T11:00:00",
                ValidationStatus.VALIDATED,
                "HIE000000"
        );

        assertThat(item.id()).isEqualTo(new ConceptId("c00001"));
        assertThat(item.label()).isEqualTo("Mon concept");
        assertThat(item.created()).isEqualTo("2026-01-01T10:00:00");
        assertThat(item.modified()).isEqualTo("2026-01-02T11:00:00");
        assertThat(item.validationState()).isEqualTo(ValidationStatus.VALIDATED);
        assertThat(item.creator()).isEqualTo("HIE000000");
    }

    @Test
    void modified_and_creator_may_be_null_for_unsaved_or_anonymous_records() {
        var item = new ConceptDashboardItem(
                new ConceptId("c00001"),
                "Mon concept",
                "2026-01-01T10:00:00",
                null,
                ValidationStatus.UNPUBLISHED,
                null
        );

        assertThat(item.modified()).isNull();
        assertThat(item.creator()).isNull();
    }
}
