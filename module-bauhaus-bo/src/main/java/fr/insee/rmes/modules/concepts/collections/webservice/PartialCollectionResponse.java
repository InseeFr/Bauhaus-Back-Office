package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;

public record PartialCollectionResponse(String id, LocalisedLabelResponse label) {
    static PartialCollectionResponse fromDomain(CompactCollection collection){
        return new PartialCollectionResponse(collection.id().value().toString(), LocalisedLabelResponse.fromDomain(collection.prefLabel()));
    }
}
