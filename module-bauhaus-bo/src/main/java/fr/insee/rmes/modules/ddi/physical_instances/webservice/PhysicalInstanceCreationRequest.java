package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import jakarta.validation.constraints.NotBlank;

/**
 * Corps de la création d'une PhysicalInstance. Le libellé et le rattachement (Groupe, Étude) sont
 * obligatoires : le Groupe porte le contrôle du droit CREATE, l'Étude reçoit la référence de la PI.
 * Sans libellé de DataRelationship ou de LogicalRecord, le back les dérive de celui de la PI.
 */
public record PhysicalInstanceCreationRequest(
        @NotBlank(message = "physicalInstanceLabel is required")
        String physicalInstanceLabel,

        String dataRelationshipLabel,
        String logicalRecordLabel,
        @NotBlank(message = "studyUnitId is required") String studyUnitId,
        @NotBlank(message = "studyUnitAgency is required") String studyUnitAgency,
        @NotBlank(message = "groupId is required") String groupId,
        @NotBlank(message = "groupAgency is required") String groupAgency) {

    CreatePhysicalInstanceRequest toDomain() {
        return new CreatePhysicalInstanceRequest(
                physicalInstanceLabel,
                dataRelationshipLabel,
                logicalRecordLabel,
                studyUnitId,
                studyUnitAgency,
                groupId,
                groupAgency);
    }
}
