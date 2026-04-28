package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

public record PhysicalInstanceParents(
    String studyUnitAgency,
    String studyUnitId,
    String groupAgency,
    String groupId
) {}
