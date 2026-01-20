package fr.insee.rmes.modules.concepts.collections.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ClientSidePort
public interface CollectionsService {
    List<CompactCollection> getAllCollections() throws CollectionsFetchException;
    List<CompactCollection> getUnpublishedCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException;
    CollectionId createCollection(CreateCollectionCommand collectionCommand) throws CollectionsSaveException;
    void update(UpdateCollectionCommand updateCommand) throws CollectionsSaveException;
    void publish(Set<CollectionId> ids);
}
