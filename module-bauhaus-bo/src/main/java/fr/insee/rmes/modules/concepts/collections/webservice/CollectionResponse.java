package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.Lang;
import fr.insee.rmes.modules.concepts.collections.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;
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
            collection.partialCollection().id(),
                collection.partialCollection().prefLabel().label(),
                collection.secondLabel().map(LocalisedLabel::label).orElse(""),
                collection.created(),
                collection.modified().orElse(null),
                collection.descriptions().getOrDefault(Lang.defaultLanguage(), ""),
                collection.descriptions().getOrDefault(Lang.alternativeLanguage(), ""),
                collection.isValidated(),
                collection.creator().orElse(null),
                collection.contributor()
        );
    }
}
