package fr.insee.rmes.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;

@Component
public class ExportUtils {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ATTACHMENT = "attachment";
    private static final String ODT_EXTENSION = ".odt";
    private static final String ZIP_EXTENSION = ".zip";
    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);

    @Autowired
    DocumentsUtils documentsUtils;

    public static String getExtension(String acceptHeader) {
        if (acceptHeader == null) {
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

    private void addZipEntry(String filename, Map<String, String> xmlContent, ZipOutputStream zos, String xslFile, String xmlPattern, String zip, String objectType)
            throws IOException, RmesException {
        filename = filename.replace(ODT_EXTENSION, "");
        ZipEntry entry = new ZipEntry(filename + ODT_EXTENSION);
        InputStream input = exportAsInputStream(filename, xmlContent, xslFile, xmlPattern, zip, objectType);
        if (input == null)
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't generate codebook", "Stream is null");
        zos.putNextEntry(entry);
        input.transferTo(zos);
        zos.closeEntry(); // close the entry. Note: not closing the zos just yet as we need to add more files to our ZIP
    }

    public void exportMultipleResourceAsZip(Map<String, Map<String, String>> resources, String xslFile, String xmlPattern, String zip, String objectType, HttpServletResponse response) throws RmesException {

        String zipFileName = "concepts" + ZIP_EXTENSION;

        response.addHeader(HttpHeaders.ACCEPT, "*/*");
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
        response.addHeader(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Access-Control-Allow-Origin, Access-Control-Allow-Credentials");

        try (ZipOutputStream zipOutputStreamStream = new ZipOutputStream(response.getOutputStream())) {
            Iterator<String> resourceIterator = resources.keySet().iterator();
            while (resourceIterator.hasNext()) {
                String key = resourceIterator.next();
                this.addZipEntry(key, resources.get(key), zipOutputStreamStream, xslFile, xmlPattern, zip, objectType);
            }
        } catch (IOException e1) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "IOException on " + zipFileName, e1.getMessage());
        }
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
            InputStream input = exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType);
            if (input == null){
                logger.debug("Error when creating the export of the SIMS {}", simsId);
                throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't export this object", "");
            }

            logger.debug("Creating the .odt file for the SIMS {}", simsId);
            Path tempFile = Files.createFile(Path.of(simsDirectory.toString(), fileName + Constants.DOT_ODT));
            Files.write(tempFile, input.readAllBytes(), StandardOpenOption.APPEND);
            logger.debug("Finishing the creation of the .odt file for the SIMS {}", simsId);

            logger.debug("Starting downloading documents for the SIMS {}", simsId);
            this.exportRubricsDocuments(sims, simsDirectory);
            logger.debug("Ending downloading documents for the SIMS {}", simsId);

            logger.debug("Zipping the folder for the SIMS {}", simsId);
            FilesUtils.zipDirectory(simsDirectory.toFile());

            logger.debug("Zip created for the SIMS {}", simsId);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentDisposition(content);
            responseHeaders.set(HttpHeaders.CONTENT_TYPE, "application/zip");
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

    private void exportRubricsDocuments(JSONObject sims, Path directory) throws IOException, RmesException {
        Set<String> history = new HashSet<>();
        JSONArray documents = documentsUtils.getDocumentsUriAndUrlForSims(sims.getString("id"));
        for (int i = 0; i < documents.length(); i++) {
            JSONObject document = documents.getJSONObject(i);
            String url = document.getString("url").replace("file://", "");
            if(!history.contains(url)){
                history.add(url);
                logger.debug("Extracting document {}", url);

                String documentFileName = UriUtils.getLastPartFromUri(url);

                Path documentPath = Path.of(url);
                InputStream inputStream = Files.newInputStream(documentPath);

                logger.debug("Writing the document {} with the name {} into the folder {}", url, documentFileName, directory.toString());
                Path documentTempFile = Files.createFile(Path.of(directory.toString(), documentFileName));
                Files.write(documentTempFile, inputStream.readAllBytes(), StandardOpenOption.APPEND);

            }
        }
    }

    public ResponseEntity<Resource> exportAsResponse(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        logger.debug("Begin To export {} as Response", objectType);
        fileName = fileName.replace(ODT_EXTENSION, ""); //Remove extension if exists

        InputStream input = exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType);
        if (input == null)
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't generate codebook", "Stream is null");

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
        ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName + ODT_EXTENSION).build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.ACCEPT, "*/*");
        responseHeaders.setContentDisposition(content);
        List<String> allowHeaders = new ArrayList<>();
        allowHeaders.add("Content-Disposition");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("Access-Control-Allow-Credentials");
        responseHeaders.setAccessControlExposeHeaders(allowHeaders);
        responseHeaders.add(CONTENT_TYPE, "application/vnd.oasis.opendocument.text");

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
            Path finalPath = Paths.get(tempDir.toString(), fileName + ODT_EXTENSION);

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
            Resource resource = new UrlResource(Paths.get(tempDir.toString(), tempDir.getFileName() + ZIP_EXTENSION).toUri());
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(resource);

        } catch (IOException e1) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e1.getMessage(), e1.getClass().getSimpleName());
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
