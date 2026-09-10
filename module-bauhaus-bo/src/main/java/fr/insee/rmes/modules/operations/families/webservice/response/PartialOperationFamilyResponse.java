package fr.insee.rmes.modules.operations.families.webservice.response;

import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;

public class PartialOperationFamilyResponse
        extends BaseResponse<PartialOperationFamilyResponse, PartialOperationFamily> {

    private PartialOperationFamilyResponse(PartialOperationFamily domainObject) {
        super(domainObject);
    }

    public static PartialOperationFamilyResponse fromDomain(PartialOperationFamily family) {
        return new PartialOperationFamilyResponse(family);
    }
}
