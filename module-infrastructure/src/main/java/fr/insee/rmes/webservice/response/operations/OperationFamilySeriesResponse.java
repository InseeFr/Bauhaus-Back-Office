package fr.insee.rmes.webservice.response.operations;

import fr.insee.rmes.domain.model.operations.families.OperationFamilySeries;
import fr.insee.rmes.webservice.response.BaseResponse;

public class OperationFamilySeriesResponse extends BaseResponse<OperationFamilySeriesResponse, OperationFamilySeries> {

    private OperationFamilySeriesResponse(OperationFamilySeries domainObject) {
        super(domainObject);
    }
    
    public static OperationFamilySeriesResponse fromDomain(OperationFamilySeries series) {
        return new OperationFamilySeriesResponse(series);
    }
}