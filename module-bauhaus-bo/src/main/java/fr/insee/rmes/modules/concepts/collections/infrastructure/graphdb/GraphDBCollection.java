package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record GraphDBCollection(
        String id,
        String prefLabelLg1,
        @Nullable String prefLabelLg2,
        String created,
        @Nullable String modified,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        boolean isValidated,
        String creator,
        String contributor
) {
    Collection toDomain(){
        var partialCollection = new PartialCollection(
                new CollectionId(id),
                new LocalisedLabel(prefLabelLg1, Lang.defaultLanguage())
        );

        return new Collection(
                partialCollection,
                Objects.isNull(prefLabelLg2) ? null : new LocalisedLabel(prefLabelLg2, Lang.alternativeLanguage()),
                creator,
                contributor,
                toLocalisedDescriptions(),
                LocalDateTime.parse(created),
                Objects.isNull(modified) ? null : LocalDateTime.parse(modified),
                isValidated
        );
    }

    private Map<Lang, String> toLocalisedDescriptions() {
        var list = new HashMap<Lang, String>();
        if(Objects.nonNull(descriptionLg1())){
            list.put(Lang.defaultLanguage(), descriptionLg1);
        }
        if(Objects.nonNull(descriptionLg2())){
            list.put(Lang.alternativeLanguage(), descriptionLg2);
        }
        return list;
    }
}
