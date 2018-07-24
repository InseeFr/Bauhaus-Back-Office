package fr.insee.rmes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

}
