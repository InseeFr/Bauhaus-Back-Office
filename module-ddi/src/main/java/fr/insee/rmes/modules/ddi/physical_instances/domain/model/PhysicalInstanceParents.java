package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

/**
 * Parents d'une instance physique. {@code seriesIri} et {@code operationIri} désignent la série et
 * l'opération du module « opérations » dont le groupe et l'étude sont le miroir ; {@code null} pour
 * un parent qui n'en reflète aucune.
 */
public record PhysicalInstanceParents(
        String studyUnitAgency,
        String studyUnitId,
        String studyUnitLabel,
        String groupAgency,
        String groupId,
        String groupLabel,
        List<String> stamps,
        String seriesIri,
        String operationIri) {
    public PhysicalInstanceParents {
        stamps = stamps == null ? List.of() : List.copyOf(stamps);
    }

    /**
     * Construit des parents sans série ni opération résolues.
     */
    public PhysicalInstanceParents(
            String studyUnitAgency,
            String studyUnitId,
            String studyUnitLabel,
            String groupAgency,
            String groupId,
            String groupLabel,
            List<String> stamps) {
        this(studyUnitAgency, studyUnitId, studyUnitLabel, groupAgency, groupId, groupLabel, stamps, null, null);
    }

    /**
     * Construit des parents sans label ni stamps résolus.
     */
    public PhysicalInstanceParents(String studyUnitAgency, String studyUnitId, String groupAgency, String groupId) {
        this(studyUnitAgency, studyUnitId, null, groupAgency, groupId, null, List.of());
    }

    /** Les mêmes parents, complétés de ce que la remontée des relations ne fournit pas. */
    public PhysicalInstanceParents resolved(List<String> stamps, String seriesIri, String operationIri) {
        return new PhysicalInstanceParents(
                studyUnitAgency,
                studyUnitId,
                studyUnitLabel,
                groupAgency,
                groupId,
                groupLabel,
                stamps,
                seriesIri,
                operationIri);
    }
}
