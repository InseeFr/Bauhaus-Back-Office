package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;

public record PartialCollectionResponse(String id, String label) {
    static PartialCollectionResponse fromDomain(PartialCollection collection){
        return new PartialCollectionResponse(collection.id().value().toString(), collection.prefLabel().value());
    }
}
