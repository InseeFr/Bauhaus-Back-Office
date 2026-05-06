package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;

import java.util.regex.Pattern;

public record CollectionId(String value) {

    public static final String VALID_PATTERN = "^[A-Za-z0-9-]+$";
    private static final Pattern PATTERN = Pattern.compile(VALID_PATTERN);

    public CollectionId {
        if (value == null) {
            throw new InvalidCollectionIdException("The identifier is null");
        }
        if (value.isEmpty()) {
            throw new InvalidCollectionIdException("The identifier is empty");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidCollectionIdException("The identifier is invalid: only alphanumeric characters and hyphens are allowed");
        }
    }
}
