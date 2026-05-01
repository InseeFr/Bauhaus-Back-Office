package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;

public record CollectionId(String value) {

    public CollectionId {
        if (value == null) {
            throw new InvalidCollectionIdException("The identifier is null");
        }
        if (value.isEmpty()) {
            throw new InvalidCollectionIdException("The identifier is empty");
        }
    }
}
