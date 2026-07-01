package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.Date;

/**
 * Ligne aplatie de la recherche avancée d'instances physiques : une PhysicalInstance jointe à sa
 * StudyUnit parente et à son Group parent, avec les libellés déjà résolus. Le front filtre/visualise
 * ces lignes par {@code label} (PI), {@code studyUnitLabel} et {@code groupLabel}.
 *
 * <p>Les champs StudyUnit/Group peuvent être {@code null} pour une PI qui n'est rattachée à aucune
 * StudyUnit résolvable (PI orpheline).
 */
public record PhysicalInstanceSearchRow(
        String agency,
        String id,
        String label,
        Date versionDate,
        String studyUnitAgency,
        String studyUnitId,
        String studyUnitLabel,
        String groupAgency,
        String groupId,
        String groupLabel
) {
}
