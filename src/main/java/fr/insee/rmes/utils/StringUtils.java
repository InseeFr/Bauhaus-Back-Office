package fr.insee.rmes.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
	
	  private StringUtils() {
		    throw new IllegalStateException("Utility class");
	}

	public static List<String> stringToList(String value) {
		List<String> val = new ArrayList<>();
		val.add(value);
		return val;
	}

	public static String convertHtmlStringToRaw(String html) {
		String raw = html
				.replaceAll("<p>", "\n")
				.replaceAll("<[^>]*>", "");

		if (raw.startsWith("\n")) {
			raw = raw.substring(1);
		}
		return raw;
	}
}
