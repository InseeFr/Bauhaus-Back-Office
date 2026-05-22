package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;

import java.util.List;

/**
 * Server-side port for persisting DDI StudyUnit items.
 */
public interface StudyUnitRepository extends DdiItemRepository<StudyUnit> {

    List<PartialStudyUnit> getAll();

    void addPhysicalInstance(StudyUnit studyUnit, DDIReference physicalInstanceReference);
}
