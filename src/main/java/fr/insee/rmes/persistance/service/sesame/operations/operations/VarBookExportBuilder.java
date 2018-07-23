package fr.insee.rmes.persistance.service.sesame.operations.operations;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.utils.XMLUtils;

@Component
public class VarBookExportBuilder {

	final static Logger logger = LogManager.getLogger(VarBookExportBuilder.class);

	@Autowired
	RestTemplate restTemplate;


	public String getData(String id) throws Exception, ParserConfigurationException, SAXException, IOException {
		String xml = getDataForVarBook(id);
		Document xmlDocForJasper = addSortedVariableList(xml);
		return XMLUtils.toString(xmlDocForJasper);
	}

	/**
	 * Call DDI Access Service
	 * @param operationId
	 * @return
	 * @throws Exception
	 */
	private String getDataForVarBook(String operationId) throws Exception {
		String url = String.format("%s/api/meta-data/operation/%s/variableBook", Config.BASE_URI_METADATA_API,
				operationId);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> seriesRes = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		logger.info("GET data for variable book");
		return seriesRes.getBody();
	}


	/**
	 * Copy all variables from DDIStudyUnit to DDIInstance/rootListVar
	 * 1. Remove duplicate variable
	 * 2. Sort variables
	 * 
	 * 
	 * @param xml
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document addSortedVariableList(String xml)
			throws ParserConfigurationException, SAXException, IOException {

		// transform inputXml into Document
		InputSource inputXml = new InputSource(new StringReader(xml));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xmlInitial = db.parse(inputXml);

		// initialize document with DDIInstance + new root for variables
		Document xmlListVar = xmlInitial;
		Element rootElem = xmlListVar.createElement("rootListVar");

		// copy all variables
		NodeList list = xmlInitial.getElementsByTagName("RepresentedVariable");
		Map<String, Node> sortedList = new TreeMap<String, Node>();

		for (int i = 0; i < list.getLength(); i++) {
			Node variableNode = list.item(i);
			sortedList.put(getVariableName(variableNode), variableNode);
		}

		for (Entry<String, Node> entry : sortedList.entrySet()) {
			Node importNode = xmlListVar.importNode(entry.getValue(), true);
			rootElem.appendChild(importNode);
		}

		xmlListVar.getDocumentElement().appendChild(rootElem);
		return xmlListVar;
	}

	private static String getVariableName(Node variableNode) {
		Node nameNode = XMLUtils.getChild(variableNode, "RepresentedVariableName");
		Node nameNodeString = XMLUtils.getChild(nameNode, "r:String");
		String name = nameNodeString.getFirstChild().getNodeValue();
		return name;
	}
}
