package fr.insee.rmes.modules.operations.operations.webservice;

import fr.insee.rmes.model.operations.PartialOperation;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialOperationResponse extends BaseResponse<PartialOperationResponse, PartialOperation> {

    private PartialOperationResponse(PartialOperation domainObject) {
        super(domainObject);
    }

    public static PartialOperationResponse fromDomain(PartialOperation operation) {
        return new PartialOperationResponse(operation);
    }
}
