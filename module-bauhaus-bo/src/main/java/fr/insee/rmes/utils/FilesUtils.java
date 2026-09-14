package fr.insee.rmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

public class FilesUtils {

    private static final Logger log = LoggerFactory.getLogger(FilesUtils.class);
    public static final String ODT_EXTENSION = ".odt";
    public static final String ODS_EXTENSION = ".ods";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String PDF_EXTENSION = ".pdf";
    public static final String XML_EXTENSION = ".xml";
    public static final String FODT_EXTENSION = ".fodt";
    private static final char EXTENSION_SEPARATOR = '.';
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");
    private static final Pattern SEPARATORS = Pattern.compile("[-_\\s\\p{Z}]+");
    private static final Pattern NON_ASCII_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9 ]");
    private static final String DEFAULT_BASE_NAME = "export";

    public static MediaType getMediaTypeFromExtension(String extension) {
        return switch (extension) {
            case FilesUtils.ODT_EXTENSION -> new MediaType("application", "vnd.oasis.opendocument.text");
            case FilesUtils.ODS_EXTENSION -> new MediaType("application", "vnd.oasis.opendocument.spreadsheet");
            case FilesUtils.ZIP_EXTENSION -> new MediaType("application", "zip");
            default -> throw new IllegalStateException("Unexpected value: " + extension);
        };
    }

    public static String getExtension(String acceptHeader) {
        return switch (acceptHeader) {
            case null -> FilesUtils.ODT_EXTENSION;
            case "application/octet-stream" -> PDF_EXTENSION;
            case "flatODT" -> FODT_EXTENSION;
            case "XML" -> XML_EXTENSION;
            default -> ODT_EXTENSION;
        };
    }

    /**
     * Le nom passé en paramètre est un libellé métier, pas un chemin du système de fichiers :
     * il peut contenir des caractères (dont {@code :}) que {@code FilenameUtils} refuse sous Windows
     * (séparateur NTFS ADS). La découpe nom/extension est donc faite ici, indépendamment de l'OS.
     */
    public static String generateFinalFileNameWithExtension(String fileName, int maxLength) {
        var extensionIndex = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        var basename = extensionIndex == -1 ? fileName : fileName.substring(0, extensionIndex);
        var extension = extensionIndex == -1
                ? ""
                : toAscii(fileName.substring(extensionIndex + 1)).trim();
        var finalBaseName = generateFinalBaseName(basename, maxLength);
        return extension.isEmpty() ? finalBaseName : finalBaseName + "." + extension;
    }

    public static String generateFinalFileNameWithoutExtension(String fileName, int maxLength) {
        return generateFinalBaseName(fileName, maxLength);
    }

    private static String generateFinalBaseName(String baseName, int maxLength) {
        var finalBaseName = reduceFileNameSize(CaseUtils.toCamelCase(toAscii(baseName), false), maxLength);
        // Un libellé entièrement non-ASCII se réduirait à une chaîne vide : le fichier, et surtout le
        // dossier créé pour un zip, doivent tout de même porter un nom.
        return finalBaseName.isEmpty() ? DEFAULT_BASE_NAME : finalBaseName;
    }

    private static String reduceFileNameSize(String fileName, int maxLength) {
        return fileName.substring(0, Math.min(fileName.length(), maxLength));
    }

    /**
     * Ne conserve que des lettres et chiffres ASCII, séparés par des espaces : c'est ce qui garantit
     * qu'un libellé métier (accentué, ponctué à la française) donne un nom de fichier ou de dossier
     * exploitable sur tous les systèmes de fichiers et tous les outils d'archivage.
     */
    private static String toAscii(String fileName) {
        var withoutDiacritics = COMBINING_MARKS
                .matcher(Normalizer.normalize(fileName, Normalizer.Form.NFD))
                .replaceAll("");
        var transliterated = transliterateUndecomposableLetters(withoutDiacritics);
        var separated = SEPARATORS.matcher(transliterated).replaceAll(" ");
        return NON_ASCII_ALPHANUMERIC.matcher(separated).replaceAll("");
    }

    /**
     * Ces lettres n'ont pas de forme décomposée : la normalisation NFD les laisse intactes, il faut
     * donc les translittérer explicitement pour qu'elles ne survivent pas au filtre ASCII.
     */
    private static String transliterateUndecomposableLetters(String fileName) {
        return fileName.replace("œ", "oe")
                .replace("Œ", "OE")
                .replace("æ", "ae")
                .replace("Æ", "AE")
                .replace("ø", "o")
                .replace("Ø", "O")
                .replace("ß", "ss")
                .replace("ẞ", "SS")
                .replace("đ", "d")
                .replace("Đ", "D")
                .replace("ð", "d")
                .replace("Ð", "D")
                .replace("ł", "l")
                .replace("Ł", "L")
                .replace("þ", "th")
                .replace("Þ", "TH")
                .replace("ı", "i");
    }

    public static void addFileToZipFolder(File fileToAdd, File zipArchive) {
        ZipEntrySource entry = new FileSource(fileToAdd.getName(), fileToAdd);
        ZipUtil.addEntry(zipArchive, entry);
    }

    private FilesUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void zipDirectory(File directoryToZip) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(directoryToZip + "/" + directoryToZip.getName() + ".zip");
                ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            zipFile(directoryToZip, directoryToZip.getName(), zipOut);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden() || fileName.endsWith(".zip")) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }

            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}
