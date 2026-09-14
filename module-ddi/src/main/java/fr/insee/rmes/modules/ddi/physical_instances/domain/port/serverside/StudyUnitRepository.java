package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialStudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Server-side port for persisting DDI StudyUnit items.
 */
public interface StudyUnitRepository extends DdiItemRepository<Ddi4StudyUnit> {

    List<PartialStudyUnit> getAll();

    /**
     * La StudyUnit d'identifiant {@code id}, ou {@link Optional#empty()} si elle est absente du dépôt
     * DDI. Toute autre défaillance du dépôt remonte.
     */
    Optional<Ddi4StudyUnit> find(String agencyId, String id);

    void addPhysicalInstance(Ddi4StudyUnit studyUnit, Reference physicalInstanceReference);

    /**
     * Deprecates only the existing study units whose identifier is in {@code studyUnitIds}. Study
     * units absent from Colectica are ignored; an empty collection deprecates nothing.
     */
    void deprecate(Collection<String> studyUnitIds);
}
