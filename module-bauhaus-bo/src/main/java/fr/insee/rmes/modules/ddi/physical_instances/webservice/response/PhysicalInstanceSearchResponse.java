package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import java.util.Date;

/**
 * Ligne de résultat de la recherche avancée d'instances physiques : PhysicalInstance jointe à sa
 * StudyUnit et à son Group parents, libellés déjà résolus. Le front filtre/affiche sur
 * {@code label}, {@code studyUnitLabel} et {@code groupLabel}.
 */
public record PhysicalInstanceSearchResponse(
        String agency,
        String id,
        String label,
        Date versionDate,
        String studyUnitAgency,
        String studyUnitId,
        String studyUnitLabel,
        String groupAgency,
        String groupId,
        String groupLabel) {
    public static PhysicalInstanceSearchResponse fromDomain(PhysicalInstanceSearchRow row) {
        return new PhysicalInstanceSearchResponse(
                row.agency(),
                row.id(),
                row.label(),
                row.versionDate(),
                row.studyUnitAgency(),
                row.studyUnitId(),
                row.studyUnitLabel(),
                row.groupAgency(),
                row.groupId(),
                row.groupLabel());
    }
}
