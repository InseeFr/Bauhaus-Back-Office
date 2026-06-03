package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * A usage of a CodeList: a Variable (identified by agency + id) that references the code list,
 * together with the PhysicalInstance (agency + id) it belongs to.
 */
public record CodeListVariableUsage(
        String physicalInstanceAgencyId,
        String physicalInstanceId,
        String variableAgencyId,
        String variableId
) {}
