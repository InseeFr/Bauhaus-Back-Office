package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.external_services.export.XsltTransformer;

@Component
public class DocumentationExport {

	@Autowired
	private DocumentationsUtils documentationsUtils;

	private static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);

	private XsltTransformer saxonService = new XsltTransformer();

	public File export(File inputFile) throws Exception {
		InputStream isInputFile = FileUtils.openInputStream(inputFile);
		return export(isInputFile);
	}

	public File export(InputStream inputFile) throws Exception {
		logger.debug("Begin To export documentation");

		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("flatODT"));
		//File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("application/vnd.oasis.opendocument.text"));

		output.deleteOnExit();

		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);

		final PrintStream printStream = new PrintStream(osOutputFile);

		saxonService.transform(inputFile, xslFile, printStream);
		inputFile.close();
		xslFile.close();
		//osOutputFile.close();
		printStream.close();

		logger.debug("End To export documentation");
		return output;
	}

	public File transfoTest(InputStream inputFile) throws Exception  {
		logger.debug("Begin transformation test");
		File output =  File.createTempFile(Constants.OUTPUT, ".xml");
		output.deleteOnExit();
		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/transfoXSLT.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);
		PrintStream printStream = new PrintStream(osOutputFile);
		StreamSource xsrc = new StreamSource(xslFile);
		//TransformerFactory transformerFactory = net.sf.saxon.TransformerFactoryImpl.newInstance();	
		TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();	

		Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
		xsltTransformer.transform(new StreamSource(inputFile), new StreamResult(printStream));
		inputFile.close();
		xslFile.close();
		osOutputFile.close();
		printStream.close();
		logger.debug("End transformation test");
		return output;	
	}

	public File convertRichText(InputStream inputFile) throws IOException {

		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("flatODT"));
		output.deleteOnExit();
		
		OutputStream osOutputFile = FileUtils.openOutputStream(output);
		//InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/convertXhtmlToFodt.xsl");
		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/convertRichText.xsl");

		PrintStream printStream= null;

		try{
			printStream = new PrintStream(osOutputFile);
			StreamSource xsrc = new StreamSource(xslFile);
			TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
			xsltTransformer.transform(new StreamSource(inputFile), new StreamResult(printStream));
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		} finally {
			inputFile.close();
			osOutputFile.close();
			printStream.close();
		}
		return output;
	}
	
	public File testExport() throws IOException {
		
		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("flatODT"));
		output.deleteOnExit();
		
		OutputStream osOutputFile = FileUtils.openOutputStream(output);
		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/convertRichText.xsl");

		PrintStream printStream= null;

		InputStream inputFile = getClass().getResourceAsStream("/testXML.xml");
		try{
			printStream = new PrintStream(osOutputFile);
			StreamSource xsrc = new StreamSource(xslFile);
			TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
			xsltTransformer.transform(new StreamSource(inputFile), new StreamResult(printStream));
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		} finally {
			inputFile.close();
			osOutputFile.close();
			printStream.close();
		}
		return output;
	}
		
	
	
	public File export(InputStream inputFile, 
			String absolutePath, String accessoryAbsolutePath, String organizationsAbsolutePath, 
			String codeListAbsolutePath, String targetType) throws RmesException, IOException  {
		logger.debug("Begin To export documentation");

		String msdXml = documentationsUtils.buildShellSims();
		File msdFile =  File.createTempFile("msdXml", ".xml");
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };

		InputStream is = new ByteArrayInputStream(msdXml.getBytes(StandardCharsets.UTF_8));
		Files.copy(is, msdFile.toPath(), options);

		String msdPath = msdFile.getAbsolutePath();

		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("flatODT"));
		//File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("application/vnd.oasis.opendocument.text"));
		output.deleteOnExit();

	
//		File outputIntermediate =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension("XML"));
//		outputIntermediate.deleteOnExit();
		
		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
	//	OutputStream osOutputFile = FileUtils.openOutputStream(outputIntermediate);
		OutputStream osOutputFile = FileUtils.openOutputStream(output);

		PrintStream printStream= null;

		try{
			printStream = new PrintStream(osOutputFile);
			StreamSource xsrc = new StreamSource(xslFile);
			TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);

			absolutePath = absolutePath.replace('\\', '/');
			accessoryAbsolutePath = accessoryAbsolutePath.replace('\\', '/');
			organizationsAbsolutePath = organizationsAbsolutePath.replace('\\', '/');
			msdPath = msdPath.replace('\\', '/');
			codeListAbsolutePath = codeListAbsolutePath.replace('\\', '/');

			xsltTransformer.setParameter("tempFile", absolutePath);
			xsltTransformer.setParameter("accessoryTempFile", accessoryAbsolutePath);
			xsltTransformer.setParameter("orga", organizationsAbsolutePath);
			xsltTransformer.setParameter("msd", msdPath);
			xsltTransformer.setParameter("codeList", codeListAbsolutePath);
			xsltTransformer.setParameter("targetType", targetType);

			xsltTransformer.transform(new StreamSource(inputFile), new StreamResult(printStream));
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		} finally {
			inputFile.close();
			xslFile.close();
			osOutputFile.close();
			printStream.close();
		}
		logger.debug("End To export documentation");
//		return convertRichText(new FileInputStream(outputIntermediate));
		return(output);
	}

}
