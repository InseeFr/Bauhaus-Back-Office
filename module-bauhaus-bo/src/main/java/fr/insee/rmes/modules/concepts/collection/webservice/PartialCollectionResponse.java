package fr.insee.rmes.modules.concepts.collection.webservice;

import fr.insee.rmes.modules.concepts.collection.domain.model.PartialCollection;

public record PartialCollectionResponse(String id, String label) {
    static PartialCollectionResponse fromDomain(PartialCollection collection){
        return new PartialCollectionResponse(collection.id().value().toString(), collection.prefLabel().value());
    }
}
