package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionAlreadyExistsException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionAlreadyPublishedException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionNotFoundException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionExport;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionExportType;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DomainCollectionsService implements CollectionsService {

    private final CollectionsRepository repository;

    public DomainCollectionsService(CollectionsRepository repository) {
        this.repository = repository;
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
    public CollectionId createCollection(CreateCollectionCommand collectionCommand)
            throws CollectionsSaveException, CollectionsFetchException {
        // TODO ajouter verication si les conceptsIdentifiers existent bien

        CollectionId requestedId = new CollectionId(collectionCommand.id());
        if (this.repository.getCollection(requestedId).isPresent()) {
            throw new CollectionAlreadyExistsException(
                    "Collection with id %s already exists".formatted(requestedId.value()));
        }

        Collection newCollection = Collection.create(collectionCommand, requestedId);
        this.repository.save(newCollection);
        return newCollection.id();
    }

    @Override
    public void update(UpdateCollectionCommand updateCommand) throws CollectionsSaveException {
        ValidationStatus next = nextValidationStateForUpdate(updateCommand.collectionId());
        Collection collection = Collection.create(updateCommand, updateCommand.collectionId(), next);
        this.repository.update(collection);
    }

    private ValidationStatus nextValidationStateForUpdate(CollectionId id) throws CollectionsSaveException {
        try {
            ValidationStatus current = this.repository
                    .getCollection(id)
                    .map(Collection::validationState)
                    .orElse(ValidationStatus.UNPUBLISHED);
            return (current == ValidationStatus.VALIDATED || current == ValidationStatus.MODIFIED)
                    ? ValidationStatus.MODIFIED
                    : ValidationStatus.UNPUBLISHED;
        } catch (CollectionsFetchException e) {
            throw new CollectionsSaveException(new Exception(e));
        }
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
        List<String> missing =
                collectionIds.stream().filter(id -> !existing.contains(id)).toList();
        if (!missing.isEmpty()) {
            throw new CollectionsFetchException(
                    new CollectionNotFoundException("Collections not found: " + String.join(", ", missing)));
        }
    }

    @Override
    public CollectionExport exportCollection(CollectionId id) throws CollectionsFetchException {
        if (this.repository.getCollection(id).isEmpty()) {
            throw new CollectionsFetchException(
                    new CollectionNotFoundException("Collection %s not found".formatted(id.value())));
        }
        return this.repository.exportCollection(id);
    }

    @Override
    public CollectionExport exportCollectionByType(
            CollectionId id, CollectionExportType type, Language language, boolean withConcepts)
            throws CollectionsFetchException {
        if (this.repository.getCollection(id).isEmpty()) {
            throw new CollectionsFetchException(
                    new CollectionNotFoundException("Collection %s not found".formatted(id.value())));
        }
        return this.repository.exportCollectionByType(id, type, language, withConcepts);
    }

    @Override
    public CollectionExport exportCollectionsZip(
            List<CollectionId> ids, CollectionExportType type, Language language, boolean withConcepts)
            throws CollectionsFetchException {
        if (ids.isEmpty()) {
            throw new CollectionsFetchException(new CollectionNotFoundException("No collection ids provided"));
        }
        return this.repository.exportCollectionsZip(ids, type, language, withConcepts);
    }

    @Override
    public void publishCollections(List<CollectionId> collectionIds)
            throws CollectionsSaveException, CollectionsFetchException, CollectionAlreadyPublishedException {
        if (collectionIds.isEmpty()) return;
        List<String> ids = collectionIds.stream().map(CollectionId::value).toList();
        Set<String> existing = this.repository.findExistingCollectionIds(ids);
        List<String> missing = ids.stream().filter(id -> !existing.contains(id)).toList();
        if (!missing.isEmpty()) {
            throw new CollectionsFetchException(
                    new CollectionNotFoundException("Collections not found: " + String.join(", ", missing)));
        }
        Set<String> alreadyPublished = this.repository.findValidatedCollectionIds(ids);
        List<String> published = ids.stream().filter(alreadyPublished::contains).toList();
        if (!published.isEmpty()) {
            throw new CollectionAlreadyPublishedException(
                    "Collections already published: " + String.join(", ", published));
        }
        this.repository.publishCollections(collectionIds);
    }

    @Override
    public void syncConceptCollections(String conceptId, List<String> newCollectionIds)
            throws CollectionsSaveException, CollectionsFetchException {
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
