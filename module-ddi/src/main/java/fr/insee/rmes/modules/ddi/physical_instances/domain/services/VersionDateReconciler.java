package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VersionedItem;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Réconcilie les {@code VersionDate} d'un payload de PUT avec l'état stocké :
 * les items non modifiés gardent leur date stockée, les items modifiés ou
 * nouveaux passent à {@code now}, et la modification d'un enfant se propage à
 * ses parents via les {@code Reference} réellement présentes dans le payload
 * (aucune chaîne de types codée en dur).
 */
public final class VersionDateReconciler {

    private VersionDateReconciler() {}

    public static Ddi4Response reconcile(Ddi4Response current, Ddi4Response incoming, CogsDate now) {
        Map<String, Ddi4VersionedItem> storedByKey = new HashMap<>();
        items(current).forEach(item -> storedByKey.put(key(item), item));

        List<Ddi4VersionedItem> incomingItems = items(incoming).toList();
        Set<String> dirty = directlyDirtyKeys(incomingItems, storedByKey);
        propagateToReferencingItems(dirty, incomingItems);

        return new Ddi4Response(
                incoming.schema(),
                incoming.topLevelReference(),
                rewrite(incoming.physicalInstance(), dirty, storedByKey, now),
                rewrite(incoming.dataRelationship(), dirty, storedByKey, now),
                rewrite(incoming.variable(), dirty, storedByKey, now),
                rewrite(incoming.codeList(), dirty, storedByKey, now),
                rewrite(incoming.category(), dirty, storedByKey, now),
                rewrite(incoming.managedMissingValuesRepresentation(), dirty, storedByKey, now));
    }

    /** Items modifiés ou nouveaux : comparaison à l'état stocké en neutralisant la {@code VersionDate}. */
    private static Set<String> directlyDirtyKeys(
            List<Ddi4VersionedItem> incomingItems, Map<String, Ddi4VersionedItem> storedByKey) {
        Set<String> dirty = new HashSet<>();
        for (Ddi4VersionedItem item : incomingItems) {
            Ddi4VersionedItem stored = storedByKey.get(key(item));
            if (stored == null || !item.withVersionDate(null).equals(stored.withVersionDate(null))) {
                dirty.add(key(item));
            }
        }
        return dirty;
    }

    /**
     * Propagation enfant → parent par point fixe : l'item A contient une
     * {@code Reference} vers l'item B du payload ⇒ « B sale rend A sale »,
     * en profondeur arbitraire. Les références vers des items absents du
     * payload (ex. liste de codes mutualisée) sont ignorées.
     */
    private static void propagateToReferencingItems(Set<String> dirty, List<Ddi4VersionedItem> incomingItems) {
        Map<String, Set<String>> referencingKeysByKey = new HashMap<>();
        Set<String> incomingKeys = new HashSet<>();
        incomingItems.forEach(item -> incomingKeys.add(key(item)));
        for (Ddi4VersionedItem item : incomingItems) {
            for (Reference reference : collectReferences(item)) {
                String referencedKey = reference.agency() + "|" + reference.id();
                if (incomingKeys.contains(referencedKey) && !referencedKey.equals(key(item))) {
                    referencingKeysByKey
                            .computeIfAbsent(referencedKey, k -> new HashSet<>())
                            .add(key(item));
                }
            }
        }
        Deque<String> toVisit = new ArrayDeque<>(dirty);
        while (!toVisit.isEmpty()) {
            for (String referencing : referencingKeysByKey.getOrDefault(toVisit.pop(), Set.of())) {
                if (dirty.add(referencing)) {
                    toVisit.add(referencing);
                }
            }
        }
    }

    /**
     * Collecte récursive de toutes les {@code Reference} portées par la structure
     * de l'item (composants de records, listes, records imbriqués), par parcours
     * réflexif : tout futur champ portant une {@code Reference} est couvert sans
     * modification de l'algorithme. Les références de lignage
     * ({@link BasedOnObject}) pointent vers des versions antérieures, pas vers
     * des enfants : elles sont exclues.
     */
    private static List<Reference> collectReferences(Ddi4VersionedItem item) {
        List<Reference> references = new ArrayList<>();
        collectReferences(item, references);
        return references;
    }

    private static void collectReferences(Object value, List<Reference> references) {
        switch (value) {
            case null -> {
                /* rien à collecter */
            }
            case Reference reference -> references.add(reference);
            case BasedOnObject ignored -> {
                /* lignage : exclu de la propagation */
            }
            case List<?> list -> list.forEach(element -> collectReferences(element, references));
            case Record recordValue -> {
                for (RecordComponent component : recordValue.getClass().getRecordComponents()) {
                    collectReferences(componentValue(recordValue, component), references);
                }
            }
            default -> {
                /* scalaire : rien à collecter */
            }
        }
    }

    private static Object componentValue(Record recordValue, RecordComponent component) {
        try {
            return component.getAccessor().invoke(recordValue);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Unable to read record component " + component.getName() + " of " + recordValue.getClass(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Ddi4VersionedItem> List<T> rewrite(
            List<T> items, Set<String> dirty, Map<String, Ddi4VersionedItem> storedByKey, CogsDate now) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(item -> (T) item.withVersionDate(
                        dirty.contains(key(item))
                                ? now
                                : storedByKey.get(key(item)).versionDate()))
                .toList();
    }

    private static Stream<Ddi4VersionedItem> items(Ddi4Response response) {
        if (response == null) {
            return Stream.empty();
        }
        return Stream.of(
                        response.physicalInstance(),
                        response.dataRelationship(),
                        response.variable(),
                        response.codeList(),
                        response.category(),
                        response.managedMissingValuesRepresentation())
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Ddi4VersionedItem.class::cast);
    }

    private static String key(Ddi4VersionedItem item) {
        return item.agency() + "|" + item.id();
    }
}
