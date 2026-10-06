package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedItem;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Résolution des {@code versionDate} des listes de codes : le {@code versionDate} de l'enveloppe
 * {@code _query} n'est pas fiable (Colectica renvoie {@code 0001-01-01}). On lit
 * {@code DateProperties.versionDate} de {@code _query/advanced}, que Colectica indexe : un seul appel
 * pour toutes les listes, sans télécharger leur XML — qui pèse 15 Mo (~11 s) pour 45 000 codes.
 */
class ColecticaVersionDates {

    private static final String CODE_LIST = "CodeList";

    private final ColecticaClient colecticaClient;
    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    ColecticaVersionDates(
            ColecticaClient colecticaClient,
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration) {
        this.colecticaClient = colecticaClient;
        this.instanceConfiguration = instanceConfiguration;
    }

    /**
     * Associe chaque {@code agence/identifiant} des références données (et présentes dans
     * {@code itemsByKey}) au {@code versionDate} indexé par Colectica pour sa dernière version. Map
     * vide quand il n'y a rien à résoudre ; une liste sans date indexée (dépréciée, par exemple :
     * {@code _query/advanced} les exclut) est absente de la map.
     */
    Map<String, Date> byKey(List<ItemReference> refs, Map<String, ColecticaItem> itemsByKey) {
        Set<String> wantedKeys = refs.stream()
                .map(ref -> ref.agencyId() + "/" + ref.identifier())
                .filter(itemsByKey::containsKey)
                .collect(Collectors.toSet());
        if (wantedKeys.isEmpty()) {
            return Map.of();
        }

        ColecticaAdvancedResponse response = colecticaClient.queryAdvanced(
                List.of(instanceConfiguration.itemTypes().get(CODE_LIST)));
        if (response == null || response.results() == null) {
            return Map.of();
        }

        Map<String, Date> versionDateByKey = new HashMap<>();
        for (ColecticaAdvancedItem item : ColecticaItems.latestAdvancedVersions(response.results())) {
            String key = item.agencyId() + "/" + item.identifier();
            Date versionDate = versionDate(item);
            if (wantedKeys.contains(key) && versionDate != null) {
                versionDateByKey.put(key, versionDate);
            }
        }
        return versionDateByKey;
    }

    private static Date versionDate(ColecticaAdvancedItem item) {
        if (item.dateProperties() == null) {
            return null;
        }
        List<String> versionDates = item.dateProperties().get("versionDate");
        return versionDates == null || versionDates.isEmpty() ? null : ColecticaDates.parse(versionDates.getFirst());
    }
}
