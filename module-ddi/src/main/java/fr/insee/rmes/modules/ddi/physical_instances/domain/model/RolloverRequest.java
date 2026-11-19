package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

/**
 * Nouvelle période d'une PhysicalInstance (#1672) : une PI neuve dans une autre étude du groupe de
 * la source, qui reprend les variables choisies. Le Groupe, porté pour le contrôle du droit CREATE,
 * doit être celui de la source. Sans libellé de DataRelationship ou de LogicalRecord, ceux-ci sont
 * dérivés du libellé de la PI.
 */
public record RolloverRequest(
        String physicalInstanceLabel,
        String dataRelationshipLabel,
        String logicalRecordLabel,
        String studyUnitId,
        String studyUnitAgency,
        String groupId,
        String groupAgency,
        List<RolloverVariable> variables) {
    public RolloverRequest {
        variables = variables == null ? List.of() : List.copyOf(variables);
    }
}
