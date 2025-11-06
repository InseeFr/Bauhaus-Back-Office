package fr.insee.rmes.modules.concepts.collections.domain.model;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Collection {

    private final PartialCollection partialCollection;
    private final @Nullable  LocalisedLabel secondLabel;
    private final @Nullable String creator;
    private final String contributor;
    private final Map<Lang, String> descriptions;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final boolean isValidated;

    public Collection(PartialCollection partialCollection, @Nullable LocalisedLabel secondLabel, @Nullable String creator, String contributor, Map<Lang, String> descriptions, LocalDateTime created, @Nullable LocalDateTime modified, boolean isValidated) {
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

    public Optional<String> creator() {
        return Optional.ofNullable(creator);
    }

    public String contributor() {
        return contributor;
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
