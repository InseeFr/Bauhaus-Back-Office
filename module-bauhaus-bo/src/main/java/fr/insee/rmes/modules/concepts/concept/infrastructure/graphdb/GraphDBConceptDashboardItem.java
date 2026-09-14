package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.jspecify.annotations.Nullable;

public record GraphDBConceptDashboardItem(
        String id,
        String label,
        String created,
        @Nullable String modified,
        @Nullable String validationState,
        @Nullable String creator) {

    ConceptDashboardItem toDomain() {
        return new ConceptDashboardItem(
                new ConceptId(id), label, created, modified, ValidationStatus.fromValue(validationState), creator);
    }
}
