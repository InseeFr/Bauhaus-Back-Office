package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;

import java.util.List;
import java.util.Optional;

public class DomainCollectionsService implements CollectionService {

    private final CollectionsRepository repository;

    public DomainCollectionsService(CollectionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PartialCollection> getAllCollections() throws CollectionsFetchException {
        return this.repository.getCollections();
    }

    @Override
    public Optional<Collection> getCollection(String id) throws CollectionsFetchException {
        return this.repository.getCollection(id);
    }
}
