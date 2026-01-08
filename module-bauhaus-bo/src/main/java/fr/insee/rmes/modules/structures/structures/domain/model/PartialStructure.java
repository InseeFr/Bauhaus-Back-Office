package fr.insee.rmes.modules.structures.structures.domain.model;
public record PartialStructure(
        String iri,
        String id,
        String labelLg1,
        String creator,
        String validationState
) {
}
