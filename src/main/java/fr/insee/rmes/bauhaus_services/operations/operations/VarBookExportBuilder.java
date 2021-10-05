package fr.insee.rmes.bauhaus_services.operations.operations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.utils.DocumentBuilders;
import fr.insee.rmes.utils.StringUtils;
import fr.insee.rmes.utils.XMLUtils;

@Component
public class VarBookExportBuilder {

	private static final String REFERENCE = "Reference";

	static final Logger logger = LogManager.getLogger(VarBookExportBuilder.class);


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
			DocumentBuilder builder = DocumentBuilders.createSaferDocumentBuilder(DocumentBuilderFactory::isIgnoringElementContentWhitespace);
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
			if (!parentNode.getNodeName().endsWith(REFERENCE)) {
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
			removeNamespaces(node, document);
			rename(node, document);
			dereference(node, targets, document);
		}
	}

	private static void rename(Node node, Document document) {
		int nbAtt = 0;
		while (node.getAttributes().getLength() > nbAtt) {
			Node att = node.getAttributes().item(nbAtt);
			
			if (StringUtils.stringContainsItemFromList(att.getNodeName(), new String[] {"lang","Length", "regExp", "blank", "scale"})) {
				document.renameNode(att, null, att.getNodeName().replaceFirst("[a-z]*:", ""));
				nbAtt++;
			} else {
				node.getAttributes().removeNamedItem(att.getNodeName());
			}
		}
	}

	private static void dereference(Node node, Map<String, Node> targets, Document document) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (node.getNodeName().endsWith(REFERENCE) && childNode.getNodeName().endsWith("ID")) {
				Node targetNode = document.importNode(targets.get(childNode.getTextContent()), true);
				node.getParentNode().replaceChild(targetNode, node);
				renameAndDereference(targetNode, targets);
			} else if (!node.getNodeName().endsWith(REFERENCE) && childNode.getNodeType() == Node.ELEMENT_NODE) {
				// calls this method for all the children which is Element
				renameAndDereference(childNode, targets);
			}
		}
	}

	private static void removeNamespaces(Node node, Document document) {
		if (node.getNodeName().contains(":")) {
			document.renameNode(node, null, node.getNodeName().replaceFirst("[a-z]*:", ""));
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
	 * @throws RmesNotAcceptableException 
	 * @throws RmesException 
	 */
	private static Document addSortedVariableList(Document xmlInput) throws RmesNotAcceptableException {
		if (xmlInput == null) {
			return null;
		}
		// initialize document with DDIInstance + new root for variables
		Document xmlOutput = xmlInput;
		Element rootElem = xmlOutput.createElement("rootListVar");

		// copy all variables
		NodeList list = xmlInput.getElementsByTagName("RepresentedVariable");
		Map<String, Node> sortedList = new TreeMap<>();
		String lastVarOk = "";

		for (int i = 0; i < list.getLength(); i++) {
			Node variableNode = list.item(i);
			if (variableNode == null) throw new RmesNotAcceptableException("One represented variable is null. Last variable ok is "+lastVarOk, "");
			String variableName ;
			try {
				variableName = getVariableName(variableNode);
			}catch(NullPointerException e) {
				throw new RmesNotAcceptableException("One represented variable has no RepresentedVariableName. Last variable ok is "+lastVarOk, "");
			}
			sortedList.put(variableName, variableNode);
			lastVarOk = variableName;
		}

		for (Entry<String, Node> entry : sortedList.entrySet()) {
			Node importNode = xmlOutput.importNode(entry.getValue(), true);
			rootElem.appendChild(importNode);
		}

		xmlOutput.getDocumentElement().appendChild(rootElem);
		return xmlOutput;
	}

	private static Document getDocument(String xml) throws RmesException {
		DocumentBuilder db;
		Document xmlInitial = null;

		try (InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
			db = DocumentBuilders.createSaferDocumentBuilder(DocumentBuilderFactory::isIgnoringElementContentWhitespace);
			xmlInitial = db.parse(stream);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					e.getClass() + " Can't parse xml");
		} 
		return xmlInitial;
	}

	private static String getVariableName(Node variableNode) {
		Node nameNode = XMLUtils.getChild(variableNode, "RepresentedVariableName");
		Node nameNodeString = XMLUtils.getChild(nameNode, "String");
		return nameNodeString.getFirstChild().getTextContent();
	}
}
