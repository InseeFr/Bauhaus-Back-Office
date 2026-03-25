package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import org.jspecify.annotations.Nullable;

public record GraphDBCollectionDashboardItem(
        String id,
        String label,
        String created,
        @Nullable String modified,
        boolean isValidated,
        @Nullable String creator,
        String nbMembers
) {
    CollectionDashboardItem toDomain() {
        return new CollectionDashboardItem(
                new CollectionId(id),
                label,
                created,
                modified,
                isValidated,
                creator,
                Integer.parseInt(nbMembers)
        );
    }
}
