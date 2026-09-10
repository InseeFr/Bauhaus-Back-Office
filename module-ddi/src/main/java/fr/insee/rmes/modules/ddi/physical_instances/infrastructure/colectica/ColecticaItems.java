package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Conversions sans état entre les enveloppes d'items Colectica ({@link ColecticaItem},
 * {@link ColecticaItemResponse}, {@link ColecticaSetItem}) et les fragments DDI 3.3 manipulés par les
 * convertisseurs.
 */
final class ColecticaItems {

    private ColecticaItems() {}

    /** Clé {@code agence|identifiant} des mémos de relations. */
    static String key(String agency, String id) {
        return agency + "|" + id;
    }

    /**
     * Une seule entrée par item — celle de la version la plus haute.
     * <p>
     * Colectica indexe les items ET les relations par version : {@code _query} comme
     * {@code _query/relationship/.../descriptions} peuvent renvoyer autant de lignes qu'un item a de
     * versions. Sans ce filtre, un item versionné apparaît plusieurs fois dans les listings et
     * multiplie les lignes de la recherche avancée. L'ordre de première apparition est conservé.
     */
    static List<ColecticaItem> latestVersions(List<ColecticaItem> items) {
        return latestByKey(items, item -> key(item.agencyId(), item.identifier()), ColecticaItem::version);
    }

    /** Même filtre « dernière version » pour les résultats de {@code _query/advanced}. */
    static List<ColecticaAdvancedItem> latestAdvancedVersions(List<ColecticaAdvancedItem> items) {
        return latestByKey(items, item -> key(item.agencyId(), item.identifier()), ColecticaAdvancedItem::version);
    }

    /**
     * Les références distinctes (agence + identifiant). Les {@link ItemReference} ne portent pas de
     * version : les descriptions de relation d'un même item à plusieurs versions y sont
     * indiscernables, donc littéralement en double.
     */
    static List<ItemReference> distinctReferences(List<ItemReference> references) {
        Map<String, ItemReference> byKey = new LinkedHashMap<>();
        for (ItemReference reference : references) {
            byKey.putIfAbsent(key(reference.agencyId(), reference.identifier()), reference);
        }
        return List.copyOf(byKey.values());
    }

    private static <T> List<T> latestByKey(List<T> items, Function<T, String> keyOf, Function<T, Integer> versionOf) {
        Map<String, T> byKey = new LinkedHashMap<>();
        for (T item : items) {
            byKey.merge(
                    keyOf.apply(item),
                    item,
                    (kept, candidate) ->
                            version(versionOf.apply(candidate)) > version(versionOf.apply(kept)) ? candidate : kept);
        }
        return List.copyOf(byKey.values());
    }

    /** Une version absente vaut 0 : n'importe quelle version explicite l'emporte. */
    private static int version(Integer version) {
        return version == null ? 0 : version;
    }

    static ItemReference itemRef(ColecticaItem item) {
        return new ItemReference(item.agencyId(), item.identifier());
    }

    static ColecticaItemResponse toColecticaItem(Ddi3Response.Ddi3Item ddi3Item) {
        return new ColecticaItemResponse(
                ddi3Item.itemType(),
                ddi3Item.agencyId(),
                Integer.parseInt(ddi3Item.version()),
                ddi3Item.identifier(),
                ddi3Item.item(),
                ddi3Item.versionDate(),
                ddi3Item.versionResponsibility(),
                ddi3Item.isPublished(),
                ddi3Item.isDeprecated(),
                ddi3Item.isProvisional(),
                ddi3Item.itemFormat());
    }

    static Ddi3Response.Ddi3Item toDdi3Item(ColecticaItemResponse item) {
        return new Ddi3Response.Ddi3Item(
                item.itemType(),
                item.agencyId(),
                String.valueOf(item.version()),
                item.identifier(),
                item.item(),
                item.versionDate(),
                item.versionResponsibility(),
                item.isPublished(),
                item.isDeprecated(),
                item.isProvisional(),
                item.itemFormat());
    }

    static List<Ddi3Response.Ddi3Item> toDdi3Items(ColecticaItemResponse[] itemResponses) {
        return Arrays.stream(itemResponses).map(ColecticaItems::toDdi3Item).toList();
    }

    static List<String> fragmentXmls(List<ColecticaItemResponse> items) {
        return items.stream()
                .map(ColecticaItemResponse::item)
                .filter(Objects::nonNull)
                .toList();
    }

    static List<GetDescriptionsRequest.IdentifierRef> identifiersOfSet(ColecticaSetItem[] setItems) {
        return Arrays.stream(setItems)
                .map(item ->
                        new GetDescriptionsRequest.IdentifierRef(item.agencyId(), item.identifier(), item.version()))
                .toList();
    }

    static List<GetDescriptionsRequest.IdentifierRef> identifiersOf(List<ColecticaItem> items) {
        return items.stream()
                .map(item ->
                        new GetDescriptionsRequest.IdentifierRef(item.agencyId(), item.identifier(), item.version()))
                .toList();
    }
}
