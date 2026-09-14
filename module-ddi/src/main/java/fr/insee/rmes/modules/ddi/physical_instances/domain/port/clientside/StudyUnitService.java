package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Client-side port for managing DDI StudyUnit items.
 */
public interface StudyUnitService extends DdiItemService<Ddi4StudyUnit> {

    List<PartialStudyUnit> getAll();

    /**
     * La StudyUnit d'identifiant {@code id}, ou {@link Optional#empty()} si elle n'existe pas encore
     * dans le dépôt DDI.
     */
    Optional<Ddi4StudyUnit> find(String agencyId, String id);

    void addPhysicalInstance(Ddi4StudyUnit studyUnit, Reference physicalInstanceReference);

    /**
     * Deprecates only the existing study units whose identifier is in {@code studyUnitIds}.
     */
    void deprecate(Collection<String> studyUnitIds);
}
