package fr.insee.rmes.modules.operations.families.webservice.response;

import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;

public class PartialOperationFamilyResponse extends BaseResponse<PartialOperationFamilyResponse, PartialOperationFamily> {

    private PartialOperationFamilyResponse(PartialOperationFamily domainObject) {
        super(domainObject);
    }
    
    public static PartialOperationFamilyResponse fromDomain(PartialOperationFamily family) {
        return new PartialOperationFamilyResponse(family);
    }
}
