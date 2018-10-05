package fr.insee.rmes.persistance.service.sesame.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class FileUtils {

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
