package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.PublishedCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsPublicationRepository;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionPublicationStatusFilter.ALL;
import static fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionPublicationStatusFilter.UNPUBLISHED;

public class DomainCollectionsService implements CollectionsService {

    private final CollectionsRepository gestionRepository;
    private final CollectionsPublicationRepository publicationRepository;
    private final RandomIdGenerator randomIdGenerator;

    public DomainCollectionsService(CollectionsRepository gestionRepository, CollectionsPublicationRepository publicationRepository, RandomIdGenerator randomIdGenerator) {
        this.gestionRepository = gestionRepository;
        this.publicationRepository = publicationRepository;
        this.randomIdGenerator = randomIdGenerator;
    }

    @Override
    public List<CompactCollection> getAllCollections() throws CollectionsFetchException {
        return this.gestionRepository.getCollections(ALL);
    }

    @Override
    public List<CompactCollection> getUnpublishedCollections() throws CollectionsFetchException {
        return this.gestionRepository.getCollections(UNPUBLISHED);
    }

    @Override
    public Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException {
        return this.gestionRepository.getCollection(id);
    }

    @Override
    public CollectionId createCollection(CreateCollectionCommand collectionCommand) throws CollectionsSaveException {
        // TODO ajouter verication si les conceptsIdentifiers existent bien

        Collection newCollection = Collection.create(collectionCommand, randomIdGenerator.generateCollectionId());
        this.gestionRepository.save(newCollection);
        return newCollection.id();
    }

    @Override
    public void update(UpdateCollectionCommand updateCommand) throws CollectionsSaveException {
        Collection collection = Collection.create(updateCommand, updateCommand.id());
        this.gestionRepository.update(collection);
    }

    @Override
    public void publish(Set<CollectionId> ids) {
        var collections = this.gestionRepository.getCollections(ids);
        this.publicationRepository.publish(collections.stream().map(PublishedCollection::fromCollection).collect(Collectors.toSet()));
        this.gestionRepository.save(null);
    }
}
