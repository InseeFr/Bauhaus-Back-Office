package fr.insee.rmes.modules.concepts.concept.domain.model;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.InvalidConceptIdException;

public record ConceptId(String value) {

    public static final String VALID_PATTERN = "^c[0-9]+$";

    public ConceptId {
        if (value == null) {
            throw new InvalidConceptIdException("The identifier is null");
        }
        if (value.isEmpty()) {
            throw new InvalidConceptIdException("The identifier is empty");
        }
    }
}
