package fr.insee.rmes.modules.structures.structures.domain.model;
public record PartialStructureComponent(
        String iri,
        String id,
        String identifiant,
        String labelLg1,
        String concept,
        String type,
        String codeList,
        String validationState,
        String creator,
        String range
) {
}
