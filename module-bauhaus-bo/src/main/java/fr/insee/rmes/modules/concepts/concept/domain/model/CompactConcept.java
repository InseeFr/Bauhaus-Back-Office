package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

public class CompactConcept {

    private final ConceptId id;
    private final LocalisedLabel prefLabel;

    public CompactConcept(ConceptId id, LocalisedLabel prefLabel) {
        this.id = id;
        this.prefLabel = prefLabel;
    }

    public ConceptId id() {
        return id;
    }

    public LocalisedLabel prefLabel() {
        return prefLabel;
    }
}
