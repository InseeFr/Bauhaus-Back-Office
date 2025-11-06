package fr.insee.rmes.modules.concepts.collections.domain.port.clientside;

import fr.insee.rmes.modules.concepts.collections.domain.CollectionNotFoundException;
import fr.insee.rmes.modules.concepts.collections.domain.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;

import java.util.List;
import java.util.Optional;

public interface CollectionService {
    List<PartialCollection> getAllCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(String id) throws CollectionsFetchException;
}
