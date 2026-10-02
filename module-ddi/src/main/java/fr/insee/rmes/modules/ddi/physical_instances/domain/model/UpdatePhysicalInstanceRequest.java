package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

public record UpdatePhysicalInstanceRequest(
        String physicalInstanceLabel,
        String dataRelationshipLabel,
        String logicalRecordLabel,
        String studyUnitId,
        String studyUnitAgency,
        String groupId,
        String groupAgency) {
    public UpdatePhysicalInstanceRequest(
            String physicalInstanceLabel, String dataRelationshipLabel, String logicalRecordLabel) {
        this(physicalInstanceLabel, dataRelationshipLabel, logicalRecordLabel, null, null, null, null);
    }

    /** La requête rattache la PI à une StudyUnit (et change donc ses parents). */
    public boolean attachesToStudyUnit() {
        return studyUnitId != null && studyUnitAgency != null;
    }
}
