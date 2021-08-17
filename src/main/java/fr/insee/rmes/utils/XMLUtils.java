package fr.insee.rmes.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationJsonMixIn;
import fr.insee.rmes.model.operations.documentations.Documentation;

public class XMLUtils {

	private static final String AMP = "&amp;";
	static final Logger logger = LogManager.getLogger(XMLUtils.class);
	
	  private XMLUtils() {
		    throw new IllegalStateException("Utility class");
	}


	public static final String toString(Document xml)
			throws TransformerFactoryConfigurationError, TransformerException {
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
		} else {
			mapper = new ObjectMapper();
			// TODO : make it generic for all classes or change to
			// 'produceXmlResponse'
			mapper.addMixIn(Documentation.class, DocumentationJsonMixIn.class);
		}
		try {
			response = mapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return encodeXml(response);
	}

	public static String produceXMLResponse(Object obj) {
		ObjectMapper mapper = new XmlMapper();
		String response = "";
		try {
			response = mapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return encodeXml(response);
	}

	public static String produceEmptyXML() {
		return(Constants.XML_EMPTY_TAG);
	}
	
	public static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
		// disable resolving of external DTD entities
		factory.setAttribute(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		factory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlStr)));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public static List<String> getTagValues(String text, String tag) {
		final Pattern tagRegex = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">", Pattern.DOTALL);
		final List<String> tagValues = new ArrayList<>();
		final Matcher matcher = tagRegex.matcher(text);
		while (matcher.find()) {
			tagValues.add(matcher.group(1));
		}
		return tagValues;
	}

	public static String encodeXml(String response) {
		String ret = StringEscapeUtils.unescapeXml(response);
		ret = StringEscapeUtils.unescapeHtml4(ret);

		final String regex = "&";
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern.matcher(ret).replaceAll(AMP);
		
		final String regex2 = "&amp;amp;";
		final Pattern pattern2 = Pattern.compile(regex2, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern2.matcher(ret).replaceAll(AMP);
		
		final String regex3 = "&amp;gt;";
		final Pattern pattern3 = Pattern.compile(regex3, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern3.matcher(ret).replaceAll("&gt;");
		
		final String regex4 = "&amp;lt;";
		final Pattern pattern4 = Pattern.compile(regex4, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern4.matcher(ret).replaceAll("&lt;");
		
		final String regex5 = Constants.XML_ESPERLUETTE_REPLACEMENT;
		final Pattern pattern5 = Pattern.compile(regex5, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern5.matcher(ret).replaceAll(AMP);
		
		final String regex6 = Constants.XML_SUP_REPLACEMENT;
		final Pattern pattern6 = Pattern.compile(regex6, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern6.matcher(ret).replaceAll("&gt;");
		
		final String regex7 = Constants.XML_INF_REPLACEMENT;
		final Pattern pattern7 = Pattern.compile(regex7, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern7.matcher(ret).replaceAll("&lt;");
		
		
		return new String(ret.getBytes(), StandardCharsets.UTF_8);
	}

	public static String solveSpecialXmlcharacters(String rubric) {
		String ret = rubric.replace("&quot;", Constants.XML_ESPERLUETTE_REPLACEMENT + "quot;"); //Quotes are not authorized in Json
		ret = StringEscapeUtils.unescapeXml(ret);
		ret = StringEscapeUtils.unescapeHtml4(ret);
		//ret=rubric
		
		final String regex = "&";
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern.matcher(ret).replaceAll(Constants.XML_ESPERLUETTE_REPLACEMENT);

		final String regex2 = "<";
		final Pattern pattern2 = Pattern.compile(regex2, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern2.matcher(ret).replaceAll(Constants.XML_INF_REPLACEMENT);

		final String regex3 = ">";
		final Pattern pattern3 = Pattern.compile(regex3, Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		ret = pattern3.matcher(ret).replaceAll(Constants.XML_SUP_REPLACEMENT);
		
		return new String(ret.getBytes(), StandardCharsets.UTF_8);
	}
	
}
