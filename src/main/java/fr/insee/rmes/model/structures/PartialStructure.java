package fr.insee.rmes.model.structures;
public record PartialStructure(
        String iri,
        String id,
        String labelLg1,
        String creator,
        String validationState
) {
}
