package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

/**
 * Duplication d'une PhysicalInstance : libellés de la copie et Groupe/Étude de rattachement.
 * Sans libellé de DataRelationship ou de LogicalRecord, ceux-ci sont dérivés du libellé de la PI.
 */
public record DuplicatePhysicalInstanceRequest(
        String physicalInstanceLabel,
        String dataRelationshipLabel,
        String logicalRecordLabel,
        String studyUnitId,
        String studyUnitAgency,
        String groupId,
        String groupAgency) {}
