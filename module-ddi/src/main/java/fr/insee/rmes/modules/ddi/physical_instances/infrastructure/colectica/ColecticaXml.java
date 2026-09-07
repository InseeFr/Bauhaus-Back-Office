package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Plomberie XML partagée par les adaptateurs Colectica : fabriques durcies contre XXE, conversions
 * DOM ↔ String et petits lecteurs de fragments DDI 3.3.
 */
final class ColecticaXml {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaXml.class);

    static final String REUSABLE_NS = "ddi:reusable:3_3";
    static final String GROUP_NS = "ddi:group:3_3";
    static final String STUDY_UNIT_NS = "ddi:studyunit:3_3";
    static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    private ColecticaXml() {
    }

    /** Fabrique de {@code DocumentBuilder} protégée contre les entités externes (XXE). */
    static DocumentBuilderFactory secureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        return factory;
    }

    /** Fabrique de {@code Transformer} protégée contre les entités externes (XXE). */
    static TransformerFactory secureTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        return factory;
    }

    static Document parse(String xml) throws ParserConfigurationException, SAXException, IOException {
        return secureDocumentBuilderFactory()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

    /** Sérialise un nœud DOM en XML, sans déclaration {@code <?xml …?>}. */
    static String toXmlString(Node node) throws TransformerException {
        Transformer transformer = secureTransformerFactory().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    /** Contenu textuel du premier descendant portant ce namespace et ce nom local, {@code null} sinon. */
    static String textContent(Element parent, String namespaceUri, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceUri, localName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    /** Contenus textuels non vides (nettoyés) de tous les descendants portant ce namespace et ce nom local. */
    static List<String> textContents(Element parent, String namespaceUri, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceUri, localName);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            String text = nodes.item(i).getTextContent();
            if (text != null && !text.isBlank()) {
                values.add(text.trim());
            }
        }
        return values;
    }

    /**
     * Les {@code r:UserID} portés par un fragment DDI 3.3 (IRI de série d'un Group, IRI d'opération
     * d'une StudyUnit). Liste vide si le XML est absent ou illisible — l'appelant décide s'il filtre
     * les valeurs vides.
     */
    static List<String> userIds(String xml) {
        if (xml == null || xml.isBlank()) {
            return List.of();
        }
        try {
            NodeList userIdNodes = parse(xml).getElementsByTagNameNS(REUSABLE_NS, "UserID");
            List<String> userIds = new ArrayList<>();
            for (int i = 0; i < userIdNodes.getLength(); i++) {
                String text = userIdNodes.item(i).getTextContent();
                if (text != null) {
                    userIds.add(text.trim());
                }
            }
            return userIds;
        } catch (Exception e) {
            logger.warn("Failed to parse XML to extract UserIDs", e);
            return List.of();
        }
    }

    /**
     * Les items désignés par les éléments de référence {@code r:<referenceLocalName>} d'un fragment
     * DDI 3.3 (par exemple les {@code r:PhysicalInstanceReference} d'une StudyUnit), prêts pour un
     * {@code item/_getList}. Les doublons sont écartés, ainsi que les références incomplètes ou dont
     * la version n'est pas un entier : Colectica ne saurait rien en faire. Liste vide quand le XML
     * est absent ou illisible.
     */
    static List<GetDescriptionsRequest.IdentifierRef> referencedIdentifiers(
        String xml, String referenceLocalName) {
        if (xml == null || xml.isBlank()) {
            return List.of();
        }
        try {
            NodeList references = parse(xml).getElementsByTagNameNS(REUSABLE_NS, referenceLocalName);
            List<GetDescriptionsRequest.IdentifierRef> identifiers = new ArrayList<>();
            for (int i = 0; i < references.getLength(); i++) {
                Element reference = (Element) references.item(i);
                String agency = trimmed(textContent(reference, REUSABLE_NS, "Agency"));
                String id = trimmed(textContent(reference, REUSABLE_NS, "ID"));
                String version = trimmed(textContent(reference, REUSABLE_NS, "Version"));
                if (agency == null || id == null || version == null || !version.matches("\\d+")) {
                    logger.warn("Ignoring unusable {}: agency={}, id={}, version={}",
                        referenceLocalName, agency, id, version);
                    continue;
                }
                identifiers.add(new GetDescriptionsRequest.IdentifierRef(
                    agency, id, Integer.parseInt(version)));
            }
            return identifiers.stream().distinct().toList();
        } catch (Exception e) {
            logger.warn("Failed to parse XML to extract {} identifiers", referenceLocalName, e);
            return List.of();
        }
    }

    private static String trimmed(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Lit l'attribut {@code versionDate} porté par le (premier) élément DDI du fragment d'un item.
     * {@code null} quand le XML est vide, illisible ou sans cet attribut.
     */
    static Date versionDate(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        try {
            NodeList elements = parse(xml).getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                String versionDate = ((Element) elements.item(i)).getAttribute("versionDate");
                if (versionDate != null && !versionDate.isBlank()) {
                    return ColecticaDates.parse(versionDate);
                }
            }
            return null;
        } catch (Exception e) {
            logger.warn("Failed to parse versionDate from item XML", e);
            return null;
        }
    }

    static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    /**
     * Retire les caractères invisibles ou de contrôle précédant le premier {@code <}. Colectica en
     * émet parfois (BOM notamment) en tête de ses réponses {@code ddiset}, ce qui ferait échouer le parse.
     */
    static String stripLeadingGarbage(String xml) {
        int startIndex = 0;
        while (startIndex < xml.length() && xml.charAt(startIndex) != '<') {
            char c = xml.charAt(startIndex);
            if (!(c == '\uFEFF' || Character.isWhitespace(c) || Character.isISOControl(c)
                || !Character.isDefined(c))) {
                logger.warn("Unexpected character at position {}: {} (code: {})", startIndex, c, (int) c);
            }
            startIndex++;
        }
        if (startIndex > 0) {
            logger.info("Removed {} leading characters from XML", startIndex);
        }
        return xml.substring(startIndex).trim();
    }

    /**
     * Emballe des documents {@code <Fragment>} DDI 3.3 (tels que renvoyés par Colectica) dans un unique
     * {@code <FragmentInstance>}. Chaque fragment déclare déjà ses namespaces : les déclarations XML de
     * tête sont retirées avant concaténation.
     */
    static String assembleFragmentInstance(List<String> fragmentXmls) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<ddi:FragmentInstance xmlns:ddi=\"ddi:instance:3_3\" xmlns:r=\"ddi:reusable:3_3\">\n");
        for (String fragmentXml : fragmentXmls) {
            sb.append(stripXmlDeclaration(fragmentXml)).append("\n");
        }
        sb.append("</ddi:FragmentInstance>");
        return sb.toString();
    }

    static String stripXmlDeclaration(String xml) {
        String trimmed = xml.stripLeading();
        if (trimmed.startsWith("<?xml")) {
            int end = trimmed.indexOf("?>");
            if (end >= 0) {
                return trimmed.substring(end + 2).stripLeading();
            }
        }
        return trimmed;
    }
}
