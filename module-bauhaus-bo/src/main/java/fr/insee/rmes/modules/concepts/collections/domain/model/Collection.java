package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.MalformedCollectionException;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class Collection extends CompactCollection {

    private static final ValidationStatus DEFAULT_VALIDATION_STATE = ValidationStatus.UNPUBLISHED;
    private final @Nullable List<LocalisedLabel> alternativeLabels;
    private final String creator;
    private final @Nullable String contributor;
    private final List<LocalisedLabel> descriptions;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final ValidationStatus validationState;
    private final List<ConceptId> conceptIds;

    public Collection(
            CollectionId id,
            List<LocalisedLabel> labels,
            String creator,
            @Nullable String contributor,
            List<LocalisedLabel> descriptions,
            LocalDateTime created,
            @Nullable LocalDateTime modified,
            ValidationStatus validationState,
            List<ConceptId> conceptIds) {
        var prefLabel = labels.stream()
                .filter(l -> l.lang().equals(Lang.defaultLanguage()))
                .findFirst();
        var alternativeLabels = labels.stream()
                .filter(l -> !l.lang().equals(Lang.defaultLanguage()))
                .toList();

        super(
                id,
                prefLabel.orElseThrow(
                        () -> new MalformedCollectionException("There is not label for default language")));
        this.alternativeLabels = alternativeLabels;
        this.creator = creator;
        this.contributor = contributor;
        this.descriptions = descriptions;
        this.created = created;
        this.modified = modified;
        this.validationState = validationState;
        this.conceptIds = conceptIds;
    }

    public static Collection create(CreateCollectionCommand createCollection, CollectionId collectionId) {
        return create(createCollection, collectionId, DEFAULT_VALIDATION_STATE);
    }

    public static Collection create(
            CreateCollectionCommand createCollection, CollectionId collectionId, ValidationStatus validationState) {
        return new Collection(
                collectionId,
                createCollection.labels(),
                createCollection.creator(),
                createCollection.contributor().orElse(null),
                createCollection.descriptions(),
                LocalDateTime.now(),
                null,
                validationState,
                createCollection.conceptsIdendifiers().stream()
                        .map(ConceptId::new)
                        .toList());
    }

    public List<LocalisedLabel> alternativeLabels() {
        return alternativeLabels;
    }

    public String creator() {
        return creator;
    }

    public Optional<String> contributor() {
        return Optional.ofNullable(contributor);
    }

    public List<LocalisedLabel> descriptions() {
        return descriptions;
    }

    public LocalDateTime created() {
        return created;
    }

    public Optional<LocalDateTime> modified() {
        return Optional.ofNullable(modified);
    }

    public ValidationStatus validationState() {
        return validationState;
    }

    public List<ConceptId> conceptIds() {
        return conceptIds;
    }
}
