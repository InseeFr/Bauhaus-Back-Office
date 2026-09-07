package fr.insee.rmes.modules.operations.msd.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;

import fr.insee.rmes.modules.operations.msd.domain.model.DocumentationAttribute;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.NotFoundAttributeException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;

import java.util.List;

@ServerSidePort
public interface DocumentationRepository {
    List<DocumentationAttribute> getAttributesSpecification() throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException;
    DocumentationAttribute getAttributeSpecification(String id) throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException, NotFoundAttributeException;
}
