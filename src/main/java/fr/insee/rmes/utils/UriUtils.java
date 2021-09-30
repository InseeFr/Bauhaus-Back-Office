package fr.insee.rmes.utils;

import org.apache.commons.lang3.StringUtils;

public class UriUtils {
	
	  private UriUtils() {
		    throw new IllegalStateException("Utility class");
	}

	
	public static String getLastPartFromUri(String uri) {
		if (uri.contains("\\")) return StringUtils.substringAfterLast(uri, "\\");
		return StringUtils.substringAfterLast(uri, "/");
	}

}
