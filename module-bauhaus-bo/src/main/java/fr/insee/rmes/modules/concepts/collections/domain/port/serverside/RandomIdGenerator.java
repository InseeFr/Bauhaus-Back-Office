package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;

import java.util.UUID;

public class RandomIdGenerator {
    public CollectionId generateCollectionId() {
        return new CollectionId(UUID.randomUUID().toString());
    }
}
