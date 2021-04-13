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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
	
	public Response export(String simsXML,String operationXML,String indicatorXML,String seriesXML,
			String organizationsXML, String codeListsXML, String targetType, 
			Boolean includeEmptyMas, Boolean lg1, Boolean lg2, String goal) throws RmesException, IOException  {
		logger.debug("Begin To export documentation");

		String msdXML = documentationsUtils.buildShellSims();
		String parametersXML = buildParams(lg1,lg2,includeEmptyMas,targetType);
		File output =   File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.XML));
		InputStream xslFileIS = getClass().getResourceAsStream("/xslTransformerFiles/sims2fodt.xsl");
		InputStream zipToCompleteIS = null;
		InputStream odtFileIS = null ;
		
		if(goal == Constants.GOAL_RMES){
			odtFileIS = getClass().getResourceAsStream("/xslTransformerFiles/rmesPatternContent.xml");
			zipToCompleteIS = getClass().getResourceAsStream("/xslTransformerFiles/toZipForRmes/export.zip");

		}
		if(goal == Constants.GOAL_COMITE_LABEL){
			odtFileIS = getClass().getResourceAsStream("/xslTransformerFiles/labelPatternContent.xml");
			zipToCompleteIS = getClass().getResourceAsStream("/xslTransformerFiles/toZipForLabel/export.zip");
		}	
		
		OutputStream osOutputFile = FileUtils.openOutputStream(output);
		output.deleteOnExit();
		PrintStream printStream= null;
		Path tempDir= Files.createTempDirectory("forExport");
		String fileName="export.odt";
		Path finalPath = Paths.get(tempDir.toString()+"/"+fileName);
		
		try{
			// prepare transformer
			StreamSource xsrc = new StreamSource(xslFileIS);
			TransformerFactory transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer xsltTransformer = transformerFactory.newTransformer(xsrc);
			
			// Pass parameters in a file
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
			xsltTransformer.transform(new StreamSource(odtFileIS), new StreamResult(printStream));
			
			//create odt
			Path contentPath = Paths.get(tempDir.toString()+"/content.xml");
			Files.copy(Paths.get(output.getAbsolutePath()), contentPath, 
					StandardCopyOption.REPLACE_EXISTING);
			Path zipPath = Paths.get(tempDir.toString()+"/export.zip");
			Files.copy(zipToCompleteIS, zipPath, 
					StandardCopyOption.REPLACE_EXISTING);
			FilesUtils.addFileToZipFolder(contentPath.toFile(),zipPath.toFile());
			Files.copy(zipPath, finalPath, 
					StandardCopyOption.REPLACE_EXISTING);

		} catch (TransformerException e) {
			logger.error(e.getMessage());
		} finally {
			odtFileIS.close();
			xslFileIS.close();
			osOutputFile.close();
			printStream.close();
		}
		logger.debug("End To export documentation");
		
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();

		try {
			return Response.ok( (StreamingOutput) out -> {
	                InputStream input = new FileInputStream( finalPath.toFile() );
	                IOUtils.copy(input, out);
	                out.flush();   
	        } ).header( "Content-Disposition", content ).build();
		 } catch ( Exception e ) { 
         	logger.error(e.getMessage());
         	throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Error downloading file"); 
         }
			}

	private String buildParams(Boolean lg1, Boolean lg2, Boolean includeEmptyMas, String targetType) {
		String includeEmptyMasString=( includeEmptyMas ? "true" : "false");
		String parametersXML="";
		
		parametersXML=parametersXML.concat(Constants.XML_OPEN_PARAMETERS_TAG);

		parametersXML=parametersXML.concat(Constants.XML_OPEN_LANGUAGES_TAG);
		if(lg1) parametersXML=parametersXML.concat("<language id=\"Fr\">1</language>");
		if(lg2) parametersXML=parametersXML.concat("<language id=\"En\">2</language>");
		parametersXML=parametersXML.concat(Constants.XML_END_LANGUAGES_TAG);

		parametersXML=parametersXML.concat(Constants.XML_OPEN_INCLUDE_EMPTY_MAS_TAG);
		parametersXML=parametersXML.concat(includeEmptyMasString);
		parametersXML=parametersXML.concat(Constants.XML_END_INCLUDE_EMPTY_MAS_TAG);

		parametersXML=parametersXML.concat(Constants.XML_OPEN_TARGET_TYPE_TAG);
		parametersXML=parametersXML.concat(targetType);
		parametersXML=parametersXML.concat(Constants.XML_END_TARGET_TYPE_TAG);

		parametersXML=parametersXML.concat(Constants.XML_END_PARAMETERS_TAG);
		return XMLUtils.encodeXml(parametersXML);
	}

	private void addParameter (Transformer xsltTransformer, String paramName, String paramData, Path tempDir) throws IOException {
		// Pass parameters in a file
		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };
		Path tempFile = Files.createTempFile(tempDir, paramName, Constants.DOT_XML);
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
