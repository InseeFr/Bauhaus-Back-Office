package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.xml.transform.OutputKeys;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.external_services.export.XsltTransformer;

@Component
public class DocumentationExport {

	private static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);

	private XsltTransformer saxonService = new XsltTransformer();

		public File export(File inputFile) throws Exception {
		InputStream isInputFile = FileUtils.openInputStream(inputFile);
		return export(isInputFile);
	}
		
		public File export(InputStream isInputFile) throws Exception {
		logger.debug("Begin To export documentation");

		File output =  File.createTempFile("output", ExportUtils.getExtension("flatODT"));
		//File output =  File.createTempFile("output", ExportUtils.getExtension("application/vnd.oasis.opendocument.text"));

		output.deleteOnExit();

		InputStream XSL_FILE = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);

		final PrintStream printStream = new PrintStream(osOutputFile);
		
		saxonService.transform(isInputFile, XSL_FILE, printStream);
		isInputFile.close();
		XSL_FILE.close();
		//osOutputFile.close();
		printStream.close();
		
		logger.debug("End To export documentation");
		//return fr.insee.rmes.utils.FileUtils.zipFile(output);
		return output;
	}
}
