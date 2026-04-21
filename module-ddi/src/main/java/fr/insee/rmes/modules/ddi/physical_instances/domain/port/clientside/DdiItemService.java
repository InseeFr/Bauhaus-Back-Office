package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Item;

/**
 * Generic client-side port for managing DDI items.
 *
 * @param <T> the DDI4 item type (e.g. Ddi4Group, Ddi4StudyUnit)
 */
public interface DdiItemService<T extends Ddi4Item> {

    void createOrUpdate(T item);
}
