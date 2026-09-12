package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Client-side port for managing DDI Group items.
 * <p>
 * Extends the generic {@link DdiItemService} with Group-specific operations.
 */
public interface GroupService extends DdiItemService<Ddi4Group> {

    List<PartialGroup> getAll();

    /**
     * Le groupe d'identifiant {@code id}, ou {@link Optional#empty()} s'il n'existe pas encore dans
     * le dépôt DDI.
     */
    Optional<Ddi4Group> find(String agencyId, String id);

    /**
     * Deprecates only the existing groups whose identifier is in {@code groupIds}.
     */
    void deprecate(Collection<String> groupIds);
}
