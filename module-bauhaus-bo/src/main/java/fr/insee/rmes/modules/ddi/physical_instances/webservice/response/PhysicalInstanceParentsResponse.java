package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import java.util.List;

public record PhysicalInstanceParentsResponse(ParentRef studyUnit, ParentRef group, List<String> stamps) {
    public record ParentRef(String agency, String id, String label) {}

    public static PhysicalInstanceParentsResponse fromDomain(PhysicalInstanceParents parents) {
        return new PhysicalInstanceParentsResponse(
                new ParentRef(parents.studyUnitAgency(), parents.studyUnitId(), parents.studyUnitLabel()),
                new ParentRef(parents.groupAgency(), parents.groupId(), parents.groupLabel()),
                parents.stamps());
    }
}
