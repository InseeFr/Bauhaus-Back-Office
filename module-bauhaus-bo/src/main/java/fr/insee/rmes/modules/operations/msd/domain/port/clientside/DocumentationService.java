package fr.insee.rmes.modules.operations.msd.domain.port.clientside;

import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.NotFoundAttributeException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;

import java.util.List;

public interface DocumentationService {
    DocumentationAttribute getMetadataAttribute(String id) throws RmesException, NotFoundAttributeException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException;

    List<DocumentationAttribute> getMetadataAttributes() throws RmesException, GenericInternalServerException, OperationDocumentationRubricWithoutRangeException;
}
