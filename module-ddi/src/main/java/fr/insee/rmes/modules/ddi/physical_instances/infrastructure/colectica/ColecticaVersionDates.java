package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Résolution des {@code versionDate} depuis le XML des items : le {@code versionDate} de l'enveloppe
 * {@code _query} n'est pas fiable (Colectica renvoie {@code 0001-01-01}), il faut lire l'attribut
 * porté par le fragment lui-même.
 */
class ColecticaVersionDates {

    private final ColecticaClient colecticaClient;

    ColecticaVersionDates(ColecticaClient colecticaClient) {
        this.colecticaClient = colecticaClient;
    }

    /**
     * Récupère en un seul {@code item/_getList} le XML des références données et associe chaque
     * {@code agence/identifiant} au {@code versionDate} lu dans son XML. Les références absentes de
     * {@code itemsByKey} sont ignorées. Map vide quand il n'y a rien à récupérer ou que l'appel ne
     * ramène rien ; une référence dont le XML n'a pas de {@code versionDate} exploitable est associée
     * à {@code null}.
     */
    Map<String, Date> byKey(List<ItemReference> refs, Map<String, ColecticaItem> itemsByKey) {
        List<GetDescriptionsRequest.IdentifierRef> identifiers = refs.stream()
            .map(ref -> itemsByKey.get(ref.agencyId() + "/" + ref.identifier()))
            .filter(Objects::nonNull)
            .map(item -> new GetDescriptionsRequest.IdentifierRef(
                item.agencyId(), item.identifier(), item.version()))
            .distinct()
            .toList();
        if (identifiers.isEmpty()) {
            return Map.of();
        }

        ColecticaItemResponse[] responses = colecticaClient.getDescriptions(identifiers);
        if (responses == null) {
            return Map.of();
        }

        Map<String, Date> versionDateByKey = new HashMap<>();
        for (ColecticaItemResponse response : responses) {
            if (response == null) {
                continue;
            }
            versionDateByKey.put(
                response.agencyId() + "/" + response.identifier(),
                ColecticaXml.versionDate(response.item()));
        }
        return versionDateByKey;
    }
}
