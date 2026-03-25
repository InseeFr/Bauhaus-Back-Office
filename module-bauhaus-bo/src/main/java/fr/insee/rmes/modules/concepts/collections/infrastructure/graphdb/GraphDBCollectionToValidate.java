package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;

public record GraphDBCollectionToValidate(String id, String label, String creator) {
    CollectionToValidate toDomain() {
        return new CollectionToValidate(new CollectionId(id), label, creator);
    }
}
