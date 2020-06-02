package fr.insee.rmes.utils;

import java.util.Arrays;

public class StringUtils {
	
	public static boolean stringContainsItemFromList(String string, String[] list) {
		return Arrays.stream(list).anyMatch(string::contains);
	}

}
