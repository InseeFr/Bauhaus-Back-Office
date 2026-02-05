package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

public record PartialConcept(ConceptId id, LocalisedLabel defaultLabel, LocalisedLabel alternativeLabel) {
}
