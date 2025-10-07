package fr.insee.rmes.domain.model.ddi;

public record UpdatePhysicalInstanceRequest(
        String physicalInstanceLabel,
        String dataRelationshipName
) {
}