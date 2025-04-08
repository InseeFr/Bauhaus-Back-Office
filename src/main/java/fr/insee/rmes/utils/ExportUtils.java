package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public record ExportUtils(@Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength) {
    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);
    private static final String CAN_T_GENERATE_CODEBOOK = "Can't generate codebook";
    private static final String NULL_STREAM = "Stream is null";

    public ResponseEntity<Resource> exportAsODT(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        return exportAsFileByExtension(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION);
    }

    public ResponseEntity<Resource> exportAsODS(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType) throws RmesException {
        return exportAsFileByExtension(fileName, xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODS_EXTENSION);
    }

    private ResponseEntity<Resource> exportAsFileByExtension(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType, String extension) throws RmesException {
        logger.debug("Begin To export {} as Response", objectType);
        fileName = FilesUtils.generateFinalFileNameWithoutExtension(fileName.replace(extension, ""), maxLength);

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

        HttpHeaders responseHeaders = HttpUtils.generateHttpHeaders(fileName, extension);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

//    public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent, String xslFile, String xmlPattern, String zip, String objectType, String extension) throws RmesException {
//        logger.debug("Begin To export {} as InputStream", objectType);
//
//        File output = null;
//        InputStream odtFileIS = null;
//        InputStream xslFileIS = null;
//        InputStream zipToCompleteIS = null;
//        fileName = fileName.replace(extension, ""); //Remove extension if exists
//
//
//        try {
//            xslFileIS = getClass().getResourceAsStream(xslFile);
//            odtFileIS = getClass().getResourceAsStream(xmlPattern);
//            zipToCompleteIS = getClass().getResourceAsStream(zip);
//
//            // prepare output
//            output = File.createTempFile(Constants.OUTPUT, FilesUtils.getExtension(Constants.XML));
//            output.deleteOnExit();
//
//        } catch (IOException ioe) {
//            logger.error(ioe.getMessage());
//        }
//
//        try (OutputStream osOutputFile = FileUtils.openOutputStream(output);
//             PrintStream printStream = new PrintStream(osOutputFile)) {
//
//            Path tempDir = Files.createTempDirectory("forExport");
//            Path finalPath = Paths.get(tempDir.toString(), fileName + extension);
//
//            // transform
//            XsltUtils.xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream, tempDir);
//            // create odt
//            XsltUtils.createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);
//
//            logger.debug("End To export {} as InputStream", objectType);
//
//            return Files.newInputStream(finalPath);
//        } catch (IOException | TransformerException e) {
//            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
//        } finally {
//            try {
//                if (odtFileIS != null)
//                    odtFileIS.close();
//                if (xslFileIS != null)
//                    xslFileIS.close();
//                if (zipToCompleteIS != null)
//                    zipToCompleteIS.close();
//            } catch (IOException ioe) {
//                logger.error(ioe.getMessage());
//            }
//        }
//    }

    public InputStream exportAsInputStream(String fileName, Map<String, String> xmlContent,
                                           String xslFile, String xmlPattern, String zip,
                                           String objectType, String extension) throws RmesException {

        logger.debug("Begin To export {} as InputStream", objectType);

        try (
                InputStream odtFileIS = getClass().getResourceAsStream(xmlPattern);
                InputStream xslFileIS = getClass().getResourceAsStream(xslFile);
                InputStream zipToCompleteIS = getClass().getResourceAsStream(zip);
                ByteArrayOutputStream transformedXmlOutput = new ByteArrayOutputStream()
        ) {
            // Étape 1 : Transformation XSLT en mémoire
            try (PrintStream printStream = new PrintStream(transformedXmlOutput)) {
                XsltUtils.xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream);
            }

            // Étape 2 : Création du fichier ODS (ZIP) en mémoire
            ByteArrayOutputStream finalOdsOutput = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(finalOdsOutput);
                 ZipInputStream zipIn = new ZipInputStream(zipToCompleteIS)) {

                // Copier toutes les entrées existantes du modèle ZIP (zipToCompleteIS)
                ZipEntry entry;
                while ((entry = zipIn.getNextEntry()) != null) {
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    zipIn.transferTo(zipOut);
                    zipOut.closeEntry();
                }

                // Ajouter ou remplacer "content.xml" avec le XML transformé
                zipOut.putNextEntry(new ZipEntry("content.xml"));
                zipOut.write(transformedXmlOutput.toByteArray());
                zipOut.closeEntry();
            }

            logger.debug("End To export {} as InputStream", objectType);

            return new ByteArrayInputStream(finalOdsOutput.toByteArray());

        } catch (IOException | TransformerException e) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
        }
    }


    public ResponseEntity<Object> exportFilesAsResponse(Map<String, String> xmlContent) throws RmesException {
        logger.debug("Begin To export temp files as Response");
        Path tempDir;

        try {
            tempDir = Files.createTempDirectory("xmlFiles");

            // Add all files in a tempDirectory
            xmlContent.forEach((paramName, xmlData) -> {
                try {
                    Path tempFile = Files.createTempFile(tempDir, paramName, FilesUtils.XML_EXTENSION);
                    Files.write(tempFile, xmlData.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            });

            //zip tempDirectory
            FilesUtils.zipDirectory(tempDir.toFile());

            logger.debug("End To export temp files as Response");

            HttpHeaders responseHeaders = HttpUtils.generateHttpHeaders("xmlFiles", FilesUtils.ZIP_EXTENSION);
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
