package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Item;

/**
 * Generic server-side port for persisting DDI items.
 *
 * @param <T> the DDI4 item type (e.g. Ddi4Group, Ddi4StudyUnit)
 */
public interface DdiItemRepository<T extends Ddi4Item> {

    void createOrUpdate(T item);
}
