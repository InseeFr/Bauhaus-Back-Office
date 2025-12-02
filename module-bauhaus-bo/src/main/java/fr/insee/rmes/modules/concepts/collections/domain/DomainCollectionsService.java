package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;

import java.util.List;
import java.util.Optional;

public class DomainCollectionsService implements CollectionsService {

    private final CollectionsRepository repository;
    private final RandomIdGenerator randomIdGenerator;

    public DomainCollectionsService(CollectionsRepository repository, RandomIdGenerator randomIdGenerator) {
        this.repository = repository;
        this.randomIdGenerator = randomIdGenerator;
    }

    @Override
    public List<CompactCollection> getAllCollections() throws CollectionsFetchException {
        return this.repository.getCollections();
    }

    @Override
    public Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException {
        return this.repository.getCollection(id);
    }

    @Override
    public CollectionId createCollection(CreateCollectionCommand collectionCommand) throws CollectionsSaveException {
        // TODO ajouter verication si les conceptsIdentifiers existent bien

        Collection newCollection = Collection.create(collectionCommand, randomIdGenerator.generateCollectionId());
        this.repository.save(newCollection);
        return newCollection.id();
    }
}
