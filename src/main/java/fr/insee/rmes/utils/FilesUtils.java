package fr.insee.rmes.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
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

	public static MediaType getMediaTypeFromExtension(String extension) {
		MediaType contentType = switch (extension){
			case FilesUtils.ODT_EXTENSION -> new MediaType("application", "vnd.oasis.opendocument.text");
			case FilesUtils.ODS_EXTENSION -> new MediaType("application", "vnd.oasis.opendocument.spreadsheet");
			case FilesUtils.ZIP_EXTENSION -> new MediaType("application", "zip");
			default -> throw new IllegalStateException("Unexpected value: " + extension);
		};
		return contentType;
	}

	public static String getExtension(String acceptHeader) {
		if (acceptHeader == null) {
			return FilesUtils.ODT_EXTENSION;
		} else if (acceptHeader.equals("application/octet-stream")) {
			return PDF_EXTENSION;
		} else if (acceptHeader.equals("flatODT")) {
			return FODT_EXTENSION;
		} else if (acceptHeader.equals("XML")) {
			return XML_EXTENSION;
		} else if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			return ODT_EXTENSION;
		} else {
			return ODT_EXTENSION;
		}
	}

	public static String reduceFileNameSize(String fileName, int maxLength) {
		return fileName.substring(0, Math.min(fileName.length(), maxLength));
	}

	public static String removeAsciiCharacters(String fileName) {
		return Normalizer.normalize(fileName, Normalizer.Form.NFD)
				.replaceAll("œ", "oe")
				.replaceAll("Œ", "OE")
				.replaceAll("\\p{M}+", "")
				.replaceAll("\\p{Punct}", "")
				.replaceAll(":", "");
	}

	public static void addFileToZipFolder(File fileToAdd, File zipArchive) {
		ZipEntrySource entry = new FileSource(fileToAdd.getName(), fileToAdd);
		ZipUtil.addEntry(zipArchive, entry);
	}

	private FilesUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static void zipDirectory(File directoryToZip) throws IOException {
		FileOutputStream fos = new FileOutputStream(directoryToZip + "/" + directoryToZip.getName() + ".zip");
		ZipOutputStream zipOut = new ZipOutputStream(fos);

		zipFile(directoryToZip, directoryToZip.getName(), zipOut);
		
		try {
			zipOut.close();
			fos.close();
		} catch(IOException e ) {
			log.warn("outputStream already closed");
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
