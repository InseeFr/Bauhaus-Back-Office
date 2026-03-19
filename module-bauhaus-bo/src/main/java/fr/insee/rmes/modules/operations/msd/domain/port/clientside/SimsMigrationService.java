package fr.insee.rmes.modules.operations.msd.domain.port.clientside;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;

public interface SimsMigrationService {
    int migrateHtmlToMarkdown() throws GenericInternalServerException;
    int migratePublicationHtmlToMarkdown() throws GenericInternalServerException;
}