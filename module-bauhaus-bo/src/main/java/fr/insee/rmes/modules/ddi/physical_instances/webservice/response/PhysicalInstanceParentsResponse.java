package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import java.util.List;

public record PhysicalInstanceParentsResponse(ParentRef studyUnit, ParentRef group, List<String> stamps) {
    /**
     * @param operationsIri IRI, dans le module « opérations », de la série (groupe) ou de l'opération
     *     (étude) dont ce parent est le miroir ; {@code null} s'il n'en reflète aucune.
     */
    public record ParentRef(String agency, String id, String label, String operationsIri) {}

    public static PhysicalInstanceParentsResponse fromDomain(PhysicalInstanceParents parents) {
        return new PhysicalInstanceParentsResponse(
                new ParentRef(
                        parents.studyUnitAgency(),
                        parents.studyUnitId(),
                        parents.studyUnitLabel(),
                        parents.operationIri()),
                new ParentRef(parents.groupAgency(), parents.groupId(), parents.groupLabel(), parents.seriesIri()),
                parents.stamps());
    }
}
