package fr.insee.rmes.modules.classifications.nomenclatures.webservice.response;

import fr.insee.rmes.modules.classifications.families.model.PartialClassificationFamily;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialClassificationFamilyResponse extends BaseResponse<PartialClassificationFamilyResponse, PartialClassificationFamily> {

    private PartialClassificationFamilyResponse(PartialClassificationFamily domainObject) {
        super(domainObject);
    }

    public static PartialClassificationFamilyResponse fromDomain(PartialClassificationFamily family) {
        return new PartialClassificationFamilyResponse(family);
    }
}
