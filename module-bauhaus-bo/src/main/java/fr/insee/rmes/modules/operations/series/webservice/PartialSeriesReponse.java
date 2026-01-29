package fr.insee.rmes.modules.operations.series.webservice;

import fr.insee.rmes.model.operations.PartialOperationSeries;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialSeriesReponse extends BaseResponse<PartialSeriesReponse, PartialOperationSeries> {

    private PartialSeriesReponse(PartialOperationSeries domainObject) {
        super(domainObject);
    }

    public static PartialSeriesReponse fromDomain(PartialOperationSeries series) {
        return new PartialSeriesReponse(series);
    }
}