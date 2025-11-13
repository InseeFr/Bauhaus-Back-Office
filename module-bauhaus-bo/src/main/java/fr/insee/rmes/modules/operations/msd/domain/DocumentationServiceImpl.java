package fr.insee.rmes.modules.operations.msd.domain;

import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.DocumentationService;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationRepository;

import java.util.List;

public class DocumentationServiceImpl implements DocumentationService {

    private final DocumentationRepository documentationRepository;

    public DocumentationServiceImpl(DocumentationRepository documentationRepository) {
        this.documentationRepository = documentationRepository;
    }

    @Override
    public DocumentationAttribute getMetadataAttribute(String id) throws NotFoundAttributeException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
        return documentationRepository.getAttributeSpecification(id);
    }

    @Override
    public List<DocumentationAttribute> getMetadataAttributes() throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
        return documentationRepository.getAttributesSpecification();
    }
}
