package fr.insee.rmes.webservice.response.operations;

import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialOperationFamilyResponse extends BaseResponse<PartialOperationFamilyResponse, PartialOperationFamily> {

    private PartialOperationFamilyResponse(PartialOperationFamily domainObject) {
        super(domainObject);
    }
    
    public static PartialOperationFamilyResponse fromDomain(PartialOperationFamily family) {
        return new PartialOperationFamilyResponse(family);
    }
}
