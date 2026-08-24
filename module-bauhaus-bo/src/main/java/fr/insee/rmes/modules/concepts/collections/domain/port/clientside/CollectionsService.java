package fr.insee.rmes.modules.concepts.collections.domain.port.clientside;

import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionAlreadyPublishedException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionExport;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionExportType;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.UpdateCollectionCommand;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ClientSidePort
public interface CollectionsService {
    List<CompactCollection> getAllCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException;
    CollectionId createCollection(CreateCollectionCommand collectionCommand) throws CollectionsSaveException, CollectionsFetchException;
    void update(UpdateCollectionCommand updateCommand) throws CollectionsSaveException;
    List<CollectionDashboardItem> getDashboard() throws CollectionsFetchException;
    List<CollectionToValidate> getToValidate() throws CollectionsFetchException;
    List<CollectionMember> getCollectionMembers(CollectionId id) throws CollectionsFetchException;
    void validateCollections(List<String> collectionIds) throws CollectionsFetchException;
    void syncConceptCollections(String conceptId, List<String> newCollectionIds) throws CollectionsSaveException, CollectionsFetchException;
    void publishCollections(List<CollectionId> collectionIds) throws CollectionsSaveException, CollectionsFetchException, CollectionAlreadyPublishedException;
    CollectionExport exportCollection(CollectionId id) throws CollectionsFetchException;
    CollectionExport exportCollectionByType(CollectionId id, CollectionExportType type, Language language, boolean withConcepts) throws CollectionsFetchException;
    CollectionExport exportCollectionsZip(List<CollectionId> ids, CollectionExportType type, Language language, boolean withConcepts) throws CollectionsFetchException;
}
