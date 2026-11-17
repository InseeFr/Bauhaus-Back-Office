package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache opération → StudyUnits : toutes les StudyUnits de Colectica, chacune avec les {@code r:UserID}
 * (IRIs d'opération) qu'elle porte, lues et parsées une fois au lieu d'un {@code _query} suivi d'un
 * {@code item/_getList} de toutes les études à chaque recherche par opération.
 * <p>
 * Calculé au démarrage, recalculé en tâche de fond après chaque écriture de PhysicalInstance (elle
 * modifie les références de sa StudyUnit). Les recalculs sont sérialisés : le dernier lancé est le
 * dernier publié.
 */
class StudyUnitsByOperationIndex {

    private static final Logger logger = LoggerFactory.getLogger(StudyUnitsByOperationIndex.class);

    /** Une StudyUnit et les {@code r:UserID} lus dans son XML. */
    record IndexedStudyUnit(ColecticaItemResponse item, Set<String> userIds) {}

    private final Supplier<List<IndexedStudyUnit>> loader;
    private final Executor executor;
    private volatile List<IndexedStudyUnit> studyUnits;

    StudyUnitsByOperationIndex(Supplier<List<IndexedStudyUnit>> loader, Executor executor) {
        this.loader = loader;
        this.executor = executor;
    }

    /** Les StudyUnits dont un {@code r:UserID} vaut l'une des {@code operationIris}, dans l'ordre de Colectica. */
    List<ColecticaItemResponse> studyUnitsMirroring(Collection<String> operationIris) {
        List<IndexedStudyUnit> current = studyUnits;
        if (current == null) {
            current = refresh();
        }
        return current.stream()
                .filter(su -> su.userIds().stream().anyMatch(operationIris::contains))
                .map(IndexedStudyUnit::item)
                .toList();
    }

    /** Recalcule le cache et le rend ; les erreurs de Colectica remontent à l'appelant. */
    synchronized List<IndexedStudyUnit> refresh() {
        long startedAt = System.currentTimeMillis();
        List<IndexedStudyUnit> loaded = loader.get();
        studyUnits = loaded;
        logger.info(
                "StudyUnits by operation cache computed: {} study unit(s) for {} operation(s) in {} ms",
                loaded.size(),
                loaded.stream().flatMap(su -> su.userIds().stream()).distinct().count(),
                System.currentTimeMillis() - startedAt);
        return loaded;
    }

    /** Recalcul en tâche de fond : un échec est journalisé, le cache précédent reste servi. */
    void refreshAsync() {
        executor.execute(() -> {
            try {
                refresh();
            } catch (RuntimeException e) {
                logger.warn("StudyUnits by operation cache refresh failed: {}", e.getMessage());
            }
        });
    }
}
