package fr.insee.rmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

public class FilesUtils {

	private static final Logger log = LoggerFactory.getLogger(FilesUtils.class);

	public static InputStream fileToIS(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (Exception e) {
			log.error("fileToIS : {}", e.getMessage());
		}
		return is;
	}

	public static File streamToFile(InputStream in, String fileName, String fileExtension) throws IOException {
		final File tempFile = File.createTempFile(fileName, fileExtension);
		tempFile.deleteOnExit();
		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}
		return tempFile;
	}
	
	public static File streamToFile(InputStream in, String fileName) throws IOException {
		if (fileName.contains(".")) {
			return streamToFile(in, FilenameUtils.getBaseName(fileName), FilenameUtils.getExtension(fileName));
		}else return null;
	}

	public static String cleanFileNameAndAddExtension(String fileName, String extension) {
		fileName = fileName.toLowerCase().trim();
		fileName = StringUtils.normalizeSpace(fileName);
		fileName = fileName.replace(" ", "-");
		fileName = Normalizer.normalize(fileName, Normalizer.Form.NFD).replace("[^\\p{ASCII}]", "");
		if (extension.startsWith(".")) {
			fileName += extension;
		} else {
			fileName += "." + extension;
		}
		return fileName;
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
			fos.close();
			zipOut.close();
		}catch(IOException e ) {
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
