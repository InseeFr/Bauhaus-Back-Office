package fr.insee.rmes.modules.concepts.concept.webservice.response;

import fr.insee.rmes.model.concepts.PartialConcept;
import fr.insee.rmes.webservice.response.BaseResponse;

public class PartialConceptResponse extends BaseResponse<PartialConceptResponse, PartialConcept> {

    private PartialConceptResponse(PartialConcept domainObject) {
        super(domainObject);
    }

    public static PartialConceptResponse fromDomain(PartialConcept concept) {
        return new PartialConceptResponse(concept);
    }
}
