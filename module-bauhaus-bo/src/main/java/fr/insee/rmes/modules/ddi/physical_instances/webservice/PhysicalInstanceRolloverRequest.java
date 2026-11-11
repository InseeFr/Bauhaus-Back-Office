package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RolloverRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RolloverVariable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Corps de la nouvelle période d'une PhysicalInstance (#1672). Le libellé, l'Étude cible et le
 * Groupe (qui porte le contrôle du droit CREATE et doit être celui de la source) sont obligatoires,
 * comme au moins une variable. Sans libellé de DataRelationship ou de LogicalRecord, le back les
 * dérive de celui de la PI.
 */
public record PhysicalInstanceRolloverRequest(
        @NotBlank(message = "physicalInstanceLabel is required")
        String physicalInstanceLabel,

        String dataRelationshipLabel,
        String logicalRecordLabel,
        @NotBlank(message = "studyUnitId is required") String studyUnitId,
        @NotBlank(message = "studyUnitAgency is required") String studyUnitAgency,
        @NotBlank(message = "groupId is required") String groupId,
        @NotBlank(message = "groupAgency is required") String groupAgency,
        @NotEmpty(message = "variables is required") List<@Valid Variable> variables) {

    /**
     * Une variable de la source à reprendre : copiée, ou remplacée par {@code reusedAgency/reusedId},
     * variable de l'étude cible déjà dérivée d'elle.
     */
    public record Variable(
            @NotBlank(message = "sourceId is required") String sourceId, String reusedAgency, String reusedId) {}

    RolloverRequest toDomain() {
        return new RolloverRequest(
                physicalInstanceLabel,
                dataRelationshipLabel,
                logicalRecordLabel,
                studyUnitId,
                studyUnitAgency,
                groupId,
                groupAgency,
                variables.stream()
                        .map(variable ->
                                new RolloverVariable(variable.sourceId(), variable.reusedAgency(), variable.reusedId()))
                        .toList());
    }
}
