package fr.insee.rmes.utils;

import org.apache.commons.lang3.StringUtils;

public class UriUtils {
	
	public static String getLastPartFromUri(String uri) {
		if (uri.contains("\\")) return StringUtils.substringAfterLast(uri, "\\");
		return StringUtils.substringAfterLast(uri, "/");
	}

}
