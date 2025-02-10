package fr.insee.rmes.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
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

	public static boolean isValiURL(String url) {
		boolean conforme = false;
		if (url != null) {

			List<String> referentiel = List.of("http:","https:");
			List<String> detailsURL = Arrays.asList(url.split("//"));
			if (referentiel.contains(detailsURL.getFirst()) && detailsURL.size()>=2) conforme = true;

			if (url.startsWith("https://]") || url.startsWith("https://|") || url.startsWith("https:// ")  ) conforme= false;
			if (url.startsWith("http://]") || url.startsWith("http://|") || url.startsWith("http:// ") ) conforme= false;

			int openingBrace = org.springframework.util.StringUtils.countOccurrencesOf(url,"[");
			int closingBrace = org.springframework.util.StringUtils.countOccurrencesOf(url,"]");

			if(openingBrace==0 && closingBrace==0){
				if (url.startsWith("https://") && url.length()>=9 && url.substring(8).contains(":")) conforme= false;
				if (url.startsWith("http://") && url.length()>=8 && url.substring(7).contains(":")) conforme= false;

			}

			if(openingBrace-closingBrace!=0){conforme= false;
			}else {
				char order =' ';
				for( char element : url.toCharArray()){
					if ('['==element || (']'==element) ) {
						if (order==element) conforme = false;
						else {order=element;}
					}
				}
			}

			if(url.startsWith("http://http://") || url.startsWith("https://\\\\") || url.startsWith("http://\\\\") ){conforme= true;}

		}
		return (conforme);
	}
}
