package fr.insee.rmes.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;

public class XsltUtils {

	private static final Logger logger = LoggerFactory.getLogger(XsltUtils.class);

	

	public static void xsltTransform(Map<String, String> xmlContent, InputStream odtFileIS, InputStream xslFileIS,
			PrintStream printStream, Path tempDir) throws TransformerException {
		// prepare transformer
		StreamSource xsrc = new StreamSource(xslFileIS);
		TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
		transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);

		// Pass parameters in a file to the transformer
		xmlContent.forEach((paramName, xmlData) -> {
			try {
				addParameter(xsltTransformer, paramName, xmlData, tempDir);
			} catch (RmesException e) {
				logger.error(e.getMessageAndDetails2());
			}
		});

		// transformation
		xsltTransformer.transform(new StreamSource(odtFileIS), new StreamResult(printStream));
	}
	

	private static void addParameter(Transformer xsltTransformer, String paramName, String paramData, Path tempDir)
			throws RmesException {
		// Pass parameters in a file
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };
		try {
			Path tempFile = Files.createTempFile(tempDir, paramName, Constants.DOT_XML);
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
	
	public static void createOdtFromXml(File output, Path finalPath, InputStream zipToCompleteIS, Path tempDir)
			throws IOException {
		Path contentPath = Paths.get(tempDir.toString() + "/content.xml");
		Files.copy(Paths.get(output.getAbsolutePath()), contentPath, StandardCopyOption.REPLACE_EXISTING);
		Path zipPath = Paths.get(tempDir.toString() + "/export.zip");
		Files.copy(zipToCompleteIS, zipPath, StandardCopyOption.REPLACE_EXISTING);
		FilesUtils.addFileToZipFolder(contentPath.toFile(), zipPath.toFile());
		Files.copy(zipPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
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
