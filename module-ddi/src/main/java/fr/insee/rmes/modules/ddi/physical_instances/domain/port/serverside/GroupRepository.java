package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import java.util.Collection;
import java.util.List;

/**
 * Server-side port for persisting DDI Group items.
 * <p>
 * Extends the generic {@link DdiItemRepository} with Group-specific operations
 * such as deprecating a targeted set of groups.
 */
public interface GroupRepository extends DdiItemRepository<Ddi4Group> {

    List<PartialGroup> getAll();

    /**
     * Deprecates only the existing groups whose identifier is in {@code groupIds}. Groups absent
     * from Colectica are ignored; an empty collection deprecates nothing.
     */
    void deprecate(Collection<String> groupIds);
}
