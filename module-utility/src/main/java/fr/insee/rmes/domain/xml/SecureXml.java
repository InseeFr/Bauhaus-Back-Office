package fr.insee.rmes.domain.xml;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Fabriques JAXP durcies contre les entités externes (XXE), pour tout XML dont la source n'est pas
 * de confiance — en pratique tout ce qui vient de Colectica.
 * <p>
 * Vit dans {@code fr.insee.rmes.domain} (module-utility) parce que ses appelants sont des deux
 * côtés de la frontière hexagonale : les adaptateurs {@code infrastructure} et le domaine.
 */
public final class SecureXml {

    private SecureXml() {}

    /** Fabrique de {@code DocumentBuilder} protégée contre les entités externes (XXE). */
    public static DocumentBuilderFactory documentBuilderFactory() throws ParserConfigurationException {
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
    public static TransformerFactory transformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        return factory;
    }

    /** Parse un document XML avec la fabrique durcie ci-dessus. */
    public static Document parse(String xml) throws ParserConfigurationException, SAXException, IOException {
        return documentBuilderFactory().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
