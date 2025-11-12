package fr.insee.rmes.modules.concepts.collection.infrastructure.graphdb;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collection.domain.model.*;
import fr.insee.rmes.modules.concepts.collection.domain.model.Collection;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;

public record GraphDBCollection(
        String id,
        String lg1,
        String lg2,
        String prefLabelLg1,
        @Nullable String prefLabelLg2,
        String created,
        @Nullable String modified,
        @Nullable String descriptionLg1,
        @Nullable String descriptionLg2,
        boolean isValidated,
        String creator,
        String contributor,
        List<String> conceptIds
) {

    static GraphDBCollection fromDomain(Collection collection) {
        return new GraphDBCollection(
                collection.partialCollection().id().value().toString(),
                collection.partialCollection().prefLabel().lang().toString(),
                collection.secondLabel().map(LocalisedLabel::lang).map(Lang::toString).orElse(null),
                collection.partialCollection().prefLabel().value(),
                collection.secondLabel().map(LocalisedLabel::value).orElse(null),
                collection.created().toString(),
                collection.modified().map(LocalDateTime::toString).orElse(null),
                collection.descriptions().get(collection.partialCollection().prefLabel().lang()),
                collection.secondLabel().map(LocalisedLabel::lang).map(collection.descriptions()::get).orElse(null),
                collection.isValidated(),
                collection.creator(),
                collection.contributor().orElse(null),
                collection.conceptIds().stream().map(ConceptId::value).toList()
        );
    }

    Collection toDomain() {
        var partialCollection = new PartialCollection(
                new CollectionId(id),
                new LocalisedLabel(prefLabelLg1, lg1 == null ? Lang.defaultLanguage() : Lang.valueOf(lg1.toUpperCase()))
        );

        return new Collection(
                partialCollection,
                Objects.isNull(prefLabelLg2) ? null : new LocalisedLabel(prefLabelLg2, lg2 == null ? Lang.defaultLanguage() : Lang.valueOf(lg2.toUpperCase())),
                creator,
                contributor,
                toLocalisedDescriptions(),
                LocalDateTime.parse(created),
                Objects.isNull(modified) ? null : LocalDateTime.parse(modified),
                isValidated,
                conceptIds.stream()
                        .map(ConceptId::new)
                        .toList());
    }


    private Map<Lang, String> toLocalisedDescriptions() {
        var list = new HashMap<Lang, String>();
        if (Objects.nonNull(descriptionLg1())) {
            list.put(Lang.defaultLanguage(), descriptionLg1);
        }
        if (Objects.nonNull(descriptionLg2())) {
            list.put(Lang.alternativeLanguage(), descriptionLg2);
        }
        return list;
    }

    public GraphDBCollection withConcepts(GraphDBConcept[] graphDBConcepts) {
        return new GraphDBCollection(id,
                lg1,
                lg2,
                prefLabelLg1,
                prefLabelLg2,
                created,
                modified,
                descriptionLg1,
                descriptionLg2,
                isValidated,
                creator,
                contributor,
                Arrays.stream(graphDBConcepts).map(GraphDBConcept::id).toList()
        );
    }
}
