package fr.insee.rmes.utils;

import org.apache.commons.lang3.StringUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class UriUtils {

	private UriUtils() {
		    throw new IllegalStateException("Utility class");
	}

	
	public static String getLastPartFromUri(String uri) {
		if (uri.contains("\\")) return StringUtils.substringAfterLast(uri, "\\");
		return StringUtils.substringAfterLast(uri, "/");
	}

	public static boolean isValiURN(String urn) {
		boolean validate = false;
		if (urn != null) {
			if (urn.startsWith("urn") && StringUtils.countMatches(urn, ":") >= 2) {
				validate = true;}
			if (urn.contains(" :") || urn.contains(": ")) {
				validate = false;}
		}
		return (validate);
	}

	public static boolean isValiURI(String myUri) {
		boolean response = false;
		if (myUri != null) {
			try {
				URI monUri = URI.create(myUri);
				response = true;
			} catch (Exception ignored) {
			}
		}
		return response;
	}


	public static boolean isValiURL(String url) {

		boolean standard = false;

		if (url != null) {

			List<String> urlBegin1 = List.of("http://","https://");
			List<String> urlBegin2 = List.of("https://|","https:// ","http://|","http:// ");

			for(String element : urlBegin1){
				if (url.startsWith(element)) standard = true;
				if (element.equals(url)) standard = false;
			}

			for(String element : urlBegin2){if (url.startsWith(element)) standard = false;}

			if (!url.contains("//") ) standard = false;

			int openingBrace = org.springframework.util.StringUtils.countOccurrencesOf(url,"[");
			int closingBrace = org.springframework.util.StringUtils.countOccurrencesOf(url,"]");

			if(openingBrace-closingBrace!=0) {
				standard =false;}

			if (openingBrace == 0 && closingBrace == 0) {
					if (StringUtils.countMatches(url, ":") != StringUtils.countMatches(url, "http") ){
						standard = false;
					}
				}

			if (openingBrace - closingBrace == 0 && closingBrace > 0) {

					List<Character> elementsBrace = new ArrayList<>();

					for (char element : url.toCharArray()) {
						if ('[' == element || (']' == element)) elementsBrace.add(element);
					}

					if (elementsBrace.size() % 2 != 0) {
						standard = false;
					} else {
						for (int i = 0; i < elementsBrace.size(); ++i) {
							if ((i%2 == 0 && elementsBrace.get(i)!='[') || (i%2== 1 && elementsBrace.get(i) != ']')) standard = false;
						}
					}
				}
		}
		return (standard);
	}
}
