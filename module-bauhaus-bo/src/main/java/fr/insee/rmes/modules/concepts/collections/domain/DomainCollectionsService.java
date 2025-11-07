package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;

import java.util.List;
import java.util.Optional;

public class DomainCollectionsService implements CollectionsService {

    private final CollectionsRepository repository;

    public DomainCollectionsService(CollectionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PartialCollection> getAllCollections() throws CollectionsFetchException {
        return this.repository.getCollections();
    }

    @Override
    public Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException {
        return this.repository.getCollection(id);
    }

    @Override
    public CollectionId createCollection(CreateCollectionCommand collectionCommand) {
        return this.repository.save(collectionCommand);
    }
}
