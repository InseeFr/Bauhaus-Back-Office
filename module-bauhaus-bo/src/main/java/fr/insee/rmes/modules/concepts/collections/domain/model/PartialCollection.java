package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;

public record PartialCollection(CollectionId id, LocalisedLabel prefLabel) {
}
