package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Conversions sans état entre les enveloppes d'items Colectica ({@link ColecticaItem},
 * {@link ColecticaItemResponse}, {@link ColecticaSetItem}) et les fragments DDI 3.3 manipulés par les
 * convertisseurs.
 */
final class ColecticaItems {

    private ColecticaItems() {
    }

    /** Clé {@code agence|identifiant} des mémos de relations. */
    static String key(String agency, String id) {
        return agency + "|" + id;
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
            ddi3Item.itemFormat()
        );
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
            item.itemFormat()
        );
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
            .map(item -> new GetDescriptionsRequest.IdentifierRef(
                item.agencyId(), item.identifier(), item.version()))
            .toList();
    }

    static List<GetDescriptionsRequest.IdentifierRef> identifiersOf(List<ColecticaItem> items) {
        return items.stream()
            .map(item -> new GetDescriptionsRequest.IdentifierRef(
                item.agencyId(), item.identifier(), item.version()))
            .toList();
    }
}
