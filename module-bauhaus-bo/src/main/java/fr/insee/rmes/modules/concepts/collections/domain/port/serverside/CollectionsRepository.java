package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionPublicationException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ServerSidePort
public interface CollectionsRepository {
    List<CompactCollection> getCollections(CollectionPublicationStatusFilter filter) throws CollectionsFetchException;
    Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException;
    void save(Collection collection) throws CollectionsSaveException;
    void update(Collection collection) throws CollectionsSaveException;
    Set<Collection> getCollections(Set<CollectionId> ids) throws CollectionsFetchException;

    void updateValidationState(Set<CollectionId> ids) throws CollectionPublicationException;
}
