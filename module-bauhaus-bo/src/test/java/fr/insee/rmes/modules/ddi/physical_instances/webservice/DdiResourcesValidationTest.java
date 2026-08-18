package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.DomainDdi4SchemaService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema.NetworkntDdi4SchemaValidator;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.ValidationResponse;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    @Mock
    private DDIItemConvertService ddiItemConvertService;

    @Mock
    private UserProvider userProvider;

    @Mock
    private RbacFetcher rbacFetcher;

    @Mock
    private BauhausUriBuilder bauhausUriBuilder;

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
        // Le vrai schéma DDI 4 est joué par DdiResourcesSchemaValidationTest : ici on injecte un
        // schéma bouchon, ce qui garde ces tests sur le comportement du endpoint.
        Ddi4SchemaRepository schemaRepository = () -> TEST_SCHEMA;
        ddiResources = new DdiResources(ddiService, ddi4toDdi3ConverterService, ddi3toDdi4ConverterService, ddiItemConvertService, userProvider, rbacFetcher, bauhausUriBuilder,
                new DomainDdi4SchemaService(schemaRepository, new NetworkntDdi4SchemaValidator(schemaRepository)));

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
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(validJson);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().valid());
        assertEquals(0, result.getBody().errors().size());
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
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(validJson);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().valid());
        assertEquals(0, result.getBody().errors().size());
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
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(invalidJson);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid());
        assertFalse(result.getBody().errors().isEmpty());
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
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(invalidJson);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid());
        assertFalse(result.getBody().errors().isEmpty());
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
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(invalidJson);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid());
        assertFalse(result.getBody().errors().isEmpty());
        // The error message should mention the missing field (version)
        String errorMessage = result.getBody().errors().get(0);
        assertTrue(errorMessage.contains("required") || errorMessage.contains("version"),
                "Error message should mention 'required' or 'version', but was: " + errorMessage);
    }

    @Test
    void shouldRejectMalformedJson() {
        // Given - Malformed JSON
        String malformedJson = "{ invalid json }";

        // When
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(malformedJson);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid());
        assertEquals(1, result.getBody().errors().size());
        assertTrue(result.getBody().errors().get(0).startsWith("Invalid JSON:"));
    }

    @Test
    void shouldServeTheSchemaDocument() {
        ResponseEntity<String> result = ddiResources.getDdiSchema();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(TEST_SCHEMA, result.getBody());
    }

    /**
     * Un schéma illisible est une panne, pas une saisie fautive : le endpoint ne doit plus la
     * maquiller en 400 comme le faisait son {@code catch (Exception)}.
     */
    @Test
    void shouldNotDisguiseASchemaFailureAsAValidationError() {
        Ddi4SchemaService failing = mock(Ddi4SchemaService.class);
        when(failing.validate(any())).thenThrow(new IllegalStateException("Schéma DDI 4 illisible"));
        DdiResources resources = new DdiResources(ddiService, ddi4toDdi3ConverterService,
                ddi3toDdi4ConverterService, ddiItemConvertService, userProvider, rbacFetcher,
                bauhausUriBuilder, failing);

        assertThrows(IllegalStateException.class, () -> resources.validateDdi4("{}"));
    }
}
