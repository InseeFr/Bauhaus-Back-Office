package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CATEGORY;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_MISSING_VALUES_REPRESENTATION;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/** Lectures DDI4 d'une PhysicalInstance et de ses DataRelationships. */
class ColecticaPhysicalInstanceReader {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaPhysicalInstanceReader.class);

    /** Taille d'un lot de {@code item/_getListLatest} (meilleur temps mesuré sur la démo). */
    private static final int LATEST_ITEMS_BATCH_SIZE = 1000;

    /** Lots de {@code item/_getListLatest} en vol en même temps, comme les descentes du catalogue. */
    private static final int MAX_CONCURRENT_BATCHES = 8;

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final ColecticaSetReader setReader;
    private final ColecticaClient colecticaClient;

    ColecticaPhysicalInstanceReader(
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            ColecticaSetReader setReader,
            ColecticaClient colecticaClient) {
        this.instanceConfiguration = instanceConfiguration;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.setReader = setReader;
        this.colecticaClient = colecticaClient;
    }

    private static final Ddi4Response EMPTY =
            new Ddi4Response(Ddi4Response.SCHEMA_ID, null, null, null, null, null, null, null);

    /**
     * La PhysicalInstance, ses DataRelationships, leurs Variables et les MMVR de ces variables, en
     * dernière version. Les CodeList et Category sont volontairement écartées : le front les charge
     * paresseusement (endpoint dédié /codeslists).
     *
     * <p>Lecture par descente des références, un {@code item/_getListLatest} par niveau. Le set d'une PI
     * qui référence une nomenclature compte des dizaines de milliers d'items, que Colectica parcourt en
     * entier même pour un {@code _query/set} filtré par type (~25 s mesurées pour 5 items rendus, ~45 s
     * par {@code set/}). {@code _getListLatest} rend, comme {@code set/}, la dernière version de chaque
     * item.
     */
    Ddi4Response getPhysicalInstance(String agencyId, String id) {
        ColecticaItemResponse root = fetchRoot(agencyId, id, null);
        if (root == null) {
            return null;
        }
        try {
            return convert(descend(root));
        } catch (Exception e) {
            throw new RuntimeException("Failed to process DDI response", e);
        }
    }

    /** L'item {@code id} dans la version demandée (dernière quand nulle), {@code null} s'il est inconnu. */
    private ColecticaItemResponse fetchRoot(String agencyId, String id, String version) {
        try {
            return colecticaClient.getItem(agencyId, id, version);
        } catch (HttpClientErrorException.NotFound _) {
            return null;
        }
    }

    /**
     * La PhysicalInstance suivie de ses DataRelationships, de leurs Variables et des MMVR de ces
     * variables, un {@code item/_getListLatest} par niveau.
     */
    private List<ColecticaItemResponse> descend(ColecticaItemResponse root) {
        List<ColecticaItemResponse> items = new ArrayList<>(List.of(root));
        List<ColecticaItemResponse> dataRelationships = latest(
                references(convert(List.of(root)).physicalInstance(), pi -> nonNull(pi.dataRelationshipReference())));
        items.addAll(dataRelationships);
        List<ColecticaItemResponse> variables = latest(references(
                convert(dataRelationships).dataRelationship(),
                dr -> nonNull(dr.logicalRecord()).stream()
                        .map(LogicalRecord::variablesInRecord)
                        .filter(Objects::nonNull)
                        .flatMap(vir -> nonNull(vir.variableUsedReference()).stream())
                        .toList()));
        items.addAll(variables);
        items.addAll(latest(references(
                convert(variables).variable(),
                variable -> Stream.ofNullable(variable.variableRepresentation())
                        .map(VariableRepresentation::missingValuesReference)
                        .filter(Objects::nonNull)
                        .toList())));
        return items;
    }

    /**
     * {@link #descend} complété des valeurs sentinelles (#1591) : la CodeList que référence chaque
     * MMVR, puis les Category de ses codes. Les CodeList/Category des représentations restent écartées.
     */
    private List<ColecticaItemResponse> descendWithSentinels(ColecticaItemResponse root) {
        List<ColecticaItemResponse> items = descend(root);
        String missingValuesType = instanceConfiguration.itemTypes().get(MANAGED_MISSING_VALUES_REPRESENTATION);
        List<ColecticaItemResponse> missingValues = items.stream()
                .filter(item -> Objects.equals(item.itemType(), missingValuesType))
                .toList();
        List<ColecticaItemResponse> sentinelCodeLists = latest(references(
                convert(missingValues).managedMissingValuesRepresentation(),
                mmvr -> nonNull(mmvr.missingCodeRepresentation()).stream()
                        .map(CodeRepresentation::codeListReference)
                        .filter(Objects::nonNull)
                        .toList()));
        items.addAll(sentinelCodeLists);
        items.addAll(latest(references(
                convert(sentinelCodeLists).codeList(),
                codeList -> nonNull(codeList.code()).stream()
                        .map(Code::categoryReference)
                        .filter(Objects::nonNull)
                        .toList())));
        return items;
    }

    /** Les références distinctes que {@code extractor} trouve sur ces items (aucune quand la liste est nulle). */
    private static <T> List<ItemReference> references(List<T> items, Function<T, List<Reference>> extractor) {
        return nonNull(items).stream()
                .flatMap(item -> extractor.apply(item).stream())
                .filter(reference -> reference.id() != null)
                .map(reference -> new ItemReference(reference.agency(), reference.id()))
                .distinct()
                .toList();
    }

    /**
     * La dernière version des items référencés ({@code item/_getListLatest}), aucun appel quand il n'y en
     * a pas. Au-delà de {@value #LATEST_ITEMS_BATCH_SIZE} références, les lots partent en parallèle
     * ({@value #MAX_CONCURRENT_BATCHES} au plus) : une liste de sentinelles IRIS porte 33 059 Category,
     * que Colectica sert en ~16 s par lots de 1000 contre ~36 s en un seul appel. L'ordre des lots est
     * conservé.
     */
    private List<ColecticaItemResponse> latest(List<ItemReference> references) {
        if (references.isEmpty()) {
            return List.of();
        }
        if (references.size() <= LATEST_ITEMS_BATCH_SIZE) {
            return latestBatch(references);
        }
        List<List<ItemReference>> batches = new ArrayList<>();
        for (int start = 0; start < references.size(); start += LATEST_ITEMS_BATCH_SIZE) {
            batches.add(references.subList(start, Math.min(start + LATEST_ITEMS_BATCH_SIZE, references.size())));
        }
        Semaphore permits = new Semaphore(MAX_CONCURRENT_BATCHES);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<List<ColecticaItemResponse>>> futures = batches.stream()
                    .map(batch -> executor.submit(() -> throttled(permits, () -> latestBatch(batch))))
                    .toList();
            List<ColecticaItemResponse> items = new ArrayList<>(references.size());
            for (Future<List<ColecticaItemResponse>> future : futures) {
                items.addAll(future.get());
            }
            return items;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while reading the latest items", e);
        } catch (ExecutionException e) {
            throw e.getCause() instanceof RuntimeException runtime
                    ? runtime
                    : new IllegalStateException("Failed to read the latest items", e.getCause());
        }
    }

    private List<ColecticaItemResponse> latestBatch(List<ItemReference> references) {
        ColecticaItemResponse[] items = colecticaClient.getLatestItems(references);
        return items == null ? List.of() : Arrays.asList(items);
    }

    private static <T> T throttled(Semaphore permits, Callable<T> call) throws Exception {
        permits.acquire();
        try {
            return call.call();
        } finally {
            permits.release();
        }
    }

    /** Ces items convertis en DDI4 ; réponse vide pour une liste vide. */
    private Ddi4Response convert(List<ColecticaItemResponse> items) {
        if (items.isEmpty()) {
            return EMPTY;
        }
        Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(
                        null, items.stream().map(ColecticaItems::toDdi3Item).toList()),
                Ddi4Response.SCHEMA_ID);
        return response == null ? EMPTY : response;
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list == null ? List.of() : list;
    }

    /**
     * La dernière version stockée des items de ce payload, et d'eux seuls ({@code item/_getListLatest}) :
     * une sauvegarde n'a pas à relire le set complet de la PI, dont les dizaines de milliers de Category
     * d'une nomenclature référencée. Les items inconnus de Colectica sont absents de la réponse.
     */
    Ddi4Response getStoredItems(Ddi4Response items) {
        List<ItemReference> references = items.items().stream()
                .map(item -> new ItemReference(item.agency(), item.id()))
                .distinct()
                .toList();
        if (references.isEmpty()) {
            return null;
        }
        ColecticaItemResponse[] stored = colecticaClient.getLatestItems(references);
        if (stored == null || stored.length == 0) {
            return null;
        }
        return ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(null, ColecticaItems.toDdi3Items(stored)), Ddi4Response.SCHEMA_ID);
    }

    /**
     * Une référence par cible distincte connue de Colectica, pointant sa dernière version
     * ({@code item/_getLatestVersionNumbers}, sans lire le contenu des items). Les cibles inconnues sont
     * omises.
     */
    List<Reference> getLatestVersions(List<Reference> references) {
        Map<ItemReference, Reference> byTarget = new LinkedHashMap<>();
        references.forEach(
                reference -> byTarget.putIfAbsent(new ItemReference(reference.agency(), reference.id()), reference));
        if (byTarget.isEmpty()) {
            return List.of();
        }
        return colecticaClient.getLatestVersionNumbers(List.copyOf(byTarget.keySet())).stream()
                .map(latest -> Reference.of(
                        latest.agencyId(),
                        latest.identifier(),
                        String.valueOf(latest.version()),
                        byTarget.get(new ItemReference(latest.agencyId(), latest.identifier()))
                                .type()))
                .toList();
    }

    List<Ddi4CodeList> getPhysicalInstanceCodeLists(String agencyId, String id) {
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, null);
            if (itemResponses == null || itemResponses.length == 0) {
                return List.of();
            }

            Set<String> codeListAndCategoryTypes = codeListAndCategoryItemTypes();
            List<Ddi3Response.Ddi3Item> ddi3Items = Arrays.stream(itemResponses)
                    .filter(item -> codeListAndCategoryTypes.contains(item.itemType()))
                    .map(ColecticaItems::toDdi3Item)
                    .toList();

            if (ddi3Items.isEmpty()) {
                return List.of();
            }

            Ddi4Response response =
                    ddi3ToDdi4Converter.convertDdi3ToDdi4(new Ddi3Response(null, ddi3Items), Ddi4Response.SCHEMA_ID);
            return response != null && response.codeList() != null ? response.codeList() : List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch CodeLists for PhysicalInstance", e);
        }
    }

    /**
     * Le fragment de la PhysicalInstance et ceux des éléments qui la composent en DDI4 (#447 / #1146) :
     * DataRelationships, Variables, MMVR et valeurs sentinelles (#1591), lus en descendant les
     * références depuis la PI dans la version demandée (dernière quand {@code version} est nulle), les
     * éléments en dernière version comme le faisait {@code set/}. Le set n'est pas lu : celui d'une PI
     * qui référence une nomenclature compte des dizaines de milliers de Category.
     *
     * <p>{@code null} quand la PI est inconnue ou que l'item n'est pas une PhysicalInstance (#493).
     */
    Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        logger.info("Fetching data relationships {}/{}/{}", agencyId, id, version);
        ColecticaItemResponse root = fetchPhysicalInstanceRoot(agencyId, id, version);
        if (root == null) {
            return null;
        }
        try {
            return ColecticaSetReader.withTopLevelReference(
                    convert(descendWithSentinels(root)),
                    Reference.of(
                            root.agencyId(), root.identifier(), String.valueOf(root.version()), PHYSICAL_INSTANCE));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data relationships " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /**
     * Mêmes fragments que {@link #getDataRelationships}, rendus en DDI 3.3 dans une unique
     * {@code <FragmentInstance>} (#447), la PhysicalInstance en tête (#1146).
     */
    String getDataRelationshipsXml(String agencyId, String id, String version) {
        logger.info("Fetching data relationships XML {}/{}/{}", agencyId, id, version);
        ColecticaItemResponse root = fetchPhysicalInstanceRoot(agencyId, id, version);
        if (root == null) {
            return null;
        }
        try {
            return ColecticaXml.assembleFragmentInstance(ColecticaItems.fragmentXmls(descendWithSentinels(root)));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch data relationships XML " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /** La racine si c'est bien une PhysicalInstance, {@code null} sinon (le 404 ne dit pas le type). */
    private ColecticaItemResponse fetchPhysicalInstanceRoot(String agencyId, String id, String version) {
        ColecticaItemResponse root = fetchRoot(agencyId, id, version);
        if (root == null
                || !Objects.equals(
                        root.itemType(), instanceConfiguration.itemTypes().get(PHYSICAL_INSTANCE))) {
            return null;
        }
        return root;
    }

    private Set<String> codeListAndCategoryItemTypes() {
        Map<String, String> types = instanceConfiguration.itemTypes();
        if (types == null) {
            return Set.of();
        }
        return Stream.of(CODE_LIST, CATEGORY)
                .map(types::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
