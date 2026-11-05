package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;

/**
 * Corps de la modification partielle d'une PhysicalInstance. Un libellé absent reste inchangé ;
 * présent, il ne peut pas être vide. Le rattachement à une Étude est facultatif, mais ses deux
 * moitiés vont ensemble : une seule serait ignorée en silence par le service.
 */
public record PhysicalInstanceUpdateRequest(
        @Pattern(regexp = NOT_BLANK, message = "physicalInstanceLabel must not be blank")
        String physicalInstanceLabel,

        @Pattern(regexp = NOT_BLANK, message = "dataRelationshipLabel must not be blank")
        String dataRelationshipLabel,

        @Pattern(regexp = NOT_BLANK, message = "logicalRecordLabel must not be blank")
        String logicalRecordLabel,

        String studyUnitId,
        String studyUnitAgency,
        String groupId,
        String groupAgency) {

    /** Au moins un caractère non blanc ; {@code @Pattern} laisse passer {@code null}. */
    private static final String NOT_BLANK = "(?s).*\\S.*";

    @AssertTrue(message = "studyUnitId and studyUnitAgency must be given together")
    public boolean isStudyUnitComplete() {
        return (studyUnitId == null) == (studyUnitAgency == null);
    }

    UpdatePhysicalInstanceRequest toDomain() {
        return new UpdatePhysicalInstanceRequest(
                physicalInstanceLabel,
                dataRelationshipLabel,
                logicalRecordLabel,
                studyUnitId,
                studyUnitAgency,
                groupId,
                groupAgency);
    }
}
