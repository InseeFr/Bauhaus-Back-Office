package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CATEGORY;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.DATA_RELATIONSHIP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_MISSING_VALUES_REPRESENTATION;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

/** Lectures DDI4 d'une PhysicalInstance et de ses DataRelationships. */
class ColecticaPhysicalInstanceReader {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaPhysicalInstanceReader.class);

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
            new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, null, null, null, null);

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
        ColecticaItemResponse root;
        try {
            root = colecticaClient.getItem(agencyId, id, null);
        } catch (HttpClientErrorException.NotFound _) {
            return null;
        }
        if (root == null) {
            return null;
        }
        try {
            List<ColecticaItemResponse> items = new ArrayList<>(List.of(root));
            List<ColecticaItemResponse> dataRelationships = latest(references(
                    convert(List.of(root)).physicalInstance(), pi -> nonNull(pi.dataRelationshipReference())));
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
            return convert(items);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process DDI response", e);
        }
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

    /** La dernière version des items référencés ({@code item/_getListLatest}), aucun appel quand il n'y en a pas. */
    private List<ColecticaItemResponse> latest(List<ItemReference> references) {
        if (references.isEmpty()) {
            return List.of();
        }
        ColecticaItemResponse[] items = colecticaClient.getLatestItems(references);
        return items == null ? List.of() : Arrays.asList(items);
    }

    /** Ces items convertis en DDI4 ; réponse vide pour une liste vide. */
    private Ddi4Response convert(List<ColecticaItemResponse> items) {
        if (items.isEmpty()) {
            return EMPTY;
        }
        Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(
                        null, items.stream().map(ColecticaItems::toDdi3Item).toList()),
                Ddi4Response.SCHEMA);
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
                new Ddi3Response(null, ColecticaItems.toDdi3Items(stored)), Ddi4Response.SCHEMA);
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
                    ddi3ToDdi4Converter.convertDdi3ToDdi4(new Ddi3Response(null, ddi3Items), Ddi4Response.SCHEMA);
            return response != null && response.codeList() != null ? response.codeList() : List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch CodeLists for PhysicalInstance", e);
        }
    }

    /**
     * Le fragment de la PhysicalInstance et toutes ses DataRelationships en DDI4 (#447 / #1146),
     * dernière version quand {@code version} est {@code null}. La PhysicalInstance, les fragments
     * DataRelationship et les Variables qu'ils référencent sont convertis ; les CodeList/Category
     * référencées sont écartées.
     */
    Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        logger.info("Fetching data relationships {}/{}/{}", agencyId, id, version);
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, version);
            if (setReader.isMissingOrWrongType(itemResponses, id, PHYSICAL_INSTANCE)) {
                return null;
            }
            ColecticaItemResponse[] fragments = filterPhysicalInstanceDataRelationshipsAndVariables(itemResponses);
            Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                    new Ddi3Response(null, ColecticaItems.toDdi3Items(fragments)), Ddi4Response.SCHEMA);
            return ColecticaSetReader.withTopLevelReference(
                    response, setReader.findTopLevelReference(itemResponses, PHYSICAL_INSTANCE));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data relationships " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /**
     * Mêmes fragments que {@link #getDataRelationships}, rendus en DDI 3.3 dans une unique
     * {@code <FragmentInstance>} (#447).
     */
    String getDataRelationshipsXml(String agencyId, String id, String version) {
        logger.info("Fetching data relationships XML {}/{}/{}", agencyId, id, version);
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, version);
            if (setReader.isMissingOrWrongType(itemResponses, id, PHYSICAL_INSTANCE)) {
                return null;
            }
            ColecticaItemResponse[] fragments = filterPhysicalInstanceDataRelationshipsAndVariables(itemResponses);
            return ColecticaXml.assembleFragmentInstance(
                    ColecticaItems.fragmentXmls(Arrays.stream(fragments).toList()));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch data relationships XML " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /**
     * Garde le fragment de la PhysicalInstance, les fragments DataRelationship du set et les Variables
     * référencées par leurs {@code VariablesInRecord}/{@code VariableUsedReference} (#447). Dans un set
     * de PhysicalInstance les Variables sont exactement celles utilisées par les data relationships, un
     * filtre sur le type suffit donc.
     *
     * <p>S'y ajoutent les valeurs sentinelles des variables (#1591) : les
     * {@code ManagedMissingValuesRepresentation} du set — elles aussi exactement celles référencées par
     * ses variables —, la CodeList de sentinelles que chacune référence et les Category de ses codes,
     * sans quoi les sentinelles seraient consultables sans leurs valeurs ni leurs libellés. Les autres
     * CodeList/Category, celles des représentations, restent délibérément écartées : le front les charge
     * paresseusement par un endpoint dédié.
     *
     * <p>La PhysicalInstance est placée en tête (#1146), avant les éléments qui la composent, quel que
     * soit l'ordre des descriptions renvoyées par Colectica.
     */
    private ColecticaItemResponse[] filterPhysicalInstanceDataRelationshipsAndVariables(
            ColecticaItemResponse[] itemResponses) {
        Map<String, String> types = instanceConfiguration.itemTypes();
        String physicalInstanceType = types.get(PHYSICAL_INSTANCE);
        String dataRelationshipType = types.get(DATA_RELATIONSHIP);
        String variableType = types.get(VARIABLE);

        List<ColecticaItemResponse> missingValuesRepresentations =
                itemsOfType(itemResponses, types.get(MANAGED_MISSING_VALUES_REPRESENTATION));
        List<ColecticaItemResponse> sentinelCodeLists = itemsWithIdentifiers(
                itemsOfType(itemResponses, types.get(CODE_LIST)), sentinelCodeListIds(missingValuesRepresentations));
        List<ColecticaItemResponse> sentinelCategories =
                itemsWithIdentifiers(itemsOfType(itemResponses, types.get(CATEGORY)), categoryIds(sentinelCodeLists));

        return Stream.of(
                        Arrays.stream(itemResponses)
                                .filter(item -> Objects.equals(item.itemType(), physicalInstanceType)),
                        Arrays.stream(itemResponses)
                                .filter(item -> Objects.equals(item.itemType(), dataRelationshipType)
                                        || Objects.equals(item.itemType(), variableType)),
                        missingValuesRepresentations.stream(),
                        sentinelCodeLists.stream(),
                        sentinelCategories.stream())
                .flatMap(items -> items)
                .toArray(ColecticaItemResponse[]::new);
    }

    /** Les identifiants des CodeLists de sentinelles référencées par ces MMVR. */
    private Set<String> sentinelCodeListIds(List<ColecticaItemResponse> missingValuesRepresentations) {
        return missingValuesRepresentations.stream()
                .map(ColecticaItemResponse::item)
                .filter(Objects::nonNull)
                .map(ddi3ToDdi4Converter::toManagedMissingValuesRepresentation)
                .map(Ddi4ManagedMissingValuesRepresentation::missingCodeRepresentation)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(CodeRepresentation::codeListReference)
                .filter(Objects::nonNull)
                .map(Reference::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Les identifiants des Category référencées par les codes de ces CodeLists. */
    private Set<String> categoryIds(List<ColecticaItemResponse> codeLists) {
        return codeLists.stream()
                .map(ColecticaItemResponse::item)
                .filter(Objects::nonNull)
                .map(ddi3ToDdi4Converter::toCodeList)
                .map(Ddi4CodeList::code)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Code::categoryReference)
                .filter(Objects::nonNull)
                .map(Reference::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** Les items du type donné, ou aucun quand le type n'est pas configuré. */
    private static List<ColecticaItemResponse> itemsOfType(ColecticaItemResponse[] itemResponses, String itemType) {
        if (itemType == null) {
            return List.of();
        }
        return Arrays.stream(itemResponses)
                .filter(item -> Objects.equals(item.itemType(), itemType))
                .toList();
    }

    private static List<ColecticaItemResponse> itemsWithIdentifiers(
            List<ColecticaItemResponse> items, Set<String> identifiers) {
        return items.stream()
                .filter(item -> identifiers.contains(item.identifier()))
                .toList();
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
