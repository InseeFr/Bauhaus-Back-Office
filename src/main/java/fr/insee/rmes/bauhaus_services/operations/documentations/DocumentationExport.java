package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.utils.FilesUtils;
import fr.insee.rmes.utils.XMLUtils;

@Component
public class DocumentationExport {

	@Autowired
	private DocumentationsUtils documentationsUtils;

	private static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);

	public Response export(Map<String, String> xmlContent, String targetType, boolean includeEmptyMas, boolean lg1,
			boolean lg2, String goal) throws RmesException {
		logger.debug("Begin To export documentation");

		File output = null;
		String fileName = "export.odt";
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		InputStream odtFileIS = null;
		InputStream xslFileIS = null;
		InputStream zipToCompleteIS = null;

		try {
			xslFileIS = getClass().getResourceAsStream("/xslTransformerFiles/sims2fodt.xsl");

			if (Constants.GOAL_RMES.equals(goal)) {
				odtFileIS = getClass().getResourceAsStream("/xslTransformerFiles/rmesPatternContent.xml");
				zipToCompleteIS = getClass().getResourceAsStream("/xslTransformerFiles/toZipForRmes/export.zip");

			}
			if (Constants.GOAL_COMITE_LABEL.equals(goal)) {
				odtFileIS = getClass().getResourceAsStream("/xslTransformerFiles/labelPatternContent.xml");
				zipToCompleteIS = getClass().getResourceAsStream("/xslTransformerFiles/toZipForLabel/export.zip");
			}

			// prepare output
			output = File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.XML));
			output.deleteOnExit();
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
		} 

		try (OutputStream osOutputFile = FileUtils.openOutputStream(output);
				PrintStream printStream = new PrintStream(osOutputFile);) {

			Path tempDir = Files.createTempDirectory("forExport");
			Path finalPath = Paths.get(tempDir.toString() + "/" + fileName);
			
			//Add two params to xmlContents
			String msdXML = documentationsUtils.buildShellSims();
			xmlContent.put("msdFile", msdXML);
			String parametersXML = buildParams(lg1, lg2, includeEmptyMas, targetType);
			xmlContent.put("parametersFile", parametersXML);

			//transform
			xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream, tempDir);

			// create odt
			createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);

			logger.debug("End To export documentation");

			return Response.ok((StreamingOutput) out -> {
				InputStream input = Files.newInputStream(finalPath);
				IOUtils.copy(input, out);
				out.flush();
				input.close();
				out.close();
			}).header("Content-Disposition", content).build();
		} catch (IOException | TransformerException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					e.getClass().getSimpleName());
		} finally {
			try {
				if (odtFileIS != null)
					odtFileIS.close();
				if (xslFileIS != null)
					xslFileIS.close();
			} catch (IOException ioe) {
				logger.error(ioe.getMessage());
			}
		}
	}

	public void xsltTransform(Map<String, String> xmlContent, InputStream odtFileIS, InputStream xslFileIS,
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

	private void createOdtFromXml(File output, Path finalPath, InputStream zipToCompleteIS, Path tempDir)
			throws IOException {
		Path contentPath = Paths.get(tempDir.toString() + "/content.xml");
		Files.copy(Paths.get(output.getAbsolutePath()), contentPath, StandardCopyOption.REPLACE_EXISTING);
		Path zipPath = Paths.get(tempDir.toString() + "/export.zip");
		Files.copy(zipToCompleteIS, zipPath, StandardCopyOption.REPLACE_EXISTING);
		FilesUtils.addFileToZipFolder(contentPath.toFile(), zipPath.toFile());
		Files.copy(zipPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
	}

	private String buildParams(Boolean lg1, Boolean lg2, Boolean includeEmptyMas, String targetType) {
		String includeEmptyMasString = (Boolean.TRUE.equals(includeEmptyMas) ? "true" : "false");
		String parametersXML = "";

		parametersXML = parametersXML.concat(Constants.XML_OPEN_PARAMETERS_TAG);

		parametersXML = parametersXML.concat(Constants.XML_OPEN_LANGUAGES_TAG);
		if (Boolean.TRUE.equals(lg1))
			parametersXML = parametersXML.concat("<language id=\"Fr\">1</language>");
		if (Boolean.TRUE.equals(lg2))
			parametersXML = parametersXML.concat("<language id=\"En\">2</language>");
		parametersXML = parametersXML.concat(Constants.XML_END_LANGUAGES_TAG);

		parametersXML = parametersXML.concat(Constants.XML_OPEN_INCLUDE_EMPTY_MAS_TAG);
		parametersXML = parametersXML.concat(includeEmptyMasString);
		parametersXML = parametersXML.concat(Constants.XML_END_INCLUDE_EMPTY_MAS_TAG);

		parametersXML = parametersXML.concat(Constants.XML_OPEN_TARGET_TYPE_TAG);
		parametersXML = parametersXML.concat(targetType);
		parametersXML = parametersXML.concat(Constants.XML_END_TARGET_TYPE_TAG);

		parametersXML = parametersXML.concat(Constants.XML_END_PARAMETERS_TAG);
		return XMLUtils.encodeXml(parametersXML);
	}

	private void addParameter(Transformer xsltTransformer, String paramName, String paramData, Path tempDir)
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

}
