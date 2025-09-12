package fr.insee.rmes.onion.domain.port.clientside;

import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.onion.domain.exceptions.GenericInternalServerException;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.exceptions.operations.NotFoundAttributeException;
import fr.insee.rmes.onion.domain.exceptions.operations.OperationDocumentationRubricWithoutRangeException;

import java.util.List;

public interface DocumentationService {
    DocumentationAttribute getMetadataAttribute(String id) throws RmesException, NotFoundAttributeException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException;

    List<DocumentationAttribute> getMetadataAttributes() throws RmesException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException;
}
