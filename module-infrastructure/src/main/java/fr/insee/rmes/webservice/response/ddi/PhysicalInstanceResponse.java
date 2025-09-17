package fr.insee.rmes.webservice.response.ddi;

import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PhysicalInstanceResponse extends BaseResponse<PhysicalInstanceResponse, PhysicalInstance> {
    public PhysicalInstanceResponse(PhysicalInstance domainObject) {
        super(domainObject);
    }

    public static PhysicalInstanceResponse fromDomain(PhysicalInstance instance) {
        return new PhysicalInstanceResponse(instance);
    }

    public String getId() {
        return getDomainObject().id();
    }

    public String getLabel() {
        return getDomainObject().label();
    }
}