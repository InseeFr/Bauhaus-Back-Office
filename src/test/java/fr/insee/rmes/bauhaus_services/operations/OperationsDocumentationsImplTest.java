package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationExport;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class OperationsDocumentationsImplTest {

    @MockBean
    private DocumentationExport documentationsExport;

    @Autowired
    private OperationsDocumentationsService metadataReportService;

    @Test
    public void testExportMetadataReport_Success() throws RmesException {
        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        boolean document = true;
        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(documentationsExport.exportMetadataReport(eq(id), eq(includeEmptyMas), eq(lg1), eq(lg2), eq(document), eq(Constants.GOAL_RMES)))
                .thenReturn(ResponseEntity.ok().body(resource));

        ResponseEntity<Resource> response = metadataReportService.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document);
        assertEquals(ResponseEntity.ok().body(resource), response);
    }

    @Test
    public void testExportMetadataReport_Failure_NoLanguageSelected() {
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

}