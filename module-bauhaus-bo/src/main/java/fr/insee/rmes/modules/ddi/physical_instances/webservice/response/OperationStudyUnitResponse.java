package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.OperationStudyUnit;

/** Une étude (StudyUnit) miroir d'une opération et le groupe qui la classe. */
public record OperationStudyUnitResponse(Ref studyUnit, Ref group) {

    public record Ref(String agency, String id, String label) {}

    public static OperationStudyUnitResponse fromDomain(OperationStudyUnit studyUnit) {
        return new OperationStudyUnitResponse(
                new Ref(studyUnit.studyUnitAgency(), studyUnit.studyUnitId(), studyUnit.studyUnitLabel()),
                new Ref(studyUnit.groupAgency(), studyUnit.groupId(), studyUnit.groupLabel()));
    }
}
