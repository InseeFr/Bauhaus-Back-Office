package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.jspecify.annotations.Nullable;

public record CollectionDashboardItem(
        CollectionId id,
        String label,
        String created,
        @Nullable String modified,
        ValidationStatus validationState,
        @Nullable String creator,
        int nbMembers
) {
}
