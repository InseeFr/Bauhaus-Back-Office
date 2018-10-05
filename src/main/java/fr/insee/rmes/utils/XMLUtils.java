package fr.insee.rmes.utils;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLUtils {

	public static final String toString(Document xml) throws TransformerFactoryConfigurationError, TransformerException  {
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xml), new StreamResult(out));
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

}
