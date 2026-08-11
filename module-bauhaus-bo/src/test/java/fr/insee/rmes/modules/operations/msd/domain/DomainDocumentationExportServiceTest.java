package fr.insee.rmes.modules.operations.msd.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportGoal;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportedFile;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.MetadataExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.SourcesExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationExportGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationExportServiceImplTest {

    @Mock
    private DocumentationExportGateway gateway;

    private DomainDocumentationExportService service;

    @BeforeEach
    void setUp() {
        service = new DomainDocumentationExportService(gateway);
    }

    @Test
    void exportMetadataReport_delegatesToGatewayWithRmesGoal() throws RmesException {
        MetadataExportRequest request = new MetadataExportRequest("42", true, true, true, false);
        ExportedFile expected = file("report");
        when(gateway.exportMetadataReport(request, ExportGoal.RMES)).thenReturn(expected);

        ExportedFile result = service.exportMetadataReport(request);

        assertSame(expected, result);
    }

    @Test
    void exportMetadataReportForLabel_buildsLabelRequest() throws RmesException {
        ExportedFile expected = file("label");
        MetadataExportRequest expectedRequest = new MetadataExportRequest("99", true, true, false, false);
        when(gateway.exportMetadataReport(expectedRequest, ExportGoal.COMITE_LABEL)).thenReturn(expected);

        ExportedFile result = service.exportMetadataReportForLabel("99");

        assertSame(expected, result);
        verify(gateway).exportMetadataReport(expectedRequest, ExportGoal.COMITE_LABEL);
    }

    @Test
    void exportMetadataReportSources_delegatesToGateway() throws RmesException {
        SourcesExportRequest request = new SourcesExportRequest("7", false, true, false);
        ExportedFile expected = file("sources");
        when(gateway.exportMetadataReportSources(request)).thenReturn(expected);

        ExportedFile result = service.exportMetadataReportSources(request);

        assertSame(expected, result);
    }

    private static ExportedFile file(String name) {
        return new ExportedFile(name, ".odt", new ByteArrayResource(new byte[]{1, 2, 3}), "application/octet-stream", null);
    }
}
