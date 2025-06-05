package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class XsltUtils {

	private static final Logger logger = LoggerFactory.getLogger(XsltUtils.class);

	  private XsltUtils() {
		    throw new IllegalStateException("Utility class");
	}


	public static void xsltTransform(Map<String, String> xmlContent, InputStream odtFileIS, InputStream xslFileIS,
									 OutputStream outputStream) throws TransformerException {
		StreamSource xsrc = new StreamSource(xslFileIS);
		Transformer xsltTransformer = XMLUtils.getTransformerFactory().newTransformer(xsrc);

		List<String> expectedParams = List.of(
				"series", "operation", "indicator", "sims",
				"organizations", "codeLists", "msd", "concepts",
				"collections", "parameters"
		);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		try {
			DocumentBuilder builder = dbf.newDocumentBuilder();

			for (String paramName : expectedParams) {
				String xmlData = xmlContent.getOrDefault(paramName, "<" + paramName + "/>");
				Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
				xsltTransformer.setParameter(paramName, new DOMSource(doc.getDocumentElement()));
			}
		} catch (Exception e) {
			throw new RuntimeException("Error setting XSLT parameters", e);
		}

		xsltTransformer.transform(new StreamSource(odtFileIS), new StreamResult(outputStream));
	}
	

	private static void addParameter(Transformer xsltTransformer, String paramName, String paramData, Path tempDir)
			throws RmesException {
		// Pass parameters in a file
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };
		try {
			Path tempFile = Files.createTempFile(tempDir, paramName, FilesUtils.XML_EXTENSION);
			String absolutePath = tempFile.toFile().getAbsolutePath();
			InputStream is = IOUtils.toInputStream(paramData, StandardCharsets.UTF_8);
			Files.copy(is, tempFile, options);
			absolutePath = absolutePath.replace('\\', '/');
			xsltTransformer.setParameter(paramName, absolutePath);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					"IOException - Can't create temp files for XSLT Transformer");
		}

	}

	public static ByteArrayOutputStream createOdtFromXml(byte[] transformedXml, InputStream zipTemplateIS)
			throws IOException {
		Map<String, byte[]> zipEntries = new HashMap<>();

		try (ZipInputStream zis = new ZipInputStream(zipTemplateIS)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.getName().equals("content.xml")) {
					zipEntries.put(entry.getName(), zis.readAllBytes());
				}
			}
		}

		ByteArrayOutputStream odtOutput = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(odtOutput)) {
			zos.putNextEntry(new ZipEntry("content.xml"));
			zos.write(transformedXml);
			zos.closeEntry();

			for (Map.Entry<String, byte[]> other : zipEntries.entrySet()) {
				zos.putNextEntry(new ZipEntry(other.getKey()));
				zos.write(other.getValue());
				zos.closeEntry();
			}
		}

		return odtOutput;
	}


	public static String buildParams(Boolean lg1, Boolean lg2, Boolean includeEmptyFields, String targetType) {
		String includeEmptyFieldsString = (Boolean.TRUE.equals(includeEmptyFields) ? "true" : "false");
		String parametersXML = "";

		parametersXML = parametersXML.concat(Constants.XML_OPEN_PARAMETERS_TAG);

		parametersXML = parametersXML.concat(Constants.XML_OPEN_LANGUAGES_TAG);
		if (Boolean.TRUE.equals(lg1))
			parametersXML = parametersXML.concat("<language id=\"Fr\">1</language>");
		if (Boolean.TRUE.equals(lg2))
			parametersXML = parametersXML.concat("<language id=\"En\">2</language>");
		parametersXML = parametersXML.concat(Constants.XML_END_LANGUAGES_TAG);

		parametersXML = parametersXML.concat(Constants.XML_OPEN_INCLUDE_EMPTY_FIELDS_TAG);
		parametersXML = parametersXML.concat(includeEmptyFieldsString);
		parametersXML = parametersXML.concat(Constants.XML_END_INCLUDE_EMPTY_FIELDS_TAG);

		parametersXML = parametersXML.concat(Constants.XML_OPEN_TARGET_TYPE_TAG);
		parametersXML = parametersXML.concat(targetType);
		parametersXML = parametersXML.concat(Constants.XML_END_TARGET_TYPE_TAG);

		parametersXML = parametersXML.concat(Constants.XML_END_PARAMETERS_TAG);
		return XMLUtils.encodeXml(parametersXML);
	}

}
