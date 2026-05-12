package fr.insee.rmes.modules.concepts.concept.webservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptDashboardItem;
import org.jspecify.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConceptDashboardItemResponse(
        String id,
        String label,
        String created,
        @Nullable String modified,
        boolean isValidated,
        @Nullable String creator
) {

    public static ConceptDashboardItemResponse fromDomain(ConceptDashboardItem item) {
        return new ConceptDashboardItemResponse(
                item.id().value(),
                item.label(),
                item.created(),
                item.modified(),
                item.isValidated(),
                item.creator()
        );
    }
}
