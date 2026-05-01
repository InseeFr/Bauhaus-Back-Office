package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionNotFoundException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Override
    public void update(UpdateCollectionCommand updateCommand) throws CollectionsSaveException {
        Collection collection = Collection.create(updateCommand, updateCommand.id());
        this.repository.update(collection);
    }

    @Override
    public List<CollectionDashboardItem> getDashboard() throws CollectionsFetchException {
        return this.repository.getDashboard();
    }

    @Override
    public List<CollectionToValidate> getToValidate() throws CollectionsFetchException {
        return this.repository.getToValidate();
    }

    @Override
    public List<CollectionMember> getCollectionMembers(CollectionId id) throws CollectionsFetchException {
        return this.repository.getCollectionMembers(id);
    }

    @Override
    public void validateCollections(List<String> collectionIds) throws CollectionsFetchException {
        if (collectionIds.isEmpty()) return;
        Set<String> existing = this.repository.findExistingCollectionIds(collectionIds);
        List<String> missing = collectionIds.stream()
                .filter(id -> !existing.contains(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new CollectionsFetchException(new CollectionNotFoundException("Collections not found: " + String.join(", ", missing)));
        }
    }

    @Override
    public void syncConceptCollections(String conceptId, List<String> newCollectionIds) throws CollectionsSaveException, CollectionsFetchException {
        List<String> currentIds = this.repository.getCollectionIdsByConceptId(conceptId);
        List<String> toAdd = new ArrayList<>(newCollectionIds);
        toAdd.removeAll(currentIds);
        List<String> toRemove = new ArrayList<>(currentIds);
        toRemove.removeAll(newCollectionIds);

        for (String collectionId : toAdd) {
            this.repository.linkConceptToCollection(new CollectionId(collectionId), conceptId);
        }
        for (String collectionId : toRemove) {
            this.repository.unlinkConceptFromCollection(new CollectionId(collectionId), conceptId);
        }
    }
}
