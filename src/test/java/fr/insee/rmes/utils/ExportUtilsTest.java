package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(properties = {
        "fr.insee.rmes.bauhaus.filenames.maxlength=2"
})
class ExportUtilsTest {

    @Autowired
    ExportUtils exportUtils;

    @Test
    void shouldMakeFileNameSmallerWhenExportingOdtFile() throws RmesException {

        try (
                MockedStatic<XsltUtils> mockedFactory = Mockito.mockStatic(XsltUtils.class);
                MockedStatic<Files> filesFactory = Mockito.mockStatic(Files.class)
        ) {
            mockedFactory.when(() -> XsltUtils.xsltTransform(any(), any(), any(), any(), any())).thenAnswer((Answer<Void>) invocation -> null);
            mockedFactory.when(() -> XsltUtils.createOdtFromXml(any(), any(), any(), any())).thenAnswer((Answer<Void>) invocation -> null);
            filesFactory.when(() -> Files.createTempDirectory(any())).thenReturn(Path.of(""));
            filesFactory.when(() -> Files.newInputStream(any())).thenReturn(IOUtils.toInputStream("input-stream"));

            ResponseEntity<Resource> response = exportUtils.exportAsResponse("file.odt", new HashMap<>(), "file", "pattern", "zip", "objectType");
            HttpHeaders headers = response.getHeaders();
            assertEquals("attachment; filename=\"fi.odt\"", headers.get("Content-Disposition").get(0));
        }

    }
}