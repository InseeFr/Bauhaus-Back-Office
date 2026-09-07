package fr.insee.rmes.modules.ddi.config;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Pre-loads the mutualized code lists cache at application startup, so the first user request is
 * already served from the cache instead of paying for the (potentially slow) Colectica tree walk.
 *
 * <p>Triggered once the application is ready ({@link ApplicationReadyEvent}); the actual load runs on
 * the {@link TaskExecutor} so a slow or unreachable Colectica never blocks startup. Calling
 * {@link DDIRepository#getMutualizedCodesLists()} through its Spring proxy populates the
 * {@code @Cacheable} caches (both the code lists and the underlying package references). Failures are
 * swallowed: the cache is simply loaded lazily on the first request instead.
 *
 * <p>Disable with {@code fr.insee.rmes.bauhaus.colectica.cache-warmup-enabled=false}.
 */
@Component
@ConditionalOnProperty(
    name = "fr.insee.rmes.bauhaus.colectica.cache-warmup-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class MutualizedCodesCacheWarmer {

    private static final Logger logger = LoggerFactory.getLogger(MutualizedCodesCacheWarmer.class);

    private final DDIRepository ddiRepository;
    private final TaskExecutor taskExecutor;

    public MutualizedCodesCacheWarmer(
            DDIRepository ddiRepository,
            @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.ddiRepository = ddiRepository;
        this.taskExecutor = taskExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpOnStartup() {
        taskExecutor.execute(this::warmUp);
    }

    private void warmUp() {
        warmUpMutualizedCodesLists();
        warmUpPhysicalInstanceSearchRows();
    }

    private void warmUpMutualizedCodesLists() {
        logger.info("Mutualized codes list cache warm-up started");
        long startedAt = System.currentTimeMillis();
        try {
            int count = ddiRepository.getMutualizedCodesLists().size();
            logger.info("Mutualized codes list cache warm-up finished: {} entries loaded in {} ms",
                count, System.currentTimeMillis() - startedAt);
        } catch (RuntimeException e) {
            logger.warn("Mutualized codes list cache warm-up failed after {} ms; "
                + "the cache will be loaded lazily on the first request: {}",
                System.currentTimeMillis() - startedAt, e.getMessage());
        }
    }

    /**
     * Pré-charge le cache des lignes de recherche avancée d'instances physiques : la descente
     * Colectica Group → StudyUnit → PhysicalInstance est coûteuse, on la paie une fois au démarrage
     * plutôt qu'à la première ouverture de la page de recherche.
     */
    private void warmUpPhysicalInstanceSearchRows() {
        logger.info("Physical instance search rows cache warm-up started");
        long startedAt = System.currentTimeMillis();
        try {
            int count = ddiRepository.getPhysicalInstanceSearchRows().size();
            logger.info("Physical instance search rows cache warm-up finished: {} rows loaded in {} ms",
                count, System.currentTimeMillis() - startedAt);
        } catch (RuntimeException e) {
            logger.warn("Physical instance search rows cache warm-up failed after {} ms; "
                + "the cache will be loaded lazily on the first request: {}",
                System.currentTimeMillis() - startedAt, e.getMessage());
        }
    }
}
