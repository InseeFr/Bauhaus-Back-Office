package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CATEGORY;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.CODE_LIST;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.DATA_RELATIONSHIP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE;

import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lectures DDI4 d'une PhysicalInstance et de ses DataRelationships. */
class ColecticaPhysicalInstanceReader {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaPhysicalInstanceReader.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final ColecticaSetReader setReader;

    ColecticaPhysicalInstanceReader(
        ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
        DDI3toDDI4ConverterService ddi3ToDdi4Converter,
        ColecticaSetReader setReader
    ) {
        this.instanceConfiguration = instanceConfiguration;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.setReader = setReader;
    }

    Ddi4Response getPhysicalInstance(String agencyId, String id) {
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, null);
            if (itemResponses == null || itemResponses.length == 0) {
                return null;
            }

            // Les CodeList et Category sont volontairement écartées du GET PI : payload réduit + on
            // évite la conversion DDI3 -> DDI4 sur ces items, souvent les plus gros. Le front les
            // charge paresseusement (clic sur une variable + endpoint dédié /codeslists).
            Set<String> excludedTypes = codeListAndCategoryItemTypes();
            List<Ddi3Response.Ddi3Item> ddi3Items = Arrays.stream(itemResponses)
                .filter(item -> !excludedTypes.contains(item.itemType()))
                .map(ColecticaItems::toDdi3Item)
                .toList();

            logger.info("Converting DDI3 to DDI4 using converter service");
            Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(null, ddi3Items), Ddi4Response.SCHEMA);

            logger.info("Successfully converted Physical Instance to DDI4 format");
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process DDI response", e);
        }
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

            Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(null, ddi3Items), Ddi4Response.SCHEMA);
            return response != null && response.codeList() != null ? response.codeList() : List.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch CodeLists for PhysicalInstance", e);
        }
    }

    /**
     * Toutes les DataRelationships d'un set de PhysicalInstance en DDI4 (#447), dernière version quand
     * {@code version} est {@code null}. Les fragments DataRelationship et les Variables qu'ils
     * référencent sont convertis ; les CodeList/Category référencées sont écartées.
     */
    Ddi4Response getDataRelationships(String agencyId, String id, String version) {
        logger.info("Fetching data relationships {}/{}/{}", agencyId, id, version);
        try {
            ColecticaItemResponse[] itemResponses = setReader.fetchSetItems(agencyId, id, version);
            if (setReader.isMissingOrWrongType(itemResponses, id, PHYSICAL_INSTANCE)) {
                return null;
            }
            ColecticaItemResponse[] dataRelationships = filterDataRelationshipsAndVariables(itemResponses);
            Ddi4Response response = ddi3ToDdi4Converter.convertDdi3ToDdi4(
                new Ddi3Response(null, ColecticaItems.toDdi3Items(dataRelationships)), Ddi4Response.SCHEMA);
            return ColecticaSetReader.withTopLevelReference(
                response, setReader.findTopLevelReference(itemResponses, PHYSICAL_INSTANCE));
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to fetch data relationships " + agencyId + "/" + id + "/" + version, e);
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
            ColecticaItemResponse[] dataRelationships = filterDataRelationshipsAndVariables(itemResponses);
            return ColecticaXml.assembleFragmentInstance(
                ColecticaItems.fragmentXmls(Arrays.stream(dataRelationships).toList()));
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to fetch data relationships XML " + agencyId + "/" + id + "/" + version, e);
        }
    }

    /**
     * Garde les fragments DataRelationship du set et les Variables référencées par leurs
     * {@code VariablesInRecord}/{@code VariableUsedReference} (#447). Dans un set de PhysicalInstance
     * les Variables sont exactement celles utilisées par les data relationships, un filtre sur le type
     * suffit donc. Les CodeList/Category référencées sont délibérément écartées.
     */
    private ColecticaItemResponse[] filterDataRelationshipsAndVariables(ColecticaItemResponse[] itemResponses) {
        Map<String, String> types = instanceConfiguration.itemTypes();
        String dataRelationshipType = types.get(DATA_RELATIONSHIP);
        String variableType = types.get(VARIABLE);
        return Arrays.stream(itemResponses)
            .filter(item -> Objects.equals(item.itemType(), dataRelationshipType)
                || Objects.equals(item.itemType(), variableType))
            .toArray(ColecticaItemResponse[]::new);
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
