package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ReuseVariablesRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Corps de la création d'une PhysicalInstance qui réutilise des variables d'une PI source (#1674).
 * L'Étude est celle de la source ; le Groupe, obligatoire, porte le contrôle du droit CREATE et doit
 * être celui de la source. Sans libellé de DataRelationship ou de LogicalRecord, le back les dérive
 * de celui de la PI.
 */
public record PhysicalInstanceReuseVariablesRequest(
        @NotBlank(message = "physicalInstanceLabel is required")
        String physicalInstanceLabel,

        String dataRelationshipLabel,
        String logicalRecordLabel,
        @NotBlank(message = "groupId is required") String groupId,
        @NotBlank(message = "groupAgency is required") String groupAgency,
        @NotEmpty(message = "variableIds is required") List<String> variableIds) {

    ReuseVariablesRequest toDomain() {
        return new ReuseVariablesRequest(
                physicalInstanceLabel, dataRelationshipLabel, logicalRecordLabel, groupId, groupAgency, variableIds);
    }
}
