package fr.insee.rmes.external_services.export;

public class ExportUtils {

	public static String getExtension(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			return ".odt";
		} else if (acceptHeader.equals("application/octet-stream")) {
			return ".pdf";
			// default --> Odt
		} else if (acceptHeader.equals("flatODT")) {
			return ".fodt";
		} else {
			return ".odt";
		}
	}
}
