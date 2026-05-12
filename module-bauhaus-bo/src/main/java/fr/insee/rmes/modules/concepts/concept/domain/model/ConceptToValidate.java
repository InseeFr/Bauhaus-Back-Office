package fr.insee.rmes.modules.concepts.concept.domain.model;

public record ConceptToValidate(
        ConceptId id,
        String label,
        String creator
) {
}
