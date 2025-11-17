package fr.insee.rmes.modules.classifications.nomenclatures.webservice.response;

import fr.insee.rmes.modules.classifications.series.model.PartialClassificationSeries;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialClassificationSeriesResponse extends BaseResponse<PartialClassificationSeriesResponse, PartialClassificationSeries> {

    private PartialClassificationSeriesResponse(PartialClassificationSeries domainObject) {
        super(domainObject);
    }

    public static PartialClassificationSeriesResponse fromDomain(PartialClassificationSeries series) {
        return new PartialClassificationSeriesResponse(series);
    }
}
