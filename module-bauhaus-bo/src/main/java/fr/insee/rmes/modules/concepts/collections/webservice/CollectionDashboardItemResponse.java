package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import org.jspecify.annotations.Nullable;

public record CollectionDashboardItemResponse(
        String id,
        String label,
        String created,
        @Nullable String modified,
        boolean isValidated,
        @Nullable String creator,
        int nbMembers
) {
    static CollectionDashboardItemResponse fromDomain(CollectionDashboardItem item) {
        return new CollectionDashboardItemResponse(
                item.id().value().toString(),
                item.label(),
                item.created(),
                item.modified(),
                item.isValidated(),
                item.creator(),
                item.nbMembers()
        );
    }
}
