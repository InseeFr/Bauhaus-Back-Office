package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Item;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DdiItemService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DdiItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base service for DDI items.
 * <p>
 * Provides the common {@code createOrUpdate} logic by delegating to the
 * underlying {@link DdiItemRepository}. Subclasses can add domain-specific
 * operations (e.g. {@code deprecateAll()} for Groups).
 *
 * @param <T> the DDI4 item type
 */
public abstract class AbstractDdiItemService<T extends Ddi4Item> implements DdiItemService<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DdiItemRepository<T> repository;

    protected AbstractDdiItemService(DdiItemRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    public void createOrUpdate(T item) {
        logger.info("Creating/updating {}: id={}", itemTypeName(), item.id());
        repository.createOrUpdate(item);
    }

    /**
     * Returns a human-readable name for the item type, used in log messages.
     */
    protected abstract String itemTypeName();
}
