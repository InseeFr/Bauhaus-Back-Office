package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import org.jspecify.annotations.Nullable;

public record CollectionDashboardItemResponse(
        String id,
        String label,
        String created,
        @Nullable String modified,
        String validationState,
        @Nullable String creator,
        int nbMembers
) {
    static CollectionDashboardItemResponse fromDomain(CollectionDashboardItem item) {
        return new CollectionDashboardItemResponse(
                item.id().value().toString(),
                item.label(),
                item.created(),
                item.modified(),
                item.validationState().getValue(),
                item.creator(),
                item.nbMembers()
        );
    }
}
