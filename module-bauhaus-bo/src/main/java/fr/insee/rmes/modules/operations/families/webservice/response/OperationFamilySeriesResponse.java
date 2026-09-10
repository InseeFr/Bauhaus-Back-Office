package fr.insee.rmes.modules.operations.families.webservice.response;

import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeries;

public class OperationFamilySeriesResponse extends BaseResponse<OperationFamilySeriesResponse, OperationFamilySeries> {

    private OperationFamilySeriesResponse(OperationFamilySeries domainObject) {
        super(domainObject);
    }

    public static OperationFamilySeriesResponse fromDomain(OperationFamilySeries series) {
        return new OperationFamilySeriesResponse(series);
    }
}
