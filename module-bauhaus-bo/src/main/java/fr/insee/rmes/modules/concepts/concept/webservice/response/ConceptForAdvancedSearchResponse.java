package fr.insee.rmes.modules.concepts.concept.webservice.response;

import fr.insee.rmes.modules.commons.webservice.BaseResponse;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptForAdvancedSearch;

public class ConceptForAdvancedSearchResponse
        extends BaseResponse<ConceptForAdvancedSearchResponse, ConceptForAdvancedSearch> {

    private ConceptForAdvancedSearchResponse(ConceptForAdvancedSearch domainObject) {
        super(domainObject);
    }

    public static ConceptForAdvancedSearchResponse fromDomain(ConceptForAdvancedSearch concept) {
        return new ConceptForAdvancedSearchResponse(concept);
    }
}
