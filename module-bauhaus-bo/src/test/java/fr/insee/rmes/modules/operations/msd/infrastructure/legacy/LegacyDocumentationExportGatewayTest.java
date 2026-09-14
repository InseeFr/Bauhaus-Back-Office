package fr.insee.rmes.modules.operations.msd.infrastructure.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportGoal;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportedFile;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.MetadataExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.SourcesExportRequest;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class LegacyDocumentationExportGatewayTest {

    @Mock
    private DocumentationExport documentationExport;

    private LegacyDocumentationExportGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new LegacyDocumentationExportGateway(documentationExport, 50);
    }

    @Test
    void exportMetadataReport_mapsResponseToExportedFile_withMissingDocuments() throws RmesException {
        MetadataExportRequest request = new MetadataExportRequest("1", true, true, false, true);
        Resource resource = new ByteArrayResource("body".getBytes());
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .headers(headersWith("ReportFile.zip", MediaType.APPLICATION_OCTET_STREAM, "doc-a, doc-b ,doc-c"))
                .body(resource);
        when(documentationExport.exportMetadataReport("1", true, true, false, true, Constants.GOAL_RMES, 50))
                .thenReturn(response);

        ExportedFile result = gateway.exportMetadataReport(request, ExportGoal.RMES);

        assertEquals("ReportFile", result.filename());
        assertEquals(".zip", result.extension());
        assertSame(resource, result.content());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, result.contentType());
        assertEquals(Set.of("doc-a", "doc-b", "doc-c"), result.missingDocuments());
    }

    @Test
    void exportMetadataReport_translatesLabelGoal() throws RmesException {
        MetadataExportRequest request = new MetadataExportRequest("1", true, true, false, false);
        Resource resource = new ByteArrayResource(new byte[0]);
        ResponseEntity<Resource> response = ResponseEntity.ok()
                .headers(headersWith("Label.odt", MediaType.APPLICATION_OCTET_STREAM, null))
                .body(resource);
        when(documentationExport.exportMetadataReport("1", true, true, false, false, Constants.GOAL_COMITE_LABEL, 50))
                .thenReturn(response);

        ExportedFile result = gateway.exportMetadataReport(request, ExportGoal.COMITE_LABEL);

        assertTrue(result.missingDocuments().isEmpty());
        assertEquals(".odt", result.extension());
    }

    @Test
    void exportMetadataReportSources_delegatesToFilesEndpoint() throws RmesException {
        SourcesExportRequest request = new SourcesExportRequest("1", false, true, true);
        Resource resource = new ByteArrayResource(new byte[] {1});
        ResponseEntity<Object> response = ResponseEntity.ok()
                .headers(headersWith("xmlFiles.zip", MediaType.APPLICATION_OCTET_STREAM, null))
                .body(resource);
        when(documentationExport.exportMetadataReportFiles("1", false, true, true))
                .thenReturn(response);

        ExportedFile result = gateway.exportMetadataReportSources(request);

        assertEquals("xmlFiles", result.filename());
        assertEquals(".zip", result.extension());
    }

    @Test
    void emptyBody_throwsRmesException() throws RmesException {
        MetadataExportRequest request = new MetadataExportRequest("1", true, true, false, false);
        when(documentationExport.exportMetadataReport("1", true, true, false, false, Constants.GOAL_RMES, 50))
                .thenReturn(ResponseEntity.ok().build());

        assertThrows(RmesException.class, () -> gateway.exportMetadataReport(request, ExportGoal.RMES));
    }

    private static HttpHeaders headersWith(String filename, MediaType contentType, String missingDocuments) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build());
        headers.setContentType(contentType);
        if (missingDocuments != null) {
            headers.set("X-Missing-Documents", missingDocuments);
        }
        return headers;
    }
}
