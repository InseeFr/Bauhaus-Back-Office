package fr.insee.rmes.service.export;

public class ExportUtils {

	public static String getExtension(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			return ".odt";
		} else if (acceptHeader.equals("application/octet-stream")) {
			return ".pdf";
			// default --> Odt
		} else {
			return ".odt";
		}
	}
}
