package fr.insee.rmes.modules.classifications.nomenclatures.webservice.response;

import fr.insee.rmes.modules.classifications.nomenclatures.model.PartialClassification;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialClassificationResponse extends BaseResponse<PartialClassificationResponse, PartialClassification> {

    private PartialClassificationResponse(PartialClassification domainObject) {
        super(domainObject);
    }

    public static PartialClassificationResponse fromDomain(PartialClassification classification) {
        return new PartialClassificationResponse(classification);
    }
}
