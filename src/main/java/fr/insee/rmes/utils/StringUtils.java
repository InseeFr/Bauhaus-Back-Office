package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import org.eclipse.rdf4j.model.IRI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
	@NotNull
	public static String urisAsString(List<IRI> uris) {
		return uris.stream().map(StringUtils::uriAsString).reduce(String::concat).orElse("");
	}

	@NotNull
	private static String uriAsString(IRI uri) {
		return "<" + RdfUtils.toString(uri) + ">";
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
