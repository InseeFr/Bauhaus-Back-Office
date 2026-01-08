package fr.insee.rmes.modules.geographies.webservice.response;

import fr.insee.rmes.modules.geographies.model.GeoFeature;
import fr.insee.rmes.webservice.response.BaseResponse;

public class TerritoryResponse extends BaseResponse<TerritoryResponse, GeoFeature> {

    private TerritoryResponse(GeoFeature domainObject) {
        super(domainObject);
    }

    public static TerritoryResponse fromDomain(GeoFeature geoFeature) {
        return new TerritoryResponse(geoFeature);
    }
}
