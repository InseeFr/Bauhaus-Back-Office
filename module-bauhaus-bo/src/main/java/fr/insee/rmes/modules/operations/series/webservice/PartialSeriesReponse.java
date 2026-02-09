package fr.insee.rmes.modules.operations.series.webservice;

import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.model.operations.PartialOperationSeries;
import fr.insee.rmes.webservice.response.BaseResponse;
import fr.insee.rmes.webservice.response.operations.PartialOperationFamilyResponse;

public class PartialSeriesReponse extends BaseResponse<PartialSeriesReponse, PartialOperationSeries> {

    private PartialSeriesReponse(PartialOperationSeries domainObject) {
        super(domainObject);
    }

    public static PartialSeriesReponse fromDomain(PartialOperationSeries series) {
        return new PartialSeriesReponse(series);
    }
}