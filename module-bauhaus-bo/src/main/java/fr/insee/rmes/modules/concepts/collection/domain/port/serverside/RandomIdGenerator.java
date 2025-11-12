package fr.insee.rmes.modules.concepts.collection.domain.port.serverside;

import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;

import java.util.UUID;

public class RandomIdGenerator {
    public CollectionId generateCollectionId() {
        return new CollectionId(UUID.randomUUID().toString());
    }
}
