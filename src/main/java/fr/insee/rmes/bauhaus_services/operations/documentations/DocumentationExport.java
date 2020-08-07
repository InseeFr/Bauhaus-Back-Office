package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.external_services.export.XsltTransformer;
import net.sf.saxon.TransformerFactoryImpl;

@Component
public class DocumentationExport {

	private static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);

	private XsltTransformer saxonService = new XsltTransformer();

		public File export(File inputFile) throws Exception {
		InputStream isInputFile = FileUtils.openInputStream(inputFile);
		return export(isInputFile);
	}
		
		public File export(InputStream inputFile) throws Exception {
		logger.debug("Begin To export documentation");

		File output =  File.createTempFile("output", ExportUtils.getExtension("flatODT"));
		//File output =  File.createTempFile("output", ExportUtils.getExtension("application/vnd.oasis.opendocument.text"));

		output.deleteOnExit();

		InputStream XSL_FILE = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);

		final PrintStream printStream = new PrintStream(osOutputFile);
		
		saxonService.transform(inputFile, XSL_FILE, printStream);
		inputFile.close();
		XSL_FILE.close();
		//osOutputFile.close();
		printStream.close();
		
		logger.debug("End To export documentation");
		return output;
	}


		public File export(InputStream inputFile, Path tempDir) throws Exception {
			logger.debug("Begin To export documentation");
			
			File output =  File.createTempFile("output", ExportUtils.getExtension("flatODT"));
			//File output =  File.createTempFile("output", ExportUtils.getExtension("application/vnd.oasis.opendocument.text"));

			output.deleteOnExit();

			InputStream XSL_FILE = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
			OutputStream osOutputFile = FileUtils.openOutputStream(output);

			final PrintStream printStream = new PrintStream(osOutputFile);
			
			StreamSource xsrc = new StreamSource(XSL_FILE);
			TransformerFactory transformerFactory = TransformerFactoryImpl.newInstance();
			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
			xsltTransformer.setParameter("tempDir", tempDir);
			
			xsltTransformer.transform(new StreamSource(inputFile), new StreamResult(printStream));
			
		//	saxonService.transform(inputFile, XSL_FILE, printStream);
			inputFile.close();
			XSL_FILE.close();
			//osOutputFile.close();
			printStream.close();
			
			logger.debug("End To export documentation");
			return output;
		}
		
}
