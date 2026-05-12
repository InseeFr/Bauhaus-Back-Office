package fr.insee.rmes.modules.concepts.concept.webservice.response;

import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptToValidate;

public record ConceptToValidateResponse(String id, String label, String creator) {

    public static ConceptToValidateResponse fromDomain(ConceptToValidate item) {
        return new ConceptToValidateResponse(item.id().value(), item.label(), item.creator());
    }
}
