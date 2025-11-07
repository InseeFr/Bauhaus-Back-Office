package fr.insee.rmes.modules.concepts.collections.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class Collection {

    private final PartialCollection partialCollection;
    private final @Nullable  LocalisedLabel secondLabel;
    private final String creator;
    private final @Nullable String contributor;
    private final Map<Lang, String> descriptions;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final boolean isValidated;

    public Collection(PartialCollection partialCollection, @Nullable LocalisedLabel secondLabel,  String creator, @Nullable String contributor, Map<Lang, String> descriptions, LocalDateTime created, @Nullable LocalDateTime modified, boolean isValidated) {
        this.partialCollection = partialCollection;
        this.secondLabel = secondLabel;
        this.creator = creator;
        this.contributor = contributor;
        this.descriptions = descriptions;
        this.created = created;
        this.modified = modified;
        this.isValidated = isValidated;
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
}
