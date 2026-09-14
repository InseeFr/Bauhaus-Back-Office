package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodeListScheme;

public class PartialCodeListSchemeResponse extends BaseResponse<PartialCodeListSchemeResponse, PartialCodeListScheme> {
    public PartialCodeListSchemeResponse(PartialCodeListScheme codeListScheme) {
        super(codeListScheme);
    }

    public static PartialCodeListSchemeResponse fromDomain(PartialCodeListScheme codeListScheme) {
        return new PartialCodeListSchemeResponse(codeListScheme);
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
