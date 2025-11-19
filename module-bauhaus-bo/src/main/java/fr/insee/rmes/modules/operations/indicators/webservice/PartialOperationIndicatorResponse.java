package fr.insee.rmes.modules.operations.indicators.webservice;

import fr.insee.rmes.model.operations.PartialOperationIndicator;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialOperationIndicatorResponse extends BaseResponse<PartialOperationIndicatorResponse, PartialOperationIndicator> {

    private PartialOperationIndicatorResponse(PartialOperationIndicator domainObject) {
        super(domainObject);
    }

    public static PartialOperationIndicatorResponse fromDomain(PartialOperationIndicator indicator) {
        return new PartialOperationIndicatorResponse(indicator);
    }
}