package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConverter;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class DDIItemConvertServiceImpl implements DDIItemConvertService {

    private final List<DDIItemConverter> converters;
    private final DDI3toDDI4ConverterService ddi3ToDdi4ConverterService;
    private final Map<String, String> itemTypes;
    private final DocumentBuilderFactory documentBuilderFactory;
    private final ObjectMapper objectMapper;

    public DDIItemConvertServiceImpl(
            List<DDIItemConverter> converters,
            DDI3toDDI4ConverterService ddi3ToDdi4ConverterService,
            Map<String, String> itemTypes,
            ObjectMapper objectMapper) {
        this.converters = converters;
        this.ddi3ToDdi4ConverterService = ddi3ToDdi4ConverterService;
        this.itemTypes = itemTypes;
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(true);
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode convert(String xmlFragment) {
        String itemLocalName = resolveItemLocalName(xmlFragment);
        return converters.stream()
                .filter(c -> c.supports(itemLocalName))
                .findFirst()
                .map(c -> c.convert(xmlFragment))
                .orElseGet(() -> convertViaSchema(itemLocalName, xmlFragment));
    }

    /**
     * Fallback générique pour les types non couverts par un {@link DDIItemConverter} dédié
     * (CodeList, Category, Variable, DataRelationship, …) : on reconstruit un {@link Ddi3Response}
     * mono-item et on délègue à la conversion DDI3→DDI4 par schéma, déjà éprouvée par le GET PI et
     * {@code getMutualizedCodesList}. Le DDI4 obtenu est sérialisé en JSON.
     */
    private JsonNode convertViaSchema(String itemLocalName, String xmlFragment) {
        String itemType = itemTypes.get(itemLocalName);
        if (itemType == null) {
            throw new IllegalArgumentException("No DDI item converter for type: " + itemLocalName);
        }
        Ddi3Response.Ddi3Item item = new Ddi3Response.Ddi3Item(
                itemType, null, null, null, xmlFragment, null, null, false, false, false, null);
        Ddi3Response ddi3 = new Ddi3Response(null, List.of(item));
        Ddi4Response ddi4 = ddi3ToDdi4ConverterService.convertDdi3ToDdi4(ddi3, Ddi4Response.SCHEMA);
        return objectMapper.valueToTree(ddi4);
    }

    private String resolveItemLocalName(String xmlFragment) {
        try {
            Document doc =
                    documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlFragment)));
            Element root = doc.getDocumentElement();
            if ("Fragment".equals(root.getLocalName())) {
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        return child.getLocalName();
                    }
                }
            }
            return root.getLocalName();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML fragment to detect item type", e);
        }
    }
}
