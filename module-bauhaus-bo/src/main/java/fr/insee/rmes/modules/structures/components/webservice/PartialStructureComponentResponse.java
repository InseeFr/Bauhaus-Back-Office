package fr.insee.rmes.modules.structures.components.webservice;

import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructureComponent;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialStructureComponentResponse extends BaseResponse<PartialStructureComponentResponse, PartialStructureComponent> {

    private PartialStructureComponentResponse(PartialStructureComponent domainObject) {
        super(domainObject);
    }

    public static PartialStructureComponentResponse fromDomain(PartialStructureComponent component) {
        return new PartialStructureComponentResponse(component);
    }
}
