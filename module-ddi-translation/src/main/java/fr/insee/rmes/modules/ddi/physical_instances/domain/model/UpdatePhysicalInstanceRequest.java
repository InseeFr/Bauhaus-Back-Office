package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

public record UpdatePhysicalInstanceRequest(
        String physicalInstanceLabel,
        String dataRelationshipLabel,
        String logicalRecordLabel
) {
}