package fr.insee.rmes.utils;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

public class HttpUtils {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ATTACHMENT = "attachment";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static HttpHeaders generateHttpHeaders(String fileName, String extension, int maxLength){
        String contentType = switch (extension){
            case FilesUtils.ODT_EXTENSION -> "application/vnd.oasis.opendocument.text";
            case FilesUtils.ODS_EXTENSION -> "application/vnd.oasis.opendocument.spreadsheet";
            case FilesUtils.ZIP_EXTENSION -> "application/zip";
            default -> throw new IllegalStateException("Unexpected value: " + extension);
        };

        ContentDisposition content = ContentDisposition.builder(HttpUtils.ATTACHMENT).filename(FilesUtils.reduceFileNameSize(fileName, maxLength) + extension).build();

        List<String> allowHeaders = new ArrayList<>();
        allowHeaders.add(CONTENT_DISPOSITION);
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("Access-Control-Allow-Credentials");


        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.ACCEPT, "*/*");
        responseHeaders.setContentDisposition(content);
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
        responseHeaders.setAccessControlExposeHeaders(allowHeaders);
        responseHeaders.add(HttpUtils.CONTENT_TYPE, contentType);
        return responseHeaders;
    }
}
