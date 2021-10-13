package fr.insee.rmes.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
	
	  private StringUtils() {
		    throw new IllegalStateException("Utility class");
	}

	public static boolean stringContainsItemFromList(String string, String[] list) {
		return Arrays.stream(list).anyMatch(string::contains);
	}

	public static List<String> stringToList(String value) {
		List<String> val = new ArrayList<>();
		val.add(value);
		return val;
	}
	
}
