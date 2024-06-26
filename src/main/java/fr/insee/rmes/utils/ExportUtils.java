package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ExportUtils {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ATTACHMENT = "attachment";
    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);
    private static final String CAN_T_GENERATE_CODEBOOK = "Can't generate codebook";
    private static final String NULL_STREAM = "Stream is null";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    final int maxLength;

    final DocumentsUtils documentsUtils;

    public ExportUtils(@Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength, DocumentsUtils documentsUtils) {
        this.maxLength = maxLength;
        this.documentsUtils = documentsUtils;
    }

    public ResponseEntity<Resource> exportAsZip(JSONObject sims, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        String simsId = sims.getString("id");
        logger.debug("Begin to download the SIMS {} with its documents", simsId);
        String fileName = sims.getString("labelLg1");

        ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName + Constants.DOT_ZIP).build();

        try {

            Path directory = Files.createTempDirectory("sims");
            logger.debug("Creating tempory directory {}", directory.toString());
            Path simsDirectory = Files.createDirectory(Path.of(directory.toString(), fileName));
            logger.debug("Creating tempory directory {}", simsDirectory.toString());

            logger.debug("Generating the InputStream for the SIMS {}", simsId);

            InputStream input = exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION);
            if (input == null){
                logger.debug("Error when creating the export of the SIMS {}", simsId);
                throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't export this object", "");
            }

            logger.debug("Creating the .odt file for the SIMS {}", simsId);
            Path tempFile = Files.createFile(Path.of(simsDirectory.toString(), fileName + FilesUtils.ODT_EXTENSION));
            Files.write(tempFile, input.readAllBytes(), StandardOpenOption.APPEND);
            logger.debug("Finishing the creation of the .odt file for the SIMS {}", simsId);


            logger.debug("Starting downloading documents for the SIMS {}", simsId);
            Set<String> missingDocuments = this.exportRubricsDocuments(sims, simsDirectory);
            logger.debug("Ending downloading documents for the SIMS {}", simsId);

            logger.debug("Zipping the folder for the SIMS {}", simsId);
            FilesUtils.zipDirectory(simsDirectory.toFile());

            logger.debug("Zip created for the SIMS {}", simsId);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentDisposition(content);
            responseHeaders.set(HttpHeaders.CONTENT_TYPE, "application/zip");
            responseHeaders.setAccessControlExposeHeaders(List.of("X-Missing-Documents"));
            responseHeaders.set("X-Missing-Documents", String.join(",", missingDocuments));
            Resource resource = new UrlResource(Paths.get(simsDirectory.toString(), simsDirectory.getFileName() + Constants.DOT_ZIP).toUri());
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(resource);
        }
        catch (Exception exception) {
            logger.error("Error when downloading the SIMS {} with its documents", simsId, exception);
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), exception.getClass().getSimpleName());
        }
    }

    private Set<String> exportRubricsDocuments(JSONObject sims, Path directory) throws IOException, RmesException {
        Set<String> history = new HashSet<>();
        JSONArray documents = documentsUtils.getDocumentsUriAndUrlForSims(sims.getString("id"));
        Set<String> missingDocuments = new HashSet<>();

        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String url = document.getString("url").replace("file://", "");
            if(!history.contains(url)){
                history.add(url);
                logger.debug("Extracting document {}", url);


                Path documentPath = Path.of(url);

                if(!Files.exists(documentPath)){
                    missingDocuments.add(document.getString("id"));
                } else {
                    String documentFileName = FilesUtils.reduceFileNameSize(UriUtils.getLastPartFromUri(url), maxLength);
                    try (InputStream inputStream = Files.newInputStream(documentPath)){
                        Path documentDirectory = Path.of(directory.toString(), "documents");
                        if (!Files.exists(documentDirectory)) {
                            logger.debug("Creating the documents folder");
                            Files.createDirectory(documentDirectory);
                        }

                        logger.debug("Writing the document {} with the name {} into the folder {}", url, documentFileName, directory.toString());
                        Path documentTempFile = Files.createFile(Path.of(documentDirectory.toString(), documentFileName));
                        Files.write(documentTempFile, inputStream.readAllBytes(), StandardOpenOption.APPEND);
                    }
                }

            }
        }

        return missingDocuments;
    }


    public ResponseEntity<Resource> exportAsODT(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        return exportAsFileByExtension(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION, "application/vnd.oasis.opendocument.text");
    }

    public ResponseEntity<Resource> exportAsODS(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        return exportAsFileByExtension(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODS_EXTENSION, "application/vnd.oasis.opendocument.spreadsheet");
    }

    private ResponseEntity<Resource> exportAsFileByExtension(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType, String extension, String contentType) throws RmesException {
        logger.debug("Begin To export {} as Response", objectType);
        fileName = fileName.replace(extension, "");

        InputStream input = exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, extension);
        if (input == null)
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, CAN_T_GENERATE_CODEBOOK, NULL_STREAM);

        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(IOUtils.toByteArray(input));
            input.close();
        } catch (IOException e) {
            logger.error("Failed to getBytes of resource");
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
        }
        logger.debug("End To export {} as Response", objectType);

        //Prepare response headers
        ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName + extension).build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.ACCEPT, "*/*");
        responseHeaders.setContentDisposition(content);
        List<String> allowHeaders = new ArrayList<>();
        allowHeaders.add(CONTENT_DISPOSITION);
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("Access-Control-Allow-Credentials");
        responseHeaders.setAccessControlExposeHeaders(allowHeaders);
        responseHeaders.add(CONTENT_TYPE, contentType);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType, String extension) throws RmesException {
        logger.debug("Begin To export {} as InputStream", objectType);

        File output = null;
        InputStream odtFileIS = null;
        InputStream xslFileIS = null;
        InputStream zipToCompleteIS = null;
        fileName = fileName.replace(extension, ""); //Remove extension if exists


        try {
            xslFileIS = getClass().getResourceAsStream(xslFile);
            odtFileIS = getClass().getResourceAsStream(xmlPattern);
            zipToCompleteIS = getClass().getResourceAsStream(zip);

            // prepare output
            output = File.createTempFile(Constants.OUTPUT, FilesUtils.getExtension(Constants.XML));
            output.deleteOnExit();

        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
        }

        try (OutputStream osOutputFile = FileUtils.openOutputStream(output);
             PrintStream printStream = new PrintStream(osOutputFile);) {

            Path tempDir = Files.createTempDirectory("forExport");
            Path finalPath = Paths.get(tempDir.toString(), fileName + extension);

            // transform
            XsltUtils.xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream, tempDir);
            // create odt
            XsltUtils.createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);

            logger.debug("End To export {} as InputStream", objectType);

            return Files.newInputStream(finalPath);
        } catch (IOException | TransformerException e) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
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
        ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename("xmlFiles.zip").build();
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
            responseHeaders.set(HttpHeaders.CONTENT_TYPE, "application/zip");
            Resource resource = new UrlResource(Paths.get(tempDir.toString(), tempDir.getFileName() + FilesUtils.ZIP_EXTENSION).toUri());
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(resource);

        } catch (IOException e1) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e1.getMessage(), e1.getClass().getSimpleName());
        }


    }

    public static String toValidationStatus(String boolStatus, boolean fem) {
        if ("true".equals(boolStatus)) {
            return fem ? "Publiée" : "Publié";
        } else {
            return "Provisoire";
        }
    }
}
