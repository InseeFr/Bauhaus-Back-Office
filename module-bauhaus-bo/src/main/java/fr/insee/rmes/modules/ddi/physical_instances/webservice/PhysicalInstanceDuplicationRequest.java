package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DuplicatePhysicalInstanceRequest;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de la duplication d'une PhysicalInstance. Le libellé de la copie et son rattachement
 * (Groupe, Étude) sont obligatoires ; sans libellé de DataRelationship ou de LogicalRecord, le back
 * les dérive de celui de la PI.
 */
public record PhysicalInstanceDuplicationRequest(
        @NotBlank(message = "physicalInstanceLabel is required")
        String physicalInstanceLabel,

        String dataRelationshipLabel,
        String logicalRecordLabel,
        @NotBlank(message = "studyUnitId is required") String studyUnitId,
        @NotBlank(message = "studyUnitAgency is required") String studyUnitAgency,
        @NotBlank(message = "groupId is required") String groupId,
        @NotBlank(message = "groupAgency is required") String groupAgency) {

    DuplicatePhysicalInstanceRequest toDomain() {
        return new DuplicatePhysicalInstanceRequest(
                physicalInstanceLabel,
                dataRelationshipLabel,
                logicalRecordLabel,
                studyUnitId,
                studyUnitAgency,
                groupId,
                groupAgency);
    }
}
