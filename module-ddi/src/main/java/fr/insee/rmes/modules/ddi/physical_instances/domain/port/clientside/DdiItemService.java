package fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside;

/**
 * Generic client-side port for managing DDI items.
 *
 * @param <T> the DDI4 item type (e.g. generated Group, StudyUnit)
 */
public interface DdiItemService<T> {

    void createOrUpdate(T item);
}
