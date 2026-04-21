package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;

import java.util.List;

/**
 * Server-side port for persisting DDI StudyUnit items.
 */
public interface StudyUnitRepository extends DdiItemRepository<Ddi4StudyUnit> {

    List<PartialStudyUnit> getAll();

    void addPhysicalInstance(Ddi4StudyUnit studyUnit, DDIReference physicalInstanceReference);
}
