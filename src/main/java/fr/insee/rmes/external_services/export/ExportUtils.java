package fr.insee.rmes.external_services.export;

public class ExportUtils {

	public static String getExtension(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			return ".odt";
		} else if (acceptHeader.equals("application/octet-stream")) {
			return ".pdf";
		} else if (acceptHeader.equals("flatODT")) {
			return ".fodt";
		} else if (acceptHeader.equals("XML")) {
			return ".xml";
		} else {
			return ".odt";
			// default --> odt
		}
	}
}
