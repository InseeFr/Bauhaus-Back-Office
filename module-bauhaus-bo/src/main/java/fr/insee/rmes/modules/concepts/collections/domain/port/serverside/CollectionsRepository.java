package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

import fr.insee.rmes.modules.concepts.collections.domain.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;

import java.util.List;
import java.util.Optional;

public interface CollectionsRepository {
    List<PartialCollection> getCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(String id) throws CollectionsFetchException;
}
