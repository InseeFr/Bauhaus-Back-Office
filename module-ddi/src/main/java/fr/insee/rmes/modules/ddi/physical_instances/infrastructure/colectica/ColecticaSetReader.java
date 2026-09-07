package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pipeline commun de lecture d'un « set » Colectica ({@code set/} puis {@code item/_getList}) et des
 * vérifications qui l'accompagnent. Partagé par les accès PhysicalInstance, DataRelationship et CodeList.
 */
class ColecticaSetReader {

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;

    ColecticaSetReader(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaClient colecticaClient
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
    }

    /**
     * Les items d'un set Colectica pour une {@code version} optionnelle (la dernière quand elle est
     * {@code null}). {@code null} quand le set est vide.
     */
    ColecticaItemResponse[] fetchSetItems(String agencyId, String id, String version) {
        ColecticaSetItem[] setItems = colecticaClient.getSet(agencyId, id, version);
        if (setItems == null || setItems.length == 0) {
            return null;
        }
        return colecticaClient.getDescriptions(ColecticaItems.identifiersOfSet(setItems));
    }

    /**
     * Vérifie que l'item racine {@code id} du set est bien du type attendu (#493). La racine figure
     * toujours parmi les items remontés par {@link #fetchSetItems}, donc la validation ne coûte aucun
     * appel réseau supplémentaire.
     *
     * <p>Renvoie {@code false} quand la racine est absente <em>ou</em> d'un autre type : les accesseurs
     * retournent alors {@code null}, converti en {@code 404} côté contrôleur. Le 404 unique (mauvais
     * type comme inexistant) ne divulgue pas l'existence d'un objet d'un type différent.
     */
    boolean rootItemHasType(ColecticaItemResponse[] itemResponses, String id, String expectedTypeKey) {
        String expectedType = instanceConfiguration.itemTypes().get(expectedTypeKey);
        return Arrays.stream(itemResponses)
            .filter(item -> Objects.equals(item.identifier(), id))
            .findFirst()
            .map(item -> Objects.equals(item.itemType(), expectedType))
            .orElse(false);
    }

    /** {@code true} quand le set est absent, vide, ou que sa racine n'est pas du type attendu. */
    boolean isMissingOrWrongType(ColecticaItemResponse[] itemResponses, String id, String expectedTypeKey) {
        return itemResponses == null || itemResponses.length == 0
            || !rootItemHasType(itemResponses, id, expectedTypeKey);
    }

    /**
     * Construit le {@code TopLevelReference} d'une FragmentInstance à partir de l'item du set dont le
     * type correspond à {@code typeKey} (#494). Des endpoints comme {@code /variables} et
     * {@code /codelist} écartent leur item racine de la sortie convertie : le convertisseur seul
     * laisserait la référence nulle, on la récupère donc depuis les descriptions brutes (qui portent
     * encore la racine avec sa version résolue). {@code null} quand la configuration de type ou l'item
     * correspondant manque.
     */
    Reference findTopLevelReference(ColecticaItemResponse[] itemResponses, String typeKey) {
        Map<String, String> types = instanceConfiguration.itemTypes();
        if (types == null) {
            return null;
        }
        String typeUuid = types.get(typeKey);
        if (typeUuid == null) {
            return null;
        }
        return Arrays.stream(itemResponses)
            .filter(item -> Objects.equals(item.itemType(), typeUuid))
            .findFirst()
            .map(item -> Reference.of(
                item.agencyId(), item.identifier(), String.valueOf(item.version()), typeKey))
            .orElse(null);
    }

    /**
     * Renvoie {@code response} avec son {@code TopLevelReference} positionné à
     * {@code topLevelReference}, en laissant intacte une référence déjà renseignée. Sans effet quand
     * l'un des deux arguments est nul.
     */
    static Ddi4Response withTopLevelReference(Ddi4Response response, Reference topLevelReference) {
        if (response == null || topLevelReference == null
            || (response.topLevelReference() != null && !response.topLevelReference().isEmpty())) {
            return response;
        }
        return new Ddi4Response(
            response.schema(),
            List.of(topLevelReference),
            response.physicalInstance(),
            response.dataRelationship(),
            response.variable(),
            response.codeList(),
            response.category(),
            response.managedMissingValuesRepresentation());
    }
}
