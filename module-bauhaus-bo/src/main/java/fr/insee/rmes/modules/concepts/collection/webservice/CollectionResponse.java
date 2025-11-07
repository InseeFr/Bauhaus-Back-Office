package fr.insee.rmes.modules.concepts.collection.webservice;

import fr.insee.rmes.modules.concepts.collection.domain.model.Collection;
import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;

import java.time.LocalDateTime;

public record CollectionResponse(
        String id,
        String prefLabelLg1,
        String prefLabelLg2,
        LocalDateTime created,
        LocalDateTime modified,
        String descriptionLg1,
        String descriptionLg2,
        boolean isValidated,
        String creator,
        String contributor
) {
    static CollectionResponse fromDomain(Collection collection){
        return new CollectionResponse(
            collection.partialCollection().id().value().toString(),
                collection.partialCollection().prefLabel().value(),
                collection.secondLabel().map(LocalisedLabel::value).orElse(""),
                collection.created(),
                collection.modified().orElse(null),
                collection.descriptions().getOrDefault(Lang.defaultLanguage(), ""),
                collection.descriptions().getOrDefault(Lang.alternativeLanguage(), ""),
                collection.isValidated(),
                collection.creator(),
                collection.contributor().orElse(null)
        );
    }
}
