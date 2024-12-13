package fr.insee.rmes.utils;

import java.nio.file.Path;

public class UriUtils {
	
	  private UriUtils() {
		    throw new IllegalStateException("Utility class");
	}

	
	public static String getLastPartFromUri(String uri) {
		return Path.of(uri).getFileName().toString();
	}

}
