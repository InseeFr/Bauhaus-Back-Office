package fr.insee.rmes.modules.operations.msd.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportGoal;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportedFile;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.MetadataExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.SourcesExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationExportService;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationExportGateway;

public class DocumentationExportServiceImpl implements DocumentationExportService {

    private final DocumentationExportGateway gateway;

    public DocumentationExportServiceImpl(DocumentationExportGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public ExportedFile exportMetadataReport(MetadataExportRequest request) throws RmesException {
        return gateway.exportMetadataReport(request, ExportGoal.RMES);
    }

    @Override
    public ExportedFile exportMetadataReportForLabel(String id) throws RmesException {
        return gateway.exportMetadataReport(
                new MetadataExportRequest(id, true, true, false, false),
                ExportGoal.COMITE_LABEL);
    }

    @Override
    public ExportedFile exportMetadataReportSources(SourcesExportRequest request) throws RmesException {
        return gateway.exportMetadataReportSources(request);
    }
}
