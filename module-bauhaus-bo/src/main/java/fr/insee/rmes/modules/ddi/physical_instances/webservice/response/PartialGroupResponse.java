package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialGroupResponse extends BaseResponse<PartialGroupResponse, PartialGroup> {
    public PartialGroupResponse(PartialGroup group) {
       super(group);
    }

    public static PartialGroupResponse fromDomain(PartialGroup instance) {
        return new PartialGroupResponse(instance);
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
