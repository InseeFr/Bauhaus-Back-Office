package fr.insee.rmes.modules.concepts.concept.webservice.response;

import fr.insee.rmes.model.concepts.ConceptForAdvancedSearch;
import fr.insee.rmes.webservice.response.BaseResponse;

public class ConceptForAdvancedSearchResponse extends BaseResponse<ConceptForAdvancedSearchResponse, ConceptForAdvancedSearch> {

    private ConceptForAdvancedSearchResponse(ConceptForAdvancedSearch domainObject) {
        super(domainObject);
    }

    public static ConceptForAdvancedSearchResponse fromDomain(ConceptForAdvancedSearch concept) {
        return new ConceptForAdvancedSearchResponse(concept);
    }
}
