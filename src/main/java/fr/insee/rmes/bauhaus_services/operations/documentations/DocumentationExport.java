package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.export.ExportUtils;

@Component
public class DocumentationExport {

	@Autowired
	private DocumentationsUtils documentationsUtils;

	private static final Logger logger = LoggerFactory.getLogger(DocumentationExport.class);


	public File testExport() throws IOException {

		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.FLAT_ODT));
		output.deleteOnExit();
		OutputStream osOutputFile = FileUtils.openOutputStream(output);
		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/convertRichText.xsl");
		InputStream inputFile = getClass().getResourceAsStream("/testXML.xml");

		try(PrintStream printStream = new PrintStream(osOutputFile)	){
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
		}
		return output;
	}

	public File export(String simsXML,String operationXML,String indicatorXML,String seriesXML,
			String organizationsXML, String codeListsXML, String targetType) throws RmesException, IOException  {
		logger.debug("Begin To export documentation");

		String msdXML = documentationsUtils.buildShellSims();
		String parametersXML ="";
		InputStream parametersXMLFile = getClass().getResourceAsStream("/xslTransformerFiles/parameters.xml");
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setIgnoringComments(true);
		 DocumentBuilder docBuilder;
	     Document doc = null;
		try {
			docBuilder = domFactory.newDocumentBuilder();
			doc = docBuilder.parse(parametersXMLFile);
	         Node root=doc.getFirstChild();
	         Element newserver=doc.createElement("targetType");
	         newserver.setNodeValue(targetType);
	         root.appendChild(newserver);
		} catch (Exception e) {
			e.printStackTrace();
		}        
		
		try {
			parametersXML = IOUtils.toString(parametersXMLFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Failed to read the xml : ", e);
		}


		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.FLAT_ODT));
		output.deleteOnExit();

		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);

		InputStream odtFile = getClass().getResourceAsStream("/xslTransformerFiles/rmesPattern.fodt");
		PrintStream printStream= null;

		try{
			// prepare transformer
			StreamSource xsrc = new StreamSource(xslFile);
			TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
			// set parameters
			xsltTransformer.setParameter("simsXML", simsXML);
			xsltTransformer.setParameter("operationXML", operationXML);
			xsltTransformer.setParameter("indicatorXML", indicatorXML);
			xsltTransformer.setParameter("seriesXML", seriesXML);
			xsltTransformer.setParameter("msdXML", msdXML);
			xsltTransformer.setParameter("codeListsXML", codeListsXML);
			xsltTransformer.setParameter("parametersXML", doc.toString());
			// prepare output
			printStream = new PrintStream(osOutputFile);
			// transformation
			xsltTransformer.transform(new StreamSource(odtFile), new StreamResult(printStream));
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		} finally {
			odtFile.close();
			xslFile.close();
			osOutputFile.close();
			printStream.close();
		}
		logger.debug("End To export documentation");
		return(output);
	}

	public File exportOld(InputStream inputFile, 
			String absolutePath, String accessoryAbsolutePath, String organizationsAbsolutePath, 
			String codeListAbsolutePath, String targetType) throws RmesException, IOException  {
		logger.debug("Begin To export documentation");

		String msdXml = documentationsUtils.buildShellSims();
		File msdFile =  File.createTempFile("msdXml", ".xml");
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };

		InputStream is = new ByteArrayInputStream(msdXml.getBytes(StandardCharsets.UTF_8));
		Files.copy(is, msdFile.toPath(), options);

		String msdPath = msdFile.getAbsolutePath();

		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.FLAT_ODT));
		output.deleteOnExit();

		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/testXSLT.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);


		try(PrintStream printStream = new PrintStream(osOutputFile)	){

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
		}
		logger.debug("End To export documentation");
		return(output);
	}


}
