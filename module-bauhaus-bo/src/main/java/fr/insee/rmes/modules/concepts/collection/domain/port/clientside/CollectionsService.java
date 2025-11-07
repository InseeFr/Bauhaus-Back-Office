package fr.insee.rmes.modules.concepts.collection.domain.port.clientside;

import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collection.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collection.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collection.domain.model.commands.CreateCollectionCommand;

import java.util.List;
import java.util.Optional;

public interface CollectionsService {
    List<PartialCollection> getAllCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException;
    CollectionId createCollection(CreateCollectionCommand collectionCommand) throws CollectionsSaveException;
}
