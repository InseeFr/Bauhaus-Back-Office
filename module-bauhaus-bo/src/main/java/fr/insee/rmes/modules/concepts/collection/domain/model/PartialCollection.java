package fr.insee.rmes.modules.concepts.collection.domain.model;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;

public record PartialCollection(CollectionId id, LocalisedLabel prefLabel) {
}
