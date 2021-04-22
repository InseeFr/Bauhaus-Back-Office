package fr.insee.rmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
			log.error(e.getMessage());
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

	public static String cleanFileNameAndAddExtension(String fileName, String extension) {
		fileName = fileName.toLowerCase().trim();
		fileName = StringUtils.normalizeSpace(fileName);
		fileName = fileName.replace(" ","-");
		fileName = Normalizer.normalize(fileName, Normalizer.Form.NFD).replace("[^\\p{ASCII}]", "") ;
		if (extension.startsWith(".")) {
			fileName += extension ;
		}else {
			fileName += "." + extension ;
		}
		return fileName;
	}

	public static File zipFile(File inputFile) throws IOException {
		Map<String, String> env = new HashMap<>(); 
		env.put("create", "true");
		// locate file system by using the syntax 
		// defined in java.net.JarURLConnection
		URI uri = URI.create("jar:file:/codeSamples/zipfs/zipfstest.zip");

		FileSystem zipfs = null;

		try{ 
			zipfs = FileSystems.newFileSystem(uri, env); 
			String sourcePath = inputFile.getPath();
			Path source = Paths.get(sourcePath);
			Path pathInZipfile = zipfs.getPath(UriUtils.getLastPartFromUri(sourcePath));  
			// copy a file into the zip file
			Files.copy( source,pathInZipfile, 
					StandardCopyOption.REPLACE_EXISTING ); 
			return pathInZipfile.toFile();}
		finally { if (zipfs != null) zipfs.close();}
	}


	public static void addFileToZipFolder(File fileToAdd, File zipArchive) {
		ZipEntrySource entry =  new FileSource(fileToAdd.getName(), fileToAdd);
		ZipUtil.addEntry(zipArchive, entry);
	}


	private FilesUtils() {
		throw new IllegalStateException("Utility class");
	}



}
