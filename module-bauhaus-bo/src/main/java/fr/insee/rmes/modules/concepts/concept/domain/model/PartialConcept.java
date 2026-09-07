package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.jspecify.annotations.Nullable;

public record PartialConcept(ConceptId id, LocalisedLabel defaultLabel, @Nullable LocalisedLabel alternativeLabel) {
}
