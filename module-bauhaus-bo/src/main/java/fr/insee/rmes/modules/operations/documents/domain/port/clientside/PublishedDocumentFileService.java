package fr.insee.rmes.modules.operations.documents.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.PublishedFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.PublishedFile;

@ClientSidePort
public interface PublishedDocumentFileService {

    PublishedFile getPublishedFile(String fileName) throws PublishedFileNotFoundException;
}
