package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpUtilsTest {

    @Test
    public void testGenerateHttpHeaders() {
        String fileName = "tresoeTres";
        String extension = ".zip";
        String reducedFileName = "tresoeTres";

        // Call the method to test
        HttpHeaders headers = HttpUtils.generateHttpHeaders(fileName, extension);

        // Verify the results
        assertNotNull(headers);
        assertEquals(new MediaType("application", "zip"), headers.getContentType());

        ContentDisposition contentDisposition = headers.getContentDisposition();
        assertEquals(HttpUtils.ATTACHMENT, contentDisposition.getType());
        assertEquals(reducedFileName + extension, contentDisposition.getFilename());

        List<String> exposeHeaders = headers.getAccessControlExposeHeaders();
        assertNotNull(exposeHeaders);
        assertEquals(4, exposeHeaders.size());
        assertEquals(HttpUtils.CONTENT_DISPOSITION, exposeHeaders.get(0));
        assertEquals("X-Missing-Documents", exposeHeaders.get(1));
        assertEquals("Access-Control-Allow-Origin", exposeHeaders.get(2));
        assertEquals("Access-Control-Allow-Credentials", exposeHeaders.get(3));
    }
}