package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

public record PhysicalInstanceParents(
        String studyUnitAgency,
        String studyUnitId,
        String studyUnitLabel,
        String groupAgency,
        String groupId,
        String groupLabel,
        List<String> stamps) {
    public PhysicalInstanceParents {
        stamps = stamps == null ? List.of() : List.copyOf(stamps);
    }

    /**
     * Construit des parents sans label ni stamps résolus : le repository ne connaît que
     * la relation Colectica ; les labels (groupe, étude) et les stamps sont peuplés ensuite par le service.
     */
    public PhysicalInstanceParents(String studyUnitAgency, String studyUnitId, String groupAgency, String groupId) {
        this(studyUnitAgency, studyUnitId, null, groupAgency, groupId, null, List.of());
    }

    public PhysicalInstanceParents withStamps(List<String> stamps) {
        return new PhysicalInstanceParents(
                studyUnitAgency, studyUnitId, studyUnitLabel, groupAgency, groupId, groupLabel, stamps);
    }

    public PhysicalInstanceParents withGroupLabel(String groupLabel) {
        return new PhysicalInstanceParents(
                studyUnitAgency, studyUnitId, studyUnitLabel, groupAgency, groupId, groupLabel, stamps);
    }

    public PhysicalInstanceParents withStudyUnitLabel(String studyUnitLabel) {
        return new PhysicalInstanceParents(
                studyUnitAgency, studyUnitId, studyUnitLabel, groupAgency, groupId, groupLabel, stamps);
    }
}
