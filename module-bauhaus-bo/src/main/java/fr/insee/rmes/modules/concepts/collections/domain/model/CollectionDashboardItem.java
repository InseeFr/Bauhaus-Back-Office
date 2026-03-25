package fr.insee.rmes.modules.concepts.collections.domain.model;

import org.jspecify.annotations.Nullable;

public record CollectionDashboardItem(
        CollectionId id,
        String label,
        String created,
        @Nullable String modified,
        boolean isValidated,
        @Nullable String creator,
        int nbMembers
) {
}
