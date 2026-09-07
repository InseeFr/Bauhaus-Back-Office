package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;

import java.util.Collection;
import java.util.List;

/**
 * Client-side port for managing DDI StudyUnit items.
 */
public interface StudyUnitService extends DdiItemService<Ddi4StudyUnit> {

    List<PartialStudyUnit> getAll();

    void addPhysicalInstance(Ddi4StudyUnit studyUnit, Reference physicalInstanceReference);

    /**
     * Deprecates only the existing study units whose identifier is in {@code studyUnitIds}.
     */
    void deprecate(Collection<String> studyUnitIds);
}
