package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VersionedItem;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aligne les versions d'un payload de sauvegarde sur l'état stocké.
 *
 * <p>Colectica sert la dernière version d'un item. Un payload qui réécrit en v1 un item stocké en v2
 * — le front émet toujours {@code Version: "1"} — créerait une v1 fantôme que personne ne relit : la
 * modification serait perdue. Chaque item connu du stockage reprend donc sa version stockée, et chaque
 * {@code Reference} vers un tel item pointe cette version, URN comprise. Les items et références
 * inconnus du stockage (nouveaux, ou hors de la PhysicalInstance) gardent la version reçue. Le lignage
 * ({@link BasedOnObject}) désigne la version dont un item est issu, pas la dernière : il est laissé tel
 * quel.
 *
 * <p>Parcours réflexif des records, comme {@link VersionDateReconciler} : tout futur champ portant une
 * {@code Reference} est couvert sans modification.
 */
public final class StoredVersions {

    private StoredVersions() {}

    public static Ddi4Response align(Ddi4Response stored, Ddi4Response incoming) {
        return align(stored, List.of(), incoming);
    }

    /**
     * Comme {@link #align(Ddi4Response, Ddi4Response)}, avec en plus la version stockée d'items que le
     * payload référence sans les contenir ({@code storedReferences}, typiquement obtenue pour les
     * {@link #references cibles} du payload).
     */
    public static Ddi4Response align(Ddi4Response stored, List<Reference> storedReferences, Ddi4Response incoming) {
        if (incoming == null) {
            return null;
        }
        Map<String, String> storedVersionByKey = new HashMap<>();
        storedReferences.forEach(
                reference -> storedVersionByKey.put(key(reference.agency(), reference.id()), reference.version()));
        if (stored != null) {
            stored.items().forEach(item -> storedVersionByKey.put(key(item.agency(), item.id()), item.version()));
        }
        return (Ddi4Response) align(incoming, storedVersionByKey);
    }

    /** Les cibles des {@code Reference} du payload, lignage exclu : celles que {@code align} réaligne. */
    public static List<Reference> references(Ddi4Response incoming) {
        List<Reference> references = new ArrayList<>();
        collectReferences(incoming, references);
        return references;
    }

    private static void collectReferences(Object value, List<Reference> references) {
        switch (value) {
            case null -> {}
            case Reference reference -> references.add(reference);
            case BasedOnObject _ -> {}
            case List<?> list -> list.forEach(element -> collectReferences(element, references));
            case Record recordValue -> {
                for (RecordComponent component : recordValue.getClass().getRecordComponents()) {
                    collectReferences(componentValue(recordValue, component), references);
                }
            }
            default -> {}
        }
    }

    private static Object align(Object value, Map<String, String> storedVersionByKey) {
        return switch (value) {
            case null -> null;
            case Reference reference -> align(reference, storedVersionByKey);
            case BasedOnObject lineage -> lineage;
            case List<?> list ->
                list.stream().map(element -> align(element, storedVersionByKey)).toList();
            case Record recordValue -> align(recordValue, storedVersionByKey);
            default -> value;
        };
    }

    private static Reference align(Reference reference, Map<String, String> storedVersionByKey) {
        String storedVersion = storedVersionByKey.get(key(reference.agency(), reference.id()));
        if (storedVersion == null || storedVersion.equals(reference.version())) {
            return reference;
        }
        return new Reference(
                reference.type(),
                Reference.synthesizeUrn(reference.agency(), reference.id(), storedVersion),
                reference.agency(),
                reference.id(),
                storedVersion);
    }

    /** Record reconstruit composant par composant ; un item stocké reprend sa version et son URN. */
    private static Record align(Record recordValue, Map<String, String> storedVersionByKey) {
        String storedVersion = recordValue instanceof Ddi4VersionedItem item
                ? storedVersionByKey.get(key(item.agency(), item.id()))
                : null;
        RecordComponent[] components = recordValue.getClass().getRecordComponents();
        Object[] values = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
            values[i] = storedVersion == null
                    ? align(componentValue(recordValue, components[i]), storedVersionByKey)
                    : switch (components[i].getName()) {
                        case "version" -> storedVersion;
                        case "urn" -> {
                            Ddi4VersionedItem item = (Ddi4VersionedItem) recordValue;
                            yield Reference.synthesizeUrn(item.agency(), item.id(), storedVersion);
                        }
                        default -> align(componentValue(recordValue, components[i]), storedVersionByKey);
                    };
        }
        try {
            return recordValue.getClass().getDeclaredConstructor(types).newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to rebuild record " + recordValue.getClass(), e);
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

    private static String key(String agency, String id) {
        return agency + "|" + id;
    }
}
