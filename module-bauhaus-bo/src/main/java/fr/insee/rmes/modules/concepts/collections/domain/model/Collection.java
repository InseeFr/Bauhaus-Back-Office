package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Collection {

    private static final boolean DEFAULT_VALIDATION_STATE = false;
    //TODO Utiliser un héritage
    private final PartialCollection partialCollection;
    private final @Nullable LocalisedLabel secondLabel;
    private final String creator;
    private final @Nullable String contributor;
    //TODO revoir la modélisation des descriptions (faire analogue à preflabel ?)
    private final Map<Lang, String> descriptions;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final boolean isValidated;
    private final List<ConceptId> conceptIds;

    public Collection(PartialCollection partialCollection,
                      @Nullable LocalisedLabel secondLabel, String creator,
                      @Nullable String contributor, Map<Lang, String> descriptions,
                      LocalDateTime created, @Nullable LocalDateTime modified,
                      boolean isValidated, List<ConceptId> conceptIds) {
        this.partialCollection = partialCollection;
        this.secondLabel = secondLabel;
        this.creator = creator;
        this.contributor = contributor;
        this.descriptions = descriptions;
        this.created = created;
        this.modified = modified;
        this.isValidated = isValidated;
        this.conceptIds = conceptIds;
    }

    public static Collection create(CreateCollectionCommand createCollection, CollectionId collectionId) {
        return new Collection(new PartialCollection(collectionId, createCollection.defaultLabel()),
                createCollection.alternativeLabel().orElse(null),
                createCollection.creator(),
                createCollection.contributor().orElse(null),
                createCollection.descriptions(),
                LocalDateTime.now(),
                null,
                DEFAULT_VALIDATION_STATE,
                createCollection.conceptsIdendifiers().stream()
                        .map(ConceptId::new)
                        .toList()
        );
    }

    public PartialCollection partialCollection() {
        return partialCollection;
    }

    public Optional<LocalisedLabel> secondLabel() {
        return Optional.ofNullable(secondLabel);
    }

    public String creator() {
        return creator;
    }

    public Optional<String> contributor() {
        return Optional.ofNullable(contributor);
    }

    public Map<Lang, String> descriptions() {
        return descriptions;
    }

    public LocalDateTime created() {
        return created;
    }

    public Optional<LocalDateTime> modified() {
        return Optional.ofNullable(modified);
    }

    public boolean isValidated() {
        return isValidated;
    }

    public List<ConceptId> conceptIds() {
        return conceptIds;
    }
}
