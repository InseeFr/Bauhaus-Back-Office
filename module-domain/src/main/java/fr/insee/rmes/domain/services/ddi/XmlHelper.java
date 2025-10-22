package fr.insee.rmes.domain.services.ddi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Helper class for XML manipulation using DOM API
 */
public class XmlHelper {

    private static final String DDI_INSTANCE_NS = "ddi:instance:3_3";
    private static final String DDI_REUSABLE_NS = "ddi:reusable:3_3";
    private static final String DDI_PHYSICAL_INSTANCE_NS = "ddi:physicalinstance:3_3";
    private static final String DDI_LOGICAL_PRODUCT_NS = "ddi:logicalproduct:3_3";

    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;

    public XmlHelper() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(true);
        this.transformerFactory = TransformerFactory.newInstance();
    }

    /**
     * Parse XML string to Document
     */
    public Document parseXml(String xml) throws Exception {
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Convert Document to String
     */
    public String documentToString(Document doc) throws Exception {
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Create a new Document
     */
    public Document createDocument() throws Exception {
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        return builder.newDocument();
    }

    /**
     * Get text content of a child element by tag name
     */
    public String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Get text content with namespace
     */
    public String getElementTextNS(Element parent, String namespace, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return "";
    }

    /**
     * Get first child element by tag name
     */
    public Element getChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    /**
     * Get attribute value
     */
    public String getAttribute(Element element, String attributeName) {
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        return "";
    }

    /**
     * Create element with namespace and text content
     */
    public Element createElement(Document doc, String namespace, String prefix, String localName, String textContent) {
        String qualifiedName = prefix.isEmpty() ? localName : prefix + ":" + localName;
        Element element = doc.createElementNS(namespace, qualifiedName);
        if (textContent != null && !textContent.isEmpty()) {
            element.setTextContent(textContent);
        }
        return element;
    }

    /**
     * Create element with attribute
     */
    public Element createElementWithAttribute(Document doc, String namespace, String prefix, String localName,
                                               String attributeName, String attributeValue) {
        Element element = createElement(doc, namespace, prefix, localName, null);
        element.setAttribute(attributeName, attributeValue);
        return element;
    }

    /**
     * Add child element with text content
     */
    public void addElementWithText(Element parent, String namespace, String prefix, String localName, String textContent) {
        if (textContent != null && !textContent.isEmpty()) {
            Document doc = parent.getOwnerDocument();
            Element element = createElement(doc, namespace, prefix, localName, textContent);
            parent.appendChild(element);
        }
    }

    public static String getDdiInstanceNs() {
        return DDI_INSTANCE_NS;
    }

    public static String getDdiReusableNs() {
        return DDI_REUSABLE_NS;
    }

    public static String getDdiPhysicalInstanceNs() {
        return DDI_PHYSICAL_INSTANCE_NS;
    }

    public static String getDdiLogicalProductNs() {
        return DDI_LOGICAL_PRODUCT_NS;
    }
}
