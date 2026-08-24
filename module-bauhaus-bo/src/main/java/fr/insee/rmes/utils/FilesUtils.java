package fr.insee.rmes.utils;

import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FilesUtils {

	private static final Logger log = LoggerFactory.getLogger(FilesUtils.class);
	public static final String ODT_EXTENSION = ".odt";
	public static final String ODS_EXTENSION = ".ods";
	public static final String ZIP_EXTENSION = ".zip";
	public static final String PDF_EXTENSION = ".pdf";
	public static final String XML_EXTENSION = ".xml";
	public static final String FODT_EXTENSION = ".fodt";
	private static final char EXTENSION_SEPARATOR = '.';

	public static MediaType getMediaTypeFromExtension(String extension) {
		return switch (extension){
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
	public static String generateFinalFileNameWithExtension(String fileName, int maxLength){
		var extensionIndex = fileName.lastIndexOf(EXTENSION_SEPARATOR);
		var basename = extensionIndex == -1 ? fileName : fileName.substring(0, extensionIndex);
		var extension = extensionIndex == -1 ? "" : fileName.substring(extensionIndex + 1);
		return generateFinalBaseName(basename, maxLength) + "." + extension;
	}

	public static String generateFinalFileNameWithoutExtension(String fileName, int maxLength){
		return generateFinalBaseName(fileName, maxLength);
	}

	private static String generateFinalBaseName(String baseName, int maxLength){
		return reduceFileNameSize(CaseUtils.toCamelCase(removeAsciiCharacters(baseName), false), maxLength);
	}

	private static String reduceFileNameSize(String fileName, int maxLength) {
		return fileName.substring(0, Math.min(fileName.length(), maxLength));
	}

	private static String removeAsciiCharacters(String fileName) {
		return Normalizer.normalize(fileName, Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "")
				.replace("œ", "oe")
				.replace("Œ", "OE")
				.replaceAll("[-_]", " ")
				.replaceAll("\\p{Punct}", "")
				.replace(":", "")
				.replace("’", "");
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
