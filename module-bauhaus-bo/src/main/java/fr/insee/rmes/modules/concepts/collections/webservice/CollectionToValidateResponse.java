package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;

public record CollectionToValidateResponse(String id, String label, String creator) {
    static CollectionToValidateResponse fromDomain(CollectionToValidate item) {
        return new CollectionToValidateResponse(
                item.id().value().toString(),
                item.label(),
                item.creator()
        );
    }
}
