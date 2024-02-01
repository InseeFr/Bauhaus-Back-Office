package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ExportUtils {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ATTACHMENT = "attachment";
    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);

    private final FilesUtils filesUtils;

    @Autowired
    public ExportUtils(FilesUtils filesUtils) {
        this.filesUtils = filesUtils;
    }

    public static String getExtension(String acceptHeader) {
        if (acceptHeader == null) {
            return FilesUtils.ODT_EXTENSION;
        } else if (acceptHeader.equals("application/octet-stream")) {
            return ".pdf";
        } else if (acceptHeader.equals("flatODT")) {
            return ".fodt";
        } else if (acceptHeader.equals("XML")) {
            return ".xml";
        } else if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
            return FilesUtils.ODT_EXTENSION;
        } else {
            return FilesUtils.ODT_EXTENSION;
            // default --> odt
        }
    }

    private void addZipEntry(String filename, Map<String, String> xmlContent, ZipOutputStream zos, String xslFile, String xmlPattern, String zip, String objectType)
            throws IOException, RmesException {
        filename = filename.replace(FilesUtils.ODT_EXTENSION, "");
        ZipEntry entry = new ZipEntry(filename + FilesUtils.ODT_EXTENSION);
        InputStream input = exportAsInputStream(filename, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION);
        if (input == null)
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't generate codebook", "Stream is null");
        zos.putNextEntry(entry);
        input.transferTo(zos);
        zos.closeEntry(); // close the entry. Note: not closing the zos just yet as we need to add more files to our ZIP
    }

    public void exportMultipleResourceAsZip(Map<String, Map<String, String>> resources, String xslFile, String xmlPattern, String zip, String objectType, HttpServletResponse response) throws RmesException {

        String zipFileName = "concepts" + FilesUtils.ZIP_EXTENSION;

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


    public ResponseEntity<Resource> exportAsResponseODS(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        logger.debug("Begin To export {} as Response", objectType);
        fileName = filesUtils.reduceFileNameSize(fileName.replace(FilesUtils.ODS_EXTENSION, ""));
        InputStream input = exportAsInputStreamODS(fileName, xmlContent, xslFile, xmlPattern, zip, objectType);
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
        ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName + FilesUtils.ODS_EXTENSION).build();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.ACCEPT, "*/*");
        responseHeaders.setContentDisposition(content);
        List<String> allowHeaders = new ArrayList<>();
        allowHeaders.add("Content-Disposition");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("Access-Control-Allow-Credentials");
        responseHeaders.setAccessControlExposeHeaders(allowHeaders);
        responseHeaders.add(CONTENT_TYPE, "application/vnd.oasis.opendocument.spreadsheet");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


    public ResponseEntity<Resource> exportAsResponse(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        logger.debug("Begin To export {} as Response", objectType);
        fileName = filesUtils.reduceFileNameSize(fileName.replace(FilesUtils.ODT_EXTENSION, ""));

        InputStream input = exportAsInputStream(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION);
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
        ContentDisposition content = ContentDisposition.builder(ATTACHMENT).filename(fileName + FilesUtils.ODT_EXTENSION).build();
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

    public InputStream exportAsInputStreamODS(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        logger.debug("Begin To export {} as InputStream", objectType);

        File output = null;
        InputStream odsFileIS = null;
        InputStream xslFileIS = null;
        InputStream zipToCompleteIS = null;
        fileName = fileName.replace(FilesUtils.ODS_EXTENSION, ""); //Remove extension if exists


        try {
            xslFileIS = getClass().getResourceAsStream(xslFile);
            odsFileIS = getClass().getResourceAsStream(xmlPattern);
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
            Path finalPath = Paths.get(tempDir.toString(), fileName + FilesUtils.ODT_EXTENSION);

            // transform
            XsltUtils.xsltTransform(xmlContent, odsFileIS, xslFileIS, printStream, tempDir);
            // create ods
            XsltUtils.createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);

            logger.debug("End To export {} as InputStream", objectType);

            return Files.newInputStream(finalPath);
        } catch (IOException | TransformerException e) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
        } finally {
            try {
                if (odsFileIS != null)
                    odsFileIS.close();
                if (xslFileIS != null)
                    xslFileIS.close();
                if (zipToCompleteIS != null)
                    zipToCompleteIS.close();
            } catch (IOException ioe) {
                logger.error(ioe.getMessage());
            }
        }
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
            output = File.createTempFile(Constants.OUTPUT, getExtension(Constants.XML));
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



    public static String toLabel(String dsURL) {
        return DisseminationStatus.getEnumLabel(dsURL);
    }

    public static String toValidationStatus(String boolStatus, boolean fem) {
        if ("true".equals(boolStatus)) {
            return fem ? "Publiée" : "Publié";
        } else {
            return "Provisoire";
        }
    }
}
