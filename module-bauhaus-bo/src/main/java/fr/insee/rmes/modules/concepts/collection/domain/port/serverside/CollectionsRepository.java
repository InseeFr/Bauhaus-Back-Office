package fr.insee.rmes.modules.concepts.collection.domain.port.serverside;

import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collection.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collection.domain.model.PartialCollection;

import java.util.List;
import java.util.Optional;

public interface CollectionsRepository {
    List<PartialCollection> getCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException;
    void save(Collection collection) throws CollectionsSaveException;
}
