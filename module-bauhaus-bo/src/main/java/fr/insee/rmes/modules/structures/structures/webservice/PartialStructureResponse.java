package fr.insee.rmes.modules.structures.structures.webservice;

import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructure;

public class PartialStructureResponse extends BaseResponse<PartialStructureResponse, PartialStructure> {

    private PartialStructureResponse(PartialStructure domainObject) {
        super(domainObject);
    }

    public static PartialStructureResponse fromDomain(PartialStructure structure) {
        return new PartialStructureResponse(structure);
    }
}
