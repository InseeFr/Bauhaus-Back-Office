package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import org.jspecify.annotations.Nullable;

public record GraphDBConceptDashboardItem(
        String id,
        String label,
        String created,
        @Nullable String modified,
        boolean isValidated,
        @Nullable String creator
) {

    ConceptDashboardItem toDomain() {
        return new ConceptDashboardItem(
                new ConceptId(id),
                label,
                created,
                modified,
                isValidated,
                creator
        );
    }
}
