package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.generated.Group;

import java.util.List;

/**
 * Client-side port for managing DDI Group items.
 * <p>
 * Extends the generic {@link DdiItemService} with Group-specific operations.
 */
public interface GroupService extends DdiItemService<Group> {

    List<PartialGroup> getAll();

    void deprecateAll();
}
