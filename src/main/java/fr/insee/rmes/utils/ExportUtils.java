package fr.insee.rmes.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;

@Component
public class ExportUtils {
	
	private static final String ATTACHMENT = "attachment";
	private static final String ODT_EXTENSION = ".odt";
	private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);

	

	public static String getExtension(String acceptHeader) {
		if (acceptHeader==null) {
			return ODT_EXTENSION;
		} else if (acceptHeader.equals("application/octet-stream")) {
			return ".pdf";
		} else if (acceptHeader.equals("flatODT")) {
			return ".fodt";
		} else if (acceptHeader.equals("XML")) {
			return ".xml";
		} else if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			return ODT_EXTENSION;
		} else {
			return ODT_EXTENSION;
			// default --> odt
		}
	}

	public ResponseEntity<Resource> exportAsResponse(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
		logger.debug("Begin To export {} as Response", objectType);
		fileName = fileName.replace(ODT_EXTENSION, ""); //Remove extension if exists

		InputStream input = exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType);
		if (input == null) throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Can't generate codebook","Stream is null");

		ByteArrayResource resource = null;
		try {
			resource = new ByteArrayResource(IOUtils.toByteArray(input));
			input.close();
		} catch (IOException e) {
			logger.error("Failed to getBytes of resource");
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		logger.debug("End To export {} as Response", objectType);

		//Prepare response headers
		ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName+ODT_EXTENSION).build();
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(HttpHeaders.ACCEPT,  "*/*");
		responseHeaders.setContentDisposition(content);
		List<String> allowHeaders = new ArrayList<>();
		allowHeaders.add("Content-Disposition");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("Access-Control-Allow-Credentials");
		responseHeaders.setAccessControlExposeHeaders(allowHeaders);
		responseHeaders.add("Content-Type","application/vnd.oasis.opendocument.text" );

		return ResponseEntity.ok()
						.headers(responseHeaders)
				        .contentLength(resource.contentLength())
				        .contentType(MediaType.APPLICATION_OCTET_STREAM)
						.body(resource);		

	}
	

	public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
		logger.debug("Begin To export {} as InputStream", objectType);

		File output = null;
		InputStream odtFileIS = null;
		InputStream xslFileIS = null;
		InputStream zipToCompleteIS = null;
		fileName = fileName.replace(ODT_EXTENSION, ""); //Remove extension if exists


		try {
			xslFileIS = getClass().getResourceAsStream(xslFile);
			odtFileIS = getClass().getResourceAsStream(xmlPattern);
			zipToCompleteIS = getClass().getResourceAsStream(zip);

			// prepare output
			output = File.createTempFile(Constants.OUTPUT, getExtension(Constants.XML));
			output.deleteOnExit();
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
		}

		try (OutputStream osOutputFile = FileUtils.openOutputStream(output);
				PrintStream printStream = new PrintStream(osOutputFile);) {

			Path tempDir = Files.createTempDirectory("forExport");
			Path finalPath = Paths.get(tempDir.toString() , fileName + ODT_EXTENSION);

			// transform
			XsltUtils.xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream, tempDir);

			// create odt
			XsltUtils.createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);

			logger.debug("End To export {} as InputStream", objectType);

			return Files.newInputStream(finalPath);
		} catch (IOException | TransformerException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		} finally {
			try {
				if (odtFileIS != null)
					odtFileIS.close();
				if (xslFileIS != null)
					xslFileIS.close();
				if (zipToCompleteIS != null)
					zipToCompleteIS.close();
			} catch (IOException ioe) {
				logger.error(ioe.getMessage());
			}
		}
	}
	
	public ResponseEntity<Object> exportFilesAsResponse(Map<String, String> xmlContent) throws RmesException {
		logger.debug("Begin To export temp files as Response");
		ContentDisposition content = ContentDisposition.builder("attachment").filename("xmlFiles.zip").build();
		Path tempDir;

		try {
			tempDir = Files.createTempDirectory("xmlFiles");
		
			// Add all files in a tempDirectory
			xmlContent.forEach((paramName, xmlData) -> {	
				try {
					Path tempFile = Files.createTempFile(tempDir, paramName, Constants.DOT_XML);
					Files.write(tempFile, xmlData.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			});
			
			//zip tempDirectory
			FilesUtils.zipDirectory(tempDir.toFile()); 
			
			logger.debug("End To export temp files as Response");
			
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentDisposition(content);
		    responseHeaders.set("Content-Type",  "application/octet-stream");
			return ResponseEntity.ok()
							.headers(responseHeaders)
							.body(Paths.get(tempDir.toString(), tempDir.getFileName()+".zip").toFile());			
			
		} catch (IOException e1) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e1.getMessage(), e1.getClass().getSimpleName());
		} 


	}
	
	public static String toLabel(String dsURL) {
		return DisseminationStatus.getEnumLabel(dsURL);
	}

	public static String toDate(String dateTime) {
		if (dateTime != null && dateTime.length() > 10) {
			return dateTime.substring(8, 10) + "/" + dateTime.substring(5, 7) + "/" + dateTime.substring(0, 4);
		}
		return dateTime;
	}

	public static String toValidationStatus(String boolStatus, boolean fem) {
		if (boolStatus.equals("true")) {
				return fem ? "Publiée" : "Publié";
		} else {
			return "Provisoire";
		}
	}

	
	
}
