package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDescription;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentType;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentDescriptionRepository;

public class DomainDocumentDescriptionService implements DocumentDescriptionService {

    private final DocumentDescriptionRepository repository;

    public DomainDocumentDescriptionService(DocumentDescriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public DocumentDescription getDescription(DocumentKind kind, String id)
            throws RmesException, DocumentNotFoundException {
        DocumentMetadata metadata =
                repository.findMetadata(kind, id).orElseThrow(() -> new DocumentNotFoundException(kind, id));
        DocumentType type = DocumentType.fromRubricConcepts(repository.findRubricConcepts(metadata.uri()));
        return new DocumentDescription(metadata, type);
    }
}
