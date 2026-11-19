package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

/**
 * Nouvelle PhysicalInstance dans l'Étude d'une PI source, qui réutilise une partie de ses variables
 * (#1674). L'Étude est celle de la source ; le Groupe, porté pour le contrôle du droit CREATE, doit
 * être le sien. Sans libellé de DataRelationship ou de LogicalRecord, ceux-ci sont dérivés du
 * libellé de la PI.
 */
public record ReuseVariablesRequest(
        String physicalInstanceLabel,
        String dataRelationshipLabel,
        String logicalRecordLabel,
        String groupId,
        String groupAgency,
        List<String> variableIds) {
    public ReuseVariablesRequest {
        variableIds = variableIds == null ? List.of() : List.copyOf(variableIds);
    }
}
