package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;

import java.util.List;

/**
 * Client-side port for managing DDI Group items.
 * <p>
 * Extends the generic {@link DdiItemService} with Group-specific operations.
 */
public interface GroupService extends DdiItemService<Ddi4Group> {

    List<PartialGroup> getAll();

    void deprecateAll();
}
