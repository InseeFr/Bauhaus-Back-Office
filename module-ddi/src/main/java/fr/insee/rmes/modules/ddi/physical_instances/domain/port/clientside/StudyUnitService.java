package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DDIReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.generated.StudyUnit;

import java.util.List;

/**
 * Client-side port for managing DDI StudyUnit items.
 */
public interface StudyUnitService extends DdiItemService<StudyUnit> {

    List<PartialStudyUnit> getAll();

    void addPhysicalInstance(StudyUnit studyUnit, DDIReference physicalInstanceReference);
}
