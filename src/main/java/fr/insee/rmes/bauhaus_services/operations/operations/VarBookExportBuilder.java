package fr.insee.rmes.bauhaus_services.operations.operations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.DocumentBuilders;
import fr.insee.rmes.utils.XMLUtils;

@Component
public class VarBookExportBuilder {

	static final Logger logger = LogManager.getLogger(VarBookExportBuilder.class);

	@Autowired
	RestTemplate restTemplate;

	public String getData(String xml) throws RmesException {
		Document xmlReadyToExport = transformXml(xml);
		try {
			return XMLUtils.toString(xmlReadyToExport);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new RmesException(HttpStatus.SC_BAD_REQUEST, e.getMessage(),
					"IOException - Can't convert xml to text");
		}
	}

	private Document transformXml(String xml) throws RmesException {
		// transform inputXml into Document
		Document xmlInput = getDocument(xml);
		xmlInput = dereferenceAll(xmlInput);
		return addSortedVariableList(xmlInput);
	}

	private Document dereferenceAll(Document xmlInput) throws RmesException {
		if (xmlInput == null) {
			return null;
		}
		
		// initialize document with DDIInstance + new root for variables
		NodeList alls = xmlInput.getElementsByTagName("StudyUnit");
		Node root = alls.item(0);
		Document xmlOutput = null;
		try {
			DocumentBuilder builder = DocumentBuilders.createSaferDocumentBuilder(factory -> {
			    factory.isIgnoringElementContentWhitespace();
			});
			xmlOutput = builder.newDocument();
			
		} catch (ParserConfigurationException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					"ParserConfigurationException");
		}

		Node copiedRoot = xmlOutput.importNode(root, true);
		xmlOutput.appendChild(copiedRoot);

		Map<String, Node> targets = listReferenceTargets(xmlInput);
		renameAndDereference(xmlOutput.getDocumentElement(), targets);// xmlOutput.getDocumentElement()

		return xmlOutput;
	}

	private Map<String, Node> listReferenceTargets(Document xmlInput) {
		Map<String, Node> targets = new HashMap<>();
		NodeList ids = xmlInput.getElementsByTagName("r:ID");
		for (int i = 0; i < ids.getLength(); i++) {
			Node idNode = ids.item(i);
			Node parentNode = idNode.getParentNode();
			if (!parentNode.getNodeName().endsWith("Reference")) {
				targets.put(idNode.getTextContent(), parentNode);
			}
		}
		return targets;
	}

	private static void renameAndDereference(Node node, Map<String, Node> targets) {
		// Remove namespace and keep only lang attr
		Document document = node.getOwnerDocument();
		if (node.getNodeName().contains("BasedOnObject")) {
			node.getParentNode().removeChild(node);
		} else {// Only for Node != BasedOnObject
			if (node.getNodeName().contains(":")) {
				document.renameNode(node, null, node.getNodeName().replaceFirst("[a-z]*:", ""));
			}
			int nbAtt = 0;
			while (node.getAttributes().getLength() > nbAtt) {
				Node att = node.getAttributes().item(nbAtt);
				if (att.getNodeName().contains("lang") || att.getNodeName().contains("Length")
						|| att.getNodeName().contains("regExp") || att.getNodeName().contains("blank")
						|| att.getNodeName().contains("scale")) {
					document.renameNode(att, null, att.getNodeName().replaceFirst("[a-z]*:", ""));
					nbAtt++;
				} else {
					node.getAttributes().removeNamedItem(att.getNodeName());
				}
			}

			// Dereference
			NodeList nodeList = node.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node childNode = nodeList.item(i);
				if (node.getNodeName().endsWith("Reference") && childNode.getNodeName().endsWith("ID")) {
					Node targetNode = document.importNode(targets.get(childNode.getTextContent()), true);
					node.getParentNode().replaceChild(targetNode, node);
					renameAndDereference(targetNode, targets);
				} else if (!node.getNodeName().endsWith("Reference") && childNode.getNodeType() == Node.ELEMENT_NODE) {
					// calls this method for all the children which is Element
					renameAndDereference(childNode, targets);
				}
			}
		}
	}

	/**
	 * Copy all variables from DDIStudyUnit to DDIInstance/rootListVar
	 * 1. Remove duplicate variable
	 * 2. Sort variables
	 * 
	 * 
	 * @param xml
	 * @return
	 * @throws RmesException 
	 */
	private static Document addSortedVariableList(Document xmlInput) throws RmesException {
		if (xmlInput == null) {
			return null;
		}
		// initialize document with DDIInstance + new root for variables
		Document xmlOutput = xmlInput;
		Element rootElem = xmlOutput.createElement("rootListVar");

		// copy all variables
		NodeList list = xmlInput.getElementsByTagName("RepresentedVariable");
		Map<String, Node> sortedList = new TreeMap<>();

		for (int i = 0; i < list.getLength(); i++) {
			Node variableNode = list.item(i);
			sortedList.put(getVariableName(variableNode), variableNode);
		}

		for (Entry<String, Node> entry : sortedList.entrySet()) {
			Node importNode = xmlOutput.importNode(entry.getValue(), true);
			rootElem.appendChild(importNode);
		}

		xmlOutput.getDocumentElement().appendChild(rootElem);
		return xmlOutput;
	}

	private static Document getDocument(String xml) throws RmesException {
		InputStream stream = null;
		DocumentBuilder db;
		Document xmlInitial = null;

		try {
			stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
			db = DocumentBuilders.createSaferDocumentBuilder(factory -> {
			    factory.isIgnoringElementContentWhitespace();
			});
			xmlInitial = db.parse(stream);
			stream.close();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					e.getClass() + " Can't parse xml");
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return xmlInitial;
	}

	private static String getVariableName(Node variableNode) {
		Node nameNode = XMLUtils.getChild(variableNode, "RepresentedVariableName");
		Node nameNodeString = XMLUtils.getChild(nameNode, "String");
		return nameNodeString.getFirstChild().getTextContent();
	}
}
