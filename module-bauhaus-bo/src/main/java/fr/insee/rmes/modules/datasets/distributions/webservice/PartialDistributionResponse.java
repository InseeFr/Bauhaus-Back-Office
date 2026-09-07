package fr.insee.rmes.modules.datasets.distributions.webservice;

import fr.insee.rmes.modules.datasets.distributions.model.PartialDistribution;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;

public class PartialDistributionResponse extends BaseResponse<PartialDistributionResponse, PartialDistribution> {

    private PartialDistributionResponse(PartialDistribution domainObject) {
        super(domainObject);
    }

    public static PartialDistributionResponse fromDomain(PartialDistribution distribution) {
        return new PartialDistributionResponse(distribution);
    }
}