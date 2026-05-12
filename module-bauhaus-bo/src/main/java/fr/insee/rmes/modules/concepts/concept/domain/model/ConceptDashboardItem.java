package fr.insee.rmes.modules.concepts.concept.domain.model;

import org.jspecify.annotations.Nullable;

public record ConceptDashboardItem(
        ConceptId id,
        String label,
        String created,
        @Nullable String modified,
        boolean isValidated,
        @Nullable String creator
) {
}
