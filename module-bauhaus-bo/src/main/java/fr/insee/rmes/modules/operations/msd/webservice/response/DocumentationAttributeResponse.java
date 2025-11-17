package fr.insee.rmes.modules.operations.msd.webservice.response;

import fr.insee.rmes.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.webservice.response.BaseResponse;

public class DocumentationAttributeResponse extends BaseResponse<DocumentationAttributeResponse, DocumentationAttribute> {

    private DocumentationAttributeResponse(DocumentationAttribute domainObject) {
        super(domainObject);
    }

    public static DocumentationAttributeResponse fromDomain(DocumentationAttribute attribute) {
        return new DocumentationAttributeResponse(attribute);
    }
}
