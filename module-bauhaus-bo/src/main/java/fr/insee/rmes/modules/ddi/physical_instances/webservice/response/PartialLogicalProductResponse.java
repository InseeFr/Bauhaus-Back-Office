package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialLogicalProduct;

public class PartialLogicalProductResponse extends BaseResponse<PartialLogicalProductResponse, PartialLogicalProduct> {
    public PartialLogicalProductResponse(PartialLogicalProduct lp) {
        super(lp);
    }

    public static PartialLogicalProductResponse fromDomain(PartialLogicalProduct product) {
        return new PartialLogicalProductResponse(product);
    }

    @JsonIgnore
    public String getId() {
        return getDomainObject().id();
    }

    @JsonIgnore
    public String getLabel() {
        return getDomainObject().label();
    }
}
