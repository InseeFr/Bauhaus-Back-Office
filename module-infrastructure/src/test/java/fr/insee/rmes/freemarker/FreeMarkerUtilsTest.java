package fr.insee.rmes.freemarker;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FreeMarkerUtilsTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Path requestDir = tempDir.resolve("request");
        Files.createDirectories(requestDir);
        
        Path testTemplate = requestDir.resolve("test.ftl");
        Files.write(testTemplate, "Hello ${name}!".getBytes());
    }


    @Test
    void shouldBuildRequestWithValidTemplate() throws RmesException {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");

        assertThrows(RmesException.class, () -> {
            FreeMarkerUtils.buildRequest("", "nonexistent.ftl", params);
        });
    }

    @Test
    void shouldThrowRmesExceptionWhenTemplateNotFound() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "World");

        assertThrows(RmesException.class, () -> {
            FreeMarkerUtils.buildRequest("", "nonexistent.ftl", params);
        });
    }

    @Test
    void shouldThrowRmesExceptionWithEmptyParams() {
        // Given
        Map<String, Object> params = new HashMap<>();

        // When & Then
        assertThrows(RmesException.class, () -> {
            FreeMarkerUtils.buildRequest("", "nonexistent.ftl", params);
        });
    }

    @Test
    void shouldThrowRmesExceptionWithNullParams() {
        // When & Then
        assertThrows(RmesException.class, () -> {
            FreeMarkerUtils.buildRequest("", "nonexistent.ftl", null);
        });
    }

    @Test
    void shouldHandleEmptyRootPath() {
        Map<String, Object> params = new HashMap<>();
        params.put("test", "value");

        assertThrows(RmesException.class, () -> {
            FreeMarkerUtils.buildRequest("", "test.ftl", params);
        });
    }

    @Test
    void shouldHandleNullRootPath() {
        Map<String, Object> params = new HashMap<>();
        params.put("test", "value");

        assertThrows(RmesException.class, () -> {
            FreeMarkerUtils.buildRequest(null, "test.ftl", params);
        });
    }
}