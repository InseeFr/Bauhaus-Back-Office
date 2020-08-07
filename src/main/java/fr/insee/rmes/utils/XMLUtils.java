package fr.insee.rmes.utils;

import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationJsonMixIn;
import fr.insee.rmes.model.operations.documentations.Documentation;



public class XMLUtils {

	static final Logger logger = LogManager.getLogger(XMLUtils.class);
	
	public static final String toString(Document xml) throws TransformerFactoryConfigurationError, TransformerException  {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		Writer out = new StringWriter();
		transformer.transform(new DOMSource(xml), new StreamResult(out));
		return out.toString();
	}

	public static Node getChild(Node parent, String childName) {
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (childName.equals(child.getNodeName())) {
				return child;
			}
		}
		return null;
	}

	public static String produceResponse(Object obj, String header) {
        ObjectMapper mapper = null;
        String response = "";

        if (header != null && header.equals(MediaType.APPLICATION_XML)) {
            mapper = new XmlMapper();
        }
        else {
            mapper = new ObjectMapper();
            mapper.addMixIn(Documentation.class, DocumentationJsonMixIn.class);
        }

        try {
            response = mapper.writeValueAsString(obj);

        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }

        return response;
    }
	
}
