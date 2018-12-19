package fr.insee.rmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	
	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
	
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
    
    private FileUtils() {
    	throw new IllegalStateException("Utility class");
    }

}
