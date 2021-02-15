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
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
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

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.utils.XMLUtils;

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
			String organizationsXML, String codeListsXML, String targetType, Boolean includeEmptyMas) throws RmesException, IOException  {
		logger.debug("Begin To export documentation");

		String msdXML = documentationsUtils.buildShellSims();

		List<String> languages = new ArrayList<String>();
		String parametersXML = buildParams(languages,includeEmptyMas,targetType);

		File output =  File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.FLAT_ODT));
		output.deleteOnExit();

		InputStream xslFile = getClass().getResourceAsStream("/xslTransformerFiles/sims2fodt_v6.xsl");
		OutputStream osOutputFile = FileUtils.openOutputStream(output);

		InputStream odtFile = getClass().getResourceAsStream("/xslTransformerFiles/rmesPattern.fodt");
		PrintStream printStream= null;

		try{
			// prepare transformer
			StreamSource xsrc = new StreamSource(xslFile);
			TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
			
			// set parameters as Strings
//			xsltTransformer.setParameter("Sims", simsXML);
//			xsltTransformer.setParameter("Organizations", organizationsXML);
//			xsltTransformer.setParameter("Operation", operationXML);
//			xsltTransformer.setParameter("Indicator", indicatorXML);
//			xsltTransformer.setParameter("Series", seriesXML);
//			xsltTransformer.setParameter("Msd", msdXML);
//			xsltTransformer.setParameter("CodeLists", codeListsXML);
//			xsltTransformer.setParameter("parameters", parametersXML);
			
			// Pass parameters in a file
			Path tempDir= Files.createTempDirectory("forExport");
			addParameter ( xsltTransformer,  "parametersFile",  parametersXML,tempDir);
			addParameter ( xsltTransformer,  "simsFile",  simsXML,tempDir);
			addParameter ( xsltTransformer,  "seriesFile",  seriesXML,tempDir);
			addParameter ( xsltTransformer,  "operationFile",  operationXML,tempDir);
			addParameter ( xsltTransformer,  "indicatorFile",  indicatorXML,tempDir);
			addParameter ( xsltTransformer,  "msdFile",  msdXML,tempDir);
			addParameter ( xsltTransformer,  "codeListsFile",  codeListsXML,tempDir);
			addParameter ( xsltTransformer,  "organizationsFile",  organizationsXML,tempDir);
			
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

	private String buildParams(List<String> languages, Boolean includeEmptyMas, String targetType) {
		String includeEmptyMasString=( includeEmptyMas ? "true" : "false");
		String parametersXML="";
	//	parametersXML=parametersXML.concat(Constants.XML_START_DOCUMENT);
		
		parametersXML=parametersXML.concat(Constants.XML_OPEN_PARAMETERS_TAG);

		parametersXML=parametersXML.concat(Constants.XML_OPEN_LANGUAGES_TAG);
		//		for(String language : languages) {
		//			parametersXML=parametersXML.concat(Constants.XML_OPEN_LANGUAGE_TAG);
		//			parametersXML=parametersXML.concat(Constants.XML_END_LANGUAGE_TAG);
		//		}
		parametersXML=parametersXML.concat("<language id=\"Fr\">1</language>\r\n<language id=\"En\">2</language>");
		parametersXML=parametersXML.concat(Constants.XML_END_LANGUAGES_TAG);

		parametersXML=parametersXML.concat(Constants.XML_OPEN_INCLUDE_EMPTY_MAS_TAG);
		parametersXML=parametersXML.concat(includeEmptyMasString);
		parametersXML=parametersXML.concat(Constants.XML_END_INCLUDE_EMPTY_MAS_TAG);

		parametersXML=parametersXML.concat(Constants.XML_OPEN_TARGET_TYPE_TAG);
		parametersXML=parametersXML.concat(targetType);
		parametersXML=parametersXML.concat(Constants.XML_END_TARGET_TYPE_TAG);

		parametersXML=parametersXML.concat(Constants.XML_END_PARAMETERS_TAG);
		return XMLUtils.encodeXml(parametersXML);
		
		// return XMLUtils.convertStringToDocument(parametersXML).toString();
		
		/*	
 		InputStream parametersXMLFile = getClass().getResourceAsStream("/xslTransformerFiles/parameters.xml");
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance(); 
        domFactory.setIgnoringComments(true);
		 DocumentBuilder docBuilder;
	     Document doc = null;
		try {
			docBuilder = domFactory.newDocumentBuilder();
			doc = docBuilder.parse(parametersXMLFile);
	         Node root=doc.getFirstChild();
	         Element targetTypeNode=doc.createElement("targetType");
	         targetTypeNode.setNodeValue(targetType);
	         Element includeEmptyMasNode=doc.createElement("includeEmptyMas");
	         includeEmptyMasNode.setNodeValue(includeEmptyMasString);
	         root.appendChild(targetTypeNode);
	         root.appendChild(includeEmptyMasNode);
		} catch (Exception e) {
			e.printStackTrace();
		}        
		try {
			parametersXML = IOUtils.toString(parametersXMLFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("Failed to read the xml : ", e);
		}
		 */		
	}

	private void addParameter (Transformer xsltTransformer, String paramName, String paramData, Path tempDir) throws IOException {
		// Pass parameters in a file
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };
		Path tempFile = Files.createTempFile(tempDir, paramName,Constants.DOT_XML);
		String absolutePath = tempFile.toFile().getAbsolutePath();
		InputStream is = IOUtils.toInputStream(paramData, StandardCharsets.UTF_8);
		Files.copy(is, tempFile, options);
		absolutePath = absolutePath.replace('\\', '/');
		xsltTransformer.setParameter(paramName, absolutePath);			
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
