package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public record GraphDBCollection(
        String id,
        String prefLabelLg1,
        String prefLabelLg1_lg,
        @Nullable String prefLabelLg2,
        @Nullable String prefLabelLg2_lg,
        String created,
        @Nullable String modified,

        @Nullable String descriptionLg1,
        @Nullable String descriptionLg1_lg,

        @Nullable String descriptionLg2,
        @Nullable String descriptionLg2_lg,
        boolean isValidated,
        String creator,
        String contributor,
        List<String> conceptIds
) {

    private static Optional<LocalisedLabel> getDescriptionByIndex(List<LocalisedLabel> localisedLabels, int i) {
        if (localisedLabels.size() <= i) {
            return Optional.empty();
        }
        return Optional.of(localisedLabels.get(i));
    }

    static GraphDBCollection fromDomain(Collection collection) {

        var firstDescription = getDescriptionByIndex(collection.descriptions(), 0);
        var secondDescription = getDescriptionByIndex(collection.descriptions(), 1);

        var firstAlternativeLabel = getFirstAlternativeLabel(collection);

        return new GraphDBCollection(
                collection.id().value().toString(),
                collection.prefLabel().value(),
                collection.prefLabel().lang().toString(),

                firstAlternativeLabel.map(LocalisedLabel::value).orElse(null),
                firstAlternativeLabel.map(LocalisedLabel::lang).map(Lang::toString).orElse(null),

                collection.created().toString(),
                collection.modified().map(LocalDateTime::toString).orElse(null),

                //descriptionLg1
                firstDescription.map(LocalisedLabel::value).orElse(null),

                //descriptionLg1_lg
                firstDescription.map(LocalisedLabel::lang).map(Lang::toString).orElse(null),

                //descriptionLg2
                secondDescription.map(LocalisedLabel::value).orElse(null),

                //descriptionLg2_lg
                secondDescription.map(LocalisedLabel::lang).map(Lang::toString).orElse(null),



                collection.isValidated(),
                collection.creator(),
                collection.contributor().orElse(null),
                collection.conceptIds().stream().map(ConceptId::value).toList()
        );
    }

    private static Optional<LocalisedLabel> getFirstAlternativeLabel(Collection collection) {
        if(collection.alternativeLabels().isEmpty()){
            return Optional.empty();
        }

        return Optional.of(collection.alternativeLabels().getFirst());
    }


    Collection toDomain() {
        return new Collection(
                new CollectionId(id),
                generateLabels(),
                creator,
                contributor,
                toLocalisedDescriptions(),
                parseDateTime(created),
                Objects.isNull(modified) ? null : parseDateTime(modified),
                isValidated,
                conceptIds.stream()
                        .map(ConceptId::new)
                        .toList());
    }

    private static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(dateTimeStr).toLocalDateTime();
        }
    }

    private List<LocalisedLabel> generateLabels() {
        var list = new ArrayList<LocalisedLabel>();
        if (Objects.nonNull(prefLabelLg1())) {
            list.add(new LocalisedLabel(prefLabelLg1, Lang.valueOf(prefLabelLg1_lg.toUpperCase())));
        }
        if (Objects.nonNull(prefLabelLg2())) {
            list.add(new LocalisedLabel(prefLabelLg2, Lang.valueOf(prefLabelLg2_lg.toUpperCase())));
        }
        return list;

    }


    private List<LocalisedLabel> toLocalisedDescriptions() {
        var list = new ArrayList<LocalisedLabel>();
        if (Objects.nonNull(descriptionLg1())) {
            list.add(new LocalisedLabel(descriptionLg1, Lang.valueOf(descriptionLg1_lg.toUpperCase())));
        }
        if (Objects.nonNull(descriptionLg2())) {
            list.add(new LocalisedLabel(descriptionLg2, Lang.valueOf(descriptionLg2_lg.toUpperCase())));
        }
        return list;
    }

    public GraphDBCollection withConcepts(GraphDBConcept[] graphDBConcepts) {
        return new GraphDBCollection(id,
                prefLabelLg1,
                prefLabelLg1_lg,
                prefLabelLg2,
                prefLabelLg2_lg,
                created,
                modified,
                descriptionLg1,
                descriptionLg1_lg,
                descriptionLg2,
                descriptionLg2_lg,
                isValidated,
                creator,
                contributor,
                Arrays.stream(graphDBConcepts).map(GraphDBConcept::id).toList()
        );
    }
}
