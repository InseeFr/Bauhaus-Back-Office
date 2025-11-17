package fr.insee.rmes.modules.structures.structures.webservice;

import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructure;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialStructureResponse extends BaseResponse<PartialStructureResponse, PartialStructure> {

    private PartialStructureResponse(PartialStructure domainObject) {
        super(domainObject);
    }

    public static PartialStructureResponse fromDomain(PartialStructure structure) {
        return new PartialStructureResponse(structure);
    }
}