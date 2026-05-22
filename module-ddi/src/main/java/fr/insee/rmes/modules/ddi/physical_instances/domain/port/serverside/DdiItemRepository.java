package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

/**
 * Generic server-side port for persisting DDI items.
 *
 * @param <T> the DDI4 item type (e.g. generated Group, StudyUnit)
 */
public interface DdiItemRepository<T> {

    void createOrUpdate(T item);
}
