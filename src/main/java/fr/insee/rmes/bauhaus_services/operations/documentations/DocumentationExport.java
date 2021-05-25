package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

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
import fr.insee.rmes.utils.XsltUtils;

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
			String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyMas, targetType);
			xmlContent.put("parametersFile", parametersXML);

			//transform
			XsltUtils.xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream, tempDir);

			// create odt
			XsltUtils.createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);

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

}
