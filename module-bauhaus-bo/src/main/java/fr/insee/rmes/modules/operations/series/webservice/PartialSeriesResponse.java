package fr.insee.rmes.modules.operations.series.webservice;

import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.model.operations.PartialOperationSeries;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.operations.families.webservice.response.PartialOperationFamilyResponse;

public class PartialSeriesResponse extends BaseResponse<PartialSeriesResponse, PartialOperationSeries> {

    private PartialSeriesResponse(PartialOperationSeries domainObject) {
        super(domainObject);
    }

    public static PartialSeriesResponse fromDomain(PartialOperationSeries series) {
        return new PartialSeriesResponse(series);
    }
}