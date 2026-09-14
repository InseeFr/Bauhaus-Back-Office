package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_MISSING_VALUES_REPRESENTATION;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.MANAGED_REPRESENTATION_SCHEME;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Valeurs sentinelles réutilisables d'un groupe (#1566) : les ManagedMissingValuesRepresentations
 * rangées dans les ManagedRepresentationSchemes de ses LogicalProducts.
 */
class ColecticaMissingValuesRepository {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaMissingValuesRepository.class);

    private final ColecticaClient colecticaClient;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final ColecticaHierarchyBrowser hierarchy;

    ColecticaMissingValuesRepository(
            ColecticaClient colecticaClient,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            ColecticaHierarchyBrowser hierarchy) {
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.hierarchy = hierarchy;
    }

    /**
     * Même descente que {@code getMissingCodesListsByGroup} mais arrêtée au niveau MMVR ; le fragment
     * de chaque MMVR est ensuite récupéré et parsé pour son libellé, et la CodeList de sentinelles
     * qu'elle référence est chargée une fois pour construire l'aperçu de codes affiché par le
     * sélecteur de réutilisation.
     */
    List<PartialMissingValuesRepresentation> getMissingValuesRepresentationsByGroup(String agencyId, String groupId) {
        logger.info("Fetching reusable missing values representations for group {}/{}", agencyId, groupId);
        List<ItemReference> refs = hierarchy.descendFromGroup(
                agencyId,
                groupId,
                List.of(LOGICAL_PRODUCT, MANAGED_REPRESENTATION_SCHEME, MANAGED_MISSING_VALUES_REPRESENTATION));
        if (refs.isEmpty()) {
            return List.of();
        }

        Map<String, List<String>> codeValuesByCodeListKey = new HashMap<>();
        List<PartialMissingValuesRepresentation> result = new ArrayList<>();
        for (ItemReference ref : refs) {
            Ddi4ManagedMissingValuesRepresentation mmvr = readMissingValues(ref.agencyId(), ref.identifier());
            Reference codeListRef = firstSentinelCodeListReference(mmvr);
            result.add(new PartialMissingValuesRepresentation(
                    mmvr.id(),
                    mmvr.agency(),
                    mmvr.version(),
                    ColecticaLabels.firstValue(mmvr.label()),
                    codeListRef != null ? codeListRef.id() : null,
                    codeListRef != null
                            ? codeValuesByCodeListKey.computeIfAbsent(
                                    codeListRef.agency() + "/" + codeListRef.id(), _ -> sentinelCodeValues(codeListRef))
                            : List.of()));
        }
        return result;
    }

    private Reference firstSentinelCodeListReference(Ddi4ManagedMissingValuesRepresentation mmvr) {
        if (mmvr.missingCodeRepresentation() == null) {
            return null;
        }
        return mmvr.missingCodeRepresentation().stream()
                .map(CodeRepresentation::codeListReference)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<String> sentinelCodeValues(Reference codeListRef) {
        Ddi4CodeList codeList = readCodeList(new ItemReference(codeListRef.agency(), codeListRef.id()));
        return codesOf(codeList).stream()
                .map(Code::value)
                .filter(Objects::nonNull)
                .map(ValueType::stringValue)
                .toList();
    }

    private static List<Code> codesOf(Ddi4CodeList codeList) {
        return codeList.code() != null ? codeList.code() : List.of();
    }

    private Ddi4ManagedMissingValuesRepresentation readMissingValues(String agencyId, String id) {
        return ddi3ToDdi4Converter.toManagedMissingValuesRepresentation(
                colecticaClient.getItem(agencyId, id, null).item());
    }

    private Ddi4CodeList readCodeList(ItemReference ref) {
        return ddi3ToDdi4Converter.toCodeList(itemXml(ref));
    }

    private String itemXml(ItemReference ref) {
        return colecticaClient.getItem(ref.agencyId(), ref.identifier(), null).item();
    }
}
