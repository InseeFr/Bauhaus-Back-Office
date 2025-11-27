package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialCodesListResponse extends BaseResponse<PartialCodesListResponse, PartialCodesList> {
    public PartialCodesListResponse(PartialCodesList cl) {
       super(cl);
    }

    public static PartialCodesListResponse fromDomain(PartialCodesList instance) {
        return new PartialCodesListResponse(instance);
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