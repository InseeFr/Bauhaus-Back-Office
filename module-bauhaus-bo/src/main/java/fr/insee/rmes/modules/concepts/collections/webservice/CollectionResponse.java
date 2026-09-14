package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CollectionResponse(
        String id,
        List<LocalisedLabelResponse> labels,
        LocalDateTime created,
        LocalDateTime modified,
        List<LocalisedLabelResponse> descriptions,
        String validationState,
        String creator,
        String contributor) {
    static CollectionResponse fromDomain(Collection collection) {
        var labels = new ArrayList<LocalisedLabel>();
        labels.add(collection.prefLabel());
        labels.addAll(collection.alternativeLabels());

        return new CollectionResponse(
                collection.id().value().toString(),
                labels.stream().map(LocalisedLabelResponse::fromDomain).toList(),
                collection.created(),
                collection.modified().orElse(null),
                collection.descriptions().stream()
                        .map(LocalisedLabelResponse::fromDomain)
                        .toList(),
                collection.validationState().getValue(),
                collection.creator(),
                collection.contributor().orElse(null));
    }
}
