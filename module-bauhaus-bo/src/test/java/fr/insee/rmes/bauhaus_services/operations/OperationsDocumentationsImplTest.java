package fr.insee.rmes.bauhaus_services.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationsDocumentationsImplTest {

    @Mock
    private DocumentationsUtils documentationsUtils;

    private OperationsDocumentationsImpl metadataReportService;

    @BeforeEach
    void setUp() {
        metadataReportService =
                new OperationsDocumentationsImpl(null, null, null, null, null, documentationsUtils, null, null, null);
    }

    @Test
    void shouldGetMetadataReportAndReturnJsonString() throws RmesException {
        // Given
        String simsId = "1000";
        JSONObject expectedDocumentation = new JSONObject();
        expectedDocumentation.put("id", simsId);
        expectedDocumentation.put("labelLg1", "Documentation test");
        expectedDocumentation.put("labelLg2", "Test documentation");

        when(documentationsUtils.getDocumentationByIdSims(simsId)).thenReturn(expectedDocumentation);

        // When
        String result = metadataReportService.getMetadataReport(simsId);

        // Then
        verify(documentationsUtils).getDocumentationByIdSims(simsId);
        assertEquals(expectedDocumentation.toString(), result);
    }

    @Test
    void shouldGetMetadataReportWithEmptyJsonObject() throws RmesException {
        // Given
        String simsId = "2000";
        JSONObject emptyDocumentation = new JSONObject();

        when(documentationsUtils.getDocumentationByIdSims(simsId)).thenReturn(emptyDocumentation);

        // When
        String result = metadataReportService.getMetadataReport(simsId);

        // Then
        verify(documentationsUtils).getDocumentationByIdSims(simsId);
        assertEquals("{}", result);
    }

    @Test
    void shouldPropagateRmesExceptionWhenGetMetadataReportFails() throws RmesException {
        // Given
        String simsId = "3000";
        RmesException expectedException =
                new RmesException(404, "Documentation not found", "No documentation with id: " + simsId);

        when(documentationsUtils.getDocumentationByIdSims(simsId)).thenThrow(expectedException);

        // When / Then
        RmesException exception = assertThrows(RmesException.class, () -> {
            metadataReportService.getMetadataReport(simsId);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
        verify(documentationsUtils).getDocumentationByIdSims(simsId);
    }
}
