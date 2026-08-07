package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lectures de listes de codes : contenu d'une liste, et catalogue des listes mutualisées. */
class ColecticaCodeListRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaCodeListRepository.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final ColecticaSetReader setReader;
    private final ColecticaVersionDates versionDates;
    private final ColecticaLabels labels;
    private final MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider;

    ColecticaCodeListRepository(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        ColecticaClient colecticaClient,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter,
        ColecticaSetReader setReader,
        ColecticaVersionDates versionDates,
        ColecticaLabels labels,
        MutualizedCodeListRefsStrategy mutualizedCodeListRefsProvider
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.setReader = setReader;
        this.versionDates = versionDates;
        this.labels = labels;
        this.mutualizedCodeListRefsProvider = mutualizedCodeListRefsProvider;
    }

    /**
     * Représentation DDI4 complète d'une liste de codes (codes + catégories) pour une {@code version}
     * optionnelle (la dernière quand elle est nulle), via le pipeline {@code set/} + {@code _getList} +
     * conversion DDI3 → DDI4.
     */
    Ddi4Response getCodeList(String agencyId, String id, String version) {
        logger.info("Fetching code list {}/{}/{}", agencyId, id, version);
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, version);
            if (setReader.isMissingOrWrongType(itemResponses, id, CODE_LIST)) {
                return null;
            }
            Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(null, ColecticaItems.toDdi3Items(itemResponses)), Ddi4Response.SCHEMA);
            return ColecticaSetReader.withTopLevelReference(
                response, setReader.findTopLevelReference(itemResponses, CODE_LIST));
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to fetch code list " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /**
     * Représentation DDI 3.3 d'un set de liste de codes (CodeList + Categories référencées) dans une
     * unique {@code <FragmentInstance>} multi-fragments (#485).
     */
    String getCodeListXml(String agencyId, String id, String version) {
        logger.info("Fetching code list XML {}/{}/{}", agencyId, id, version);
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, version);
            if (setReader.isMissingOrWrongType(itemResponses, id, CODE_LIST)) {
                return null;
            }
            return ColecticaXml.assembleFragmentInstance(
                ColecticaItems.fragmentXmls(Arrays.stream(itemResponses).toList()));
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to fetch code list XML " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /**
     * Métadonnées de toutes les CodeLists atteignables depuis le package de codes mutualisés
     * configuré. Les références viennent de {@link MutualizedCodeListRefsStrategy} ; les libellés et
     * les dates sont résolus par un unique {@code _query} global sur le type CodeList, complété par la
     * lecture des {@code versionDate} dans le XML des items.
     *
     * <p>Un item n'est retenu que s'il porte un libellé non vide (pas de repli sur l'identifiant). Les
     * doublons (une CodeList atteignable par plusieurs groupes) sont éliminés par
     * {@code agence/identifiant}.
     */
    List<PartialCodesList> getMutualizedCodesLists() {
        List<ItemReference> codeListRefs = mutualizedCodeListRefsProvider.codeListRefs();
        if (codeListRefs.isEmpty()) {
            return List.of();
        }

        Map<String, ColecticaItem> itemsByKey = allCodeListsByKey();
        Map<String, Date> versionDateByKey = versionDates.byKey(codeListRefs, itemsByKey);

        Map<String, PartialCodesList> collected = new LinkedHashMap<>();
        for (ItemReference ref : codeListRefs) {
            String key = ref.agencyId() + "/" + ref.identifier();
            ColecticaItem item = itemsByKey.get(key);
            if (item == null) {
                continue;
            }
            Optional<String> label = labels.strict(item);
            if (label.isEmpty()) {
                continue;
            }
            // Nom technique (itemName) conservé à part du libellé : il sert à la recherche dans le
            // sélecteur côté front, où seul le libellé est affiché.
            String name = labels.firstNonBlank(item.itemName()).orElse(null);
            collected.putIfAbsent(key, new PartialCodesList(
                item.identifier(), label.get(), versionDateByKey.get(key), item.agencyId(), name));
        }
        logger.info("{} mutualized CodeList(s) kept", collected.size());

        return List.copyOf(collected.values());
    }

    /**
     * Résout les métadonnées (libellé, versionDate) des CodeLists dont les identifiants sont donnés.
     * Libellés via un {@code _query} global de type CodeList (les descriptions de relation ne les
     * portent pas) ; versionDate lu depuis le XML de l'item (l'enveloppe {@code _query} n'est pas
     * fiable), comme pour les listes mutualisées.
     */
    List<PartialCodesList> resolveCodeListsMetadata(Set<String> codeListIds) {
        List<ColecticaItem> keptItems = allCodeLists().stream()
            .filter(item -> item != null && codeListIds.contains(item.identifier()))
            .toList();
        if (keptItems.isEmpty()) {
            return List.of();
        }

        Map<String, ColecticaItem> itemsByKey = new LinkedHashMap<>();
        List<ItemReference> refs = new ArrayList<>();
        for (ColecticaItem item : keptItems) {
            itemsByKey.put(item.agencyId() + "/" + item.identifier(), item);
            refs.add(new ItemReference(item.agencyId(), item.identifier()));
        }
        Map<String, Date> versionDateByKey = versionDates.byKey(refs, itemsByKey);

        return keptItems.stream()
            .map(item -> new PartialCodesList(
                item.identifier(),
                labels.of(item),
                versionDateByKey.get(item.agencyId() + "/" + item.identifier()),
                item.agencyId()))
            .toList();
    }

    private Map<String, ColecticaItem> allCodeListsByKey() {
        Map<String, ColecticaItem> itemsByKey = new HashMap<>();
        for (ColecticaItem item : allCodeLists()) {
            if (item != null) {
                itemsByKey.put(item.agencyId() + "/" + item.identifier(), item);
            }
        }
        return itemsByKey;
    }

    private List<ColecticaItem> allCodeLists() {
        ColecticaResponse response = colecticaClient.query(
            List.of(instanceConfiguration.itemTypes().get(CODE_LIST)));
        return (response == null || response.results() == null) ? List.of() : response.results();
    }
}
