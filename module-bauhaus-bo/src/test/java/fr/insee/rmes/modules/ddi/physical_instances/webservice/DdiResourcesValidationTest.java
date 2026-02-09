package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.ValidationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DDI validation endpoint using a simple mock schema
 */
@ExtendWith(MockitoExtension.class)
class DdiResourcesValidationTest {

    @Mock
    private DDIService ddiService;

    @Mock
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @Mock
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    private DdiResources ddiResources;

    // Simple test schema
    private static final String TEST_SCHEMA = """
            {
              "$schema": "http://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "properties": {
                "$schema": { "type": "string" },
                "topLevelReference": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "agency": { "type": "string" },
                      "id": { "type": "string" },
                      "version": { "type": "string" },
                      "typeOfObject": { "type": "string" }
                    },
                    "required": ["agency", "id", "version", "typeOfObject"]
                  }
                }
              },
              "additionalProperties": false
            }
            """;

    @BeforeEach
    void setUp() {
        ddiResources = new DdiResources(ddiService, ddi4toDdi3ConverterService, ddi3toDdi4ConverterService);

        // Setup mock request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldValidateValidMinimalJson() {
        // Given
        String validJson = """
                {
                    "$schema": "http://localhost:8080/ddi/schema"
                }
                """;

        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
                (mock, _) -> {
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(validJson);

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertTrue(result.getBody().valid());
            assertEquals(0, result.getBody().errors().size());
        }
    }

    @Test
    void shouldValidateValidJsonWithTopLevelReference() {
        // Given
        String validJson = """
                {
                    "$schema": "http://localhost:8080/ddi/schema",
                    "topLevelReference": [
                        {
                            "agency": "fr.insee",
                            "id": "test-id",
                            "version": "1",
                            "typeOfObject": "PhysicalInstance"
                        }
                    ]
                }
                """;

        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
                (mock, _) -> {
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(validJson);

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertTrue(result.getBody().valid());
            assertEquals(0, result.getBody().errors().size());
        }
    }

    @Test
    void shouldRejectInvalidJsonWithAdditionalProperties() {
        // Given - Invalid because additionalProperties is false
        String invalidJson = """
                {
                    "$schema": "http://localhost:8080/ddi/schema",
                    "invalidField": "value"
                }
                """;

        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
                (mock, _) -> {
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(invalidJson);

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody());
            assertFalse(result.getBody().valid());
            assertTrue(!result.getBody().errors().isEmpty());
            assertTrue(result.getBody().errors().size() > 0);
        }
    }

    @Test
    void shouldRejectJsonWithInvalidTopLevelReferenceType() {
        // Given - topLevelReference should be array, not string
        String invalidJson = """
                {
                    "$schema": "http://localhost:8080/ddi/schema",
                    "topLevelReference": "not an array"
                }
                """;

        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
                (mock, _) -> {
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(invalidJson);

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody());
            assertFalse(result.getBody().valid());
            assertTrue(result.getBody().errors().size() > 0);
        }
    }

    @Test
    void shouldRejectTopLevelReferenceWithMissingRequiredFields() {
        // Given - Missing required field "version"
        String invalidJson = """
                {
                    "$schema": "http://localhost:8080/ddi/schema",
                    "topLevelReference": [
                        {
                            "agency": "fr.insee",
                            "id": "test-id",
                            "typeOfObject": "PhysicalInstance"
                        }
                    ]
                }
                """;

        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
                (mock, _) -> {
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(invalidJson);

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody());
            assertFalse(result.getBody().valid());
            assertTrue(result.getBody().errors().size() > 0);
            // The error message should mention the missing field (version)
            String errorMessage = result.getBody().errors().get(0);
            assertTrue(errorMessage.contains("required") || errorMessage.contains("version"),
                    "Error message should mention 'required' or 'version', but was: " + errorMessage);
        }
    }

    @Test
    void shouldRejectMalformedJson() {
        // Given - Malformed JSON
        String malformedJson = "{ invalid json }";

        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(malformedJson);

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
            assertNotNull(result.getBody());
            assertFalse(result.getBody().valid());
            assertEquals(1, result.getBody().errors().size());
            assertTrue(result.getBody().errors().get(0).startsWith("Invalid JSON:"));
        }
    }

    @Test
    void shouldGetDdiSchema() throws IOException {
        // When
        try (MockedConstruction<ClassPathResource> _ = mockConstruction(ClassPathResource.class,
        try (MockedConstruction<ClassPathResource> mockedResource = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.getInputStream())
                            .thenReturn(new ByteArrayInputStream(TEST_SCHEMA.getBytes(StandardCharsets.UTF_8)));
                })) {

            ResponseEntity<String> result = ddiResources.getDdiSchema();

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertFalse(result.getBody().isEmpty());
            assertTrue(result.getBody().length() > 0);
            assertTrue(result.getBody().contains("$schema"));
        }
    }
}