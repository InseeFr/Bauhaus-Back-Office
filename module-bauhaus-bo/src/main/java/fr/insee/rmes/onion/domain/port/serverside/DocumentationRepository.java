package fr.insee.rmes.onion.domain.port.serverside;

import fr.insee.rmes.onion.domain.exceptions.GenericInternalServerException;
import fr.insee.rmes.onion.domain.exceptions.operations.NotFoundAttributeException;
import fr.insee.rmes.onion.domain.exceptions.operations.OperationDocumentationRubricWithoutRangeException;
import fr.insee.rmes.onion.domain.model.operations.DocumentationAttribute;

import java.util.List;

public interface DocumentationRepository {
    List<DocumentationAttribute> getAttributesSpecification() throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException;
    DocumentationAttribute getAttributeSpecification(String id) throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException, NotFoundAttributeException;
}
