package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.List;

public record PhysicalInstanceParents(
    String studyUnitAgency,
    String studyUnitId,
    String groupAgency,
    String groupId,
    List<String> stamps
) {
    public PhysicalInstanceParents {
        stamps = stamps == null ? List.of() : List.copyOf(stamps);
    }

    /**
     * Construit des parents sans stamps résolus : le repository ne connaît que
     * la relation Colectica ; les stamps sont peuplés ensuite par le service.
     */
    public PhysicalInstanceParents(String studyUnitAgency, String studyUnitId,
                                   String groupAgency, String groupId) {
        this(studyUnitAgency, studyUnitId, groupAgency, groupId, List.of());
    }

    public PhysicalInstanceParents withStamps(List<String> stamps) {
        return new PhysicalInstanceParents(studyUnitAgency, studyUnitId, groupAgency, groupId, stamps);
    }
}
