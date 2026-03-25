package fr.insee.rmes.modules.concepts.collections.domain.model;

public record CollectionToValidate(
        CollectionId id,
        String label,
        String creator
) {
}
