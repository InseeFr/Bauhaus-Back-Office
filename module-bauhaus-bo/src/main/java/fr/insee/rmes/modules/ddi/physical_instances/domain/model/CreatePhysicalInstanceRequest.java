package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

public record CreatePhysicalInstanceRequest(
        String physicalInstanceLabel,
        String dataRelationshipName
) {
}
