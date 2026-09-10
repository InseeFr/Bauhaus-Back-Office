package fr.insee.rmes.modules.operations.msd.webservice.response;

import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.operations.msd.domain.model.DocumentationAttribute;

public class DocumentationAttributeResponse
        extends BaseResponse<DocumentationAttributeResponse, DocumentationAttribute> {

    private DocumentationAttributeResponse(DocumentationAttribute domainObject) {
        super(domainObject);
    }

    public static DocumentationAttributeResponse fromDomain(DocumentationAttribute attribute) {
        return new DocumentationAttributeResponse(attribute);
    }
}
