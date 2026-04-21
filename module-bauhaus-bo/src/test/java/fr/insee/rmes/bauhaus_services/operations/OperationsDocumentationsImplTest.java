package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationExport;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsDocumentationsImplTest {

    @Mock
    private DocumentationExport documentationsExport;

    @Mock
    private DocumentationsUtils documentationsUtils;

    @InjectMocks
    private OperationsDocumentationsImpl metadataReportService;

    @Test
    void testExportMetadataReport_Success() throws RmesException {
        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        boolean document = true;
        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(documentationsExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, Constants.GOAL_RMES, 0))
                .thenReturn(ResponseEntity.ok().body(resource));

        ResponseEntity<Resource> response = metadataReportService.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document);
        assertEquals(ResponseEntity.ok().body(resource), response);
    }

    @Test
    void testExportMetadataReport_Failure_NoLanguageSelected() {
        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = false;
        boolean lg2 = false;
        boolean document = true;

        RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> {
            metadataReportService.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document);
        });

        JSONObject formattedException = new JSONObject(exception.getDetails());
        assertEquals(ErrorCodes.SIMS_EXPORT_WITHOUT_LANGUAGE, formattedException.getInt("code"));
        assertEquals("at least one language must be selected for export", formattedException.getString("message"));
        assertEquals("in export of sims: " + id, formattedException.getString("details"));
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
        RmesException expectedException = new RmesException(404, "Documentation not found", "No documentation with id: " + simsId);

        when(documentationsUtils.getDocumentationByIdSims(simsId)).thenThrow(expectedException);

        // When / Then
        RmesException exception = assertThrows(RmesException.class, () -> {
            metadataReportService.getMetadataReport(simsId);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
        verify(documentationsUtils).getDocumentationByIdSims(simsId);
    }

}