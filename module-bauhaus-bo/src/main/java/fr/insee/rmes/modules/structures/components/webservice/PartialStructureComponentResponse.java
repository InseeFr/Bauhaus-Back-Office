package fr.insee.rmes.modules.structures.components.webservice;

import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructureComponent;

public class PartialStructureComponentResponse
        extends BaseResponse<PartialStructureComponentResponse, PartialStructureComponent> {

    private PartialStructureComponentResponse(PartialStructureComponent domainObject) {
        super(domainObject);
    }

    public static PartialStructureComponentResponse fromDomain(PartialStructureComponent component) {
        return new PartialStructureComponentResponse(component);
    }
}
