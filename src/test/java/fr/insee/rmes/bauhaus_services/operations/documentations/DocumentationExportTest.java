package fr.insee.rmes.bauhaus_services.operations.documentations;


import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.FilesUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationExportTest {
    @Mock
    private SeriesUtils seriesUtils;

    @Mock
    private OperationsUtils operationsUtils;

    @Mock
    private IndicatorsUtils indicatorsUtils;

    @Mock
    private ExportUtils exportUtils;

    @Mock
    private CodeListService codeListService;

    @Mock
    private ParentUtils parentUtils;

    @Mock
    private DocumentationsUtils documentationsUtils;

    @Mock
    private OrganizationsService organizationsService;

    @Mock
    private DocumentsUtils documentsUtils;

    @Test
    public void testExportAsZip_success() throws Exception {
        JSONObject document = new JSONObject();
        document.put("url", "file://doc.doc");
        document.put("id", "1");

        when(documentsUtils.getDocumentsUriAndUrlForSims("sims123")).thenReturn(new JSONArray().put(document));
        var sims = new JSONObject();
        sims.put("id", "sims123");
        sims.put("labelLg1", "simsLabel");

        var xmlContent = new HashMap<String, String>();
        var xslFile = "xslFile";
        var xmlPattern = "xmlPattern";
        var zip = "zip";
        var objectType = "objectType";

        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, documentationsUtils );


        InputStream inputStreamMock = mock(InputStream.class);
        when(exportUtils.exportAsInputStream(eq("simslabel"), eq(xmlContent), eq(xslFile), eq(xmlPattern), eq(zip), eq(objectType), eq(FilesUtils.ODT_EXTENSION)))
                .thenReturn(inputStreamMock);
        when(inputStreamMock.readAllBytes()).thenReturn(new byte[0]);

        ResponseEntity<Resource> response = documentationExport.exportAsZip(sims, xmlContent, xslFile, xmlPattern, zip, objectType, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getHeaders().get("X-Missing-Documents").get(0), "1");
    }

    @Test
    public void  testExportMetadataReport_Success_WithoutDocuments_Label() throws RmesException {
        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, documentationsUtils );

        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = true;
        boolean document = false;
        String goal = Constants.GOAL_COMITE_LABEL;
        String targetType = "someTargetType";

        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(documentationsUtils.getDocumentationByIdSims(id)).thenReturn(new JSONObject().put("labelLg1", "labelLg1"));
        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{targetType, "someId"});
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());
        when(exportUtils.exportAsODT(any(), any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().body(resource));

        ResponseEntity<Resource> response = documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal, 100);
        assertEquals(ResponseEntity.ok().body(resource), response);
    }

    @Test
    public void testExportMetadataReport_Failure_UnknownGoal() throws RmesException {
        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, documentationsUtils );

        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        boolean document = true;
        String goal = "unknownGoal";

        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{"someTargetType", "someId"});
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());

        RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> {
            documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal, 100);
        });

        assertEquals("{\"message\":\"The goal is unknown\"}", exception.getDetails());
    }

    @Test
    public void testExportXmlFiles_Success() throws RmesException {
        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, documentationsUtils );

        Map<String, String> xmlContent = new HashMap<>();
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        String targetType = "someTargetType";

        when(exportUtils.exportFilesAsResponse(any())).thenReturn(ResponseEntity.ok().body("Mocked File Content"));

        ResponseEntity<Object> response = documentationExport.exportXmlFiles(xmlContent, targetType, includeEmptyMas, lg1, lg2);
        assertEquals(ResponseEntity.ok().body("Mocked File Content"), response);
    }
}