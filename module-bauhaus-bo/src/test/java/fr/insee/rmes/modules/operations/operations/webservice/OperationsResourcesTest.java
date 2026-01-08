package fr.insee.rmes.modules.operations.operations.webservice;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.PartialOperation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsResourcesTest {

    @Mock
    private OperationsService operationsService;

    @Mock
    private OperationsDocumentationsService documentationsService;

    private OperationsResources operationsResources;

    @BeforeEach
    void setUp() {
        operationsResources = new OperationsResources(operationsService, documentationsService);
    }

    @Test
    void shouldGetOperations() throws RmesException {
        // Given
        PartialOperation operation1 = new PartialOperation(
                "op1", "Label 1", "http://example.com/operation1", "series1", "Alt 1"
        );
        PartialOperation operation2 = new PartialOperation(
                "op2", "Label 2", "http://example.com/operation2", "series2", "Alt 2"
        );
        List<PartialOperation> expectedOperations = Arrays.asList(operation1, operation2);
        when(operationsService.getOperations()).thenReturn(expectedOperations);

        // When
        ResponseEntity<List<PartialOperationResponse>> result = operationsResources.getOperations();

        // Then
        Assertions.assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        verify(operationsService, times(1)).getOperations();
    }

    @Test
    void should_get_operations_with_hateoas_links() throws RmesException {
        // Given
        PartialOperation operation1 = new PartialOperation(
                "op1", "Label 1", "http://example.com/operation1", "series1", "Alt 1"
        );
        List<PartialOperation> expectedOperations = List.of(operation1);
        when(operationsService.getOperations()).thenReturn(expectedOperations);

        // When
        ResponseEntity<List<PartialOperationResponse>> result = operationsResources.getOperations();

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());

        PartialOperationResponse operationResponse = result.getBody().get(0);
        Assertions.assertTrue(operationResponse.hasLinks());
        Assertions.assertTrue(operationResponse.getLink("self").isPresent());

        // Verify the self link URL
        String selfLink = operationResponse.getLink("self").get().getHref();
        Assertions.assertTrue(selfLink.contains("/operations/operation/op1"),
                "Self link should contain '/operations/operation/op1' but was: " + selfLink);

        verify(operationsService, times(1)).getOperations();
    }

    @Test
    void should_return_empty_list_when_no_operations() throws RmesException {
        // Given
        when(operationsService.getOperations()).thenReturn(List.of());

        // When
        ResponseEntity<List<PartialOperationResponse>> result = operationsResources.getOperations();

        // Then
        Assertions.assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(MediaTypes.HAL_JSON, result.getHeaders().getContentType());
        Assertions.assertNotNull(result.getBody());
        assertEquals(0, result.getBody().size());
        verify(operationsService, times(1)).getOperations();
    }
}
