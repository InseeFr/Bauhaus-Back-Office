package fr.insee.rmes.webservice.response.ddi;

import fr.insee.rmes.domain.model.ddi.PartialPhysicalInstance;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialPhysicalInstanceResponse extends BaseResponse<PartialPhysicalInstanceResponse, PartialPhysicalInstance> {
    public PartialPhysicalInstanceResponse(PartialPhysicalInstance pi) {
       super(pi);
    }

    public static PartialPhysicalInstanceResponse fromDomain(PartialPhysicalInstance instance) {
        return new PartialPhysicalInstanceResponse(instance);
    }

    public String getId() {
        return getDomainObject().id();
    }

    public String getLabel() {
        return getDomainObject().label();
    }
}