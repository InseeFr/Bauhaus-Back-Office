package fr.insee.rmes.bauhaus_services.operations.documentations;


import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.utils.ExportUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationExportTest {
    @Mock
    private ExportUtils exportUtils;

    @Mock
    private ParentUtils parentUtils;

    @Mock
    private DocumentationsUtils documentationsUtils;

    @Mock
    private OrganizationsService organizationsService;

    @InjectMocks
    private DocumentationExport documentationExport;

    @Test
    public void testExportMetadataReport_Success_WithDocuments_RMES() throws RmesException {
        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        boolean document = true;
        String goal = Constants.GOAL_RMES;
        String targetType = "someTargetType";

        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{targetType, "someId"});
        when(documentationsUtils.getDocumentationByIdSims(id)).thenReturn(new JSONObject());
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());
        when(exportUtils.exportAsZip(any(), any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().body(resource));

        ResponseEntity<Resource> response = documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal);
        assertEquals(ResponseEntity.ok().body(resource), response);
    }

    @Test
    public void testExportMetadataReport_Success_WithoutDocuments_Label() throws RmesException {
        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = true;
        boolean document = false;
        String goal = Constants.GOAL_COMITE_LABEL;
        String targetType = "someTargetType";

        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{targetType, "someId"});
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());
        when(exportUtils.exportAsResponse(any(), any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().body(resource));

        ResponseEntity<Resource> response = documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal);
        assertEquals(ResponseEntity.ok().body(resource), response);
    }

    @Test
    public void testExportMetadataReport_Failure_UnknownGoal() throws RmesException {
        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        boolean document = true;
        String goal = "unknownGoal";

        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{"someTargetType", "someId"});
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());

        RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> {
            documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal);
        });

        assertEquals("{\"message\":\"The goal is unknown\"}", exception.getDetails());
    }

    @Test
    public void testExportXmlFiles_Success() throws RmesException {
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