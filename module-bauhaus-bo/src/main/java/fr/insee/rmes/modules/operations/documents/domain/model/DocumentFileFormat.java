package fr.insee.rmes.modules.operations.documents.domain.model;

import java.util.Arrays;
import java.util.Locale;

/** Formats de fichiers diffusés, et le type MIME sous lequel chacun est servi. */
public enum DocumentFileFormat {
    SEVEN_Z("7z", "application/x-7z-compressed"),
    CSV("csv", "text/csv"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    HTML("html", "text/html"),
    ODS("ods", "application/vnd.oasis.opendocument.spreadsheet"),
    ODT("odt", "application/vnd.oasis.opendocument.text"),
    PDF("pdf", "application/pdf"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    ZIP("zip", "application/zip");

    /** Type servi pour une extension absente de la liste : le navigateur propose le téléchargement. */
    public static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

    private final String extension;
    private final String mediaType;

    DocumentFileFormat(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public static String mediaTypeOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return DEFAULT_MEDIA_TYPE;
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(format -> format.extension.equals(extension))
                .map(format -> format.mediaType)
                .findFirst()
                .orElse(DEFAULT_MEDIA_TYPE);
    }
}
