package fr.insee.rmes.persistance.service.sesame.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.export.Jasper;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.utils.XMLUtils;

@Service
public class OperationsImpl implements OperationsService {

	final static Logger logger = LogManager.getLogger(OperationsImpl.class);
	
	@Autowired
	Jasper jasper;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public List<SimpleObjectForList> getSeries() throws Exception {
		String url = String.format("%s/api/search/series", Config.BASE_URI_METADATA_API);
		ResponseEntity<SimpleObjectForList[]> seriesRes = restTemplate.exchange(url, HttpMethod.GET, null,
				SimpleObjectForList[].class);
		logger.info("GET Series");
		return Arrays.asList(seriesRes.getBody());
	}

	@Override
	public List<SimpleObjectForList> getOperations() throws Exception {
		String url = String.format("%s/api/search/operations", Config.BASE_URI_METADATA_API);
		ResponseEntity<SimpleObjectForList[]> res = restTemplate.exchange(url, HttpMethod.GET, null,
				SimpleObjectForList[].class);
		logger.info("GET Operations");
		return Arrays.asList(res.getBody());
	}

	@Override
	public String getDataForVarBook(String operationId) throws Exception {
		String url = String.format("%s/api/meta-data/operation/%s/variableBook", Config.BASE_URI_METADATA_API,
				operationId);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> seriesRes = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		logger.info("GET data for variable book");
		return seriesRes.getBody();
	}

	@Override
	public Response getVarBookExport(String id, String acceptHeader) throws Exception {
		String xml = getDataForVarBook(id);
		Document xmlDocForJasper = addSortedVariableList(xml);
		String xmlForJasper = XMLUtils.toString(xmlDocForJasper);
		InputStream is = jasper.exportVariableBook(xmlForJasper, acceptHeader);
		String fileName = "Dico" + id + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader).header("Content-Disposition", content).build();
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
