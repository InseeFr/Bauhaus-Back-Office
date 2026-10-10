package fr.insee.rmes.modules.operations.documents.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDescription;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;

@ClientSidePort
public interface DocumentDescriptionService {

    DocumentDescription getDescription(DocumentKind kind, String id) throws RmesException, DocumentNotFoundException;
}
