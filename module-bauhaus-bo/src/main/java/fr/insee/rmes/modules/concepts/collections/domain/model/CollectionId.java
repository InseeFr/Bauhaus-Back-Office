package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;

public record CollectionId(String value) {

    // Pattern check lives at the HTTP boundary (CreateCollectionRequest) so that legacy
    // identifiers stored in GraphDB before the rule existed can still be read and updated.
    public static final String VALID_PATTERN = "^[A-Za-z0-9-]+$";

    public CollectionId {
        if (value == null) {
            throw new InvalidCollectionIdException("The identifier is null");
        }
        if (value.isEmpty()) {
            throw new InvalidCollectionIdException("The identifier is empty");
        }
    }
}
