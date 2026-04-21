package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class DDIItemConvertServiceImpl implements DDIItemConvertService {

    private final List<DDIItemConverter> converters;
    private final XmlHelper xmlHelper;

    public DDIItemConvertServiceImpl(List<DDIItemConverter> converters) {
        this.converters = converters;
        this.xmlHelper = new XmlHelper();
    }

    @Override
    public JsonNode convert(String xmlFragment) {
        String itemLocalName = resolveItemLocalName(xmlFragment);
        return converters.stream()
                .filter(c -> c.supports(itemLocalName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No DDI item converter for type: " + itemLocalName))
                .convert(xmlFragment);
    }

    private String resolveItemLocalName(String xmlFragment) {
        try {
            Document doc = xmlHelper.parseXml(xmlFragment);
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
