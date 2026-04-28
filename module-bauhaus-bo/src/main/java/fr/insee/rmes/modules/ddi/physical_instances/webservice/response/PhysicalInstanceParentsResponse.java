package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;

public record PhysicalInstanceParentsResponse(
    ParentRef studyUnit,
    ParentRef group
) {
    public record ParentRef(String agency, String id) {}

    public static PhysicalInstanceParentsResponse fromDomain(PhysicalInstanceParents parents) {
        return new PhysicalInstanceParentsResponse(
            new ParentRef(parents.studyUnitAgency(), parents.studyUnitId()),
            new ParentRef(parents.groupAgency(), parents.groupId())
        );
    }
}
