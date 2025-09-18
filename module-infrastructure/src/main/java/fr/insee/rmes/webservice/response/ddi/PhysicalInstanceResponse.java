package fr.insee.rmes.webservice.response.ddi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.insee.rmes.domain.model.ddi.PhysicalInstance;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PhysicalInstanceResponse extends BaseResponse<PhysicalInstanceResponse, PhysicalInstance> {
    public PhysicalInstanceResponse(PhysicalInstance domainObject) {
        super(domainObject);
    }

    public static PhysicalInstanceResponse fromDomain(PhysicalInstance instance) {
        return new PhysicalInstanceResponse(instance);
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