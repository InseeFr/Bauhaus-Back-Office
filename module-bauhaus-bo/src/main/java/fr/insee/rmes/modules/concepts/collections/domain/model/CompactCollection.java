package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

public class CompactCollection {

    private final CollectionId id;
    private final LocalisedLabel prefLabel;

    public CompactCollection(CollectionId id, LocalisedLabel prefLabel) {
        this.id = id;
        this.prefLabel = prefLabel;
    }

    public CollectionId id() {
        return id;
    }

    public LocalisedLabel prefLabel() {
        return prefLabel;
    }
}
