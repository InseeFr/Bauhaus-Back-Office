package fr.insee.rmes.modules.operations.msd.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportedFile;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.MetadataExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.SourcesExportRequest;

public interface DocumentationExportService {

    ExportedFile exportMetadataReport(MetadataExportRequest request) throws RmesException;

    ExportedFile exportMetadataReportForLabel(String id) throws RmesException;

    ExportedFile exportMetadataReportSources(SourcesExportRequest request) throws RmesException;
}
