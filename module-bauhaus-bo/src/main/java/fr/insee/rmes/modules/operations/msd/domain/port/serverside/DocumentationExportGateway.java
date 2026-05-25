package fr.insee.rmes.modules.operations.msd.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportGoal;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportedFile;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.MetadataExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.SourcesExportRequest;

public interface DocumentationExportGateway {

    ExportedFile exportMetadataReport(MetadataExportRequest request, ExportGoal goal) throws RmesException;

    ExportedFile exportMetadataReportSources(SourcesExportRequest request) throws RmesException;
}
