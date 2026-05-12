package fr.insee.rmes.modules.concepts.concept.domain.model;

public record ConceptVersion(int value) {

    public ConceptVersion {
        if (value < 1) {
            throw new IllegalArgumentException("concept version must be >= 1 (was " + value + ")");
        }
    }

    public static ConceptVersion initial() {
        return new ConceptVersion(1);
    }

    public ConceptVersion next() {
        return new ConceptVersion(value + 1);
    }
}
