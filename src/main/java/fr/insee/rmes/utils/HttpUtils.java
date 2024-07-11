package fr.insee.rmes.utils;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

public final class HttpUtils {
    public static final String ATTACHMENT = "attachment";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    private HttpUtils() {
    }

    public static HttpHeaders generateHttpHeaders(String fileName, String extension, int maxLength) {
        MediaType contentType = FilesUtils.getMediaTypeFromExtension(extension);

        ContentDisposition content = ContentDisposition.builder(HttpUtils.ATTACHMENT).filename(
                FilesUtils.reduceFileNameSize(
                        FilesUtils.removeAsciiCharacters(fileName), maxLength) + extension
                ).build();

        List<String> allowHeaders = List.of(CONTENT_DISPOSITION,
                "X-Missing-Documents",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials");


        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setAccept(List.of(MediaType.ALL));
        responseHeaders.setContentDisposition(content);
        responseHeaders.setContentType(contentType);
        responseHeaders.setAccessControlExposeHeaders(allowHeaders);
        return responseHeaders;
    }

}
