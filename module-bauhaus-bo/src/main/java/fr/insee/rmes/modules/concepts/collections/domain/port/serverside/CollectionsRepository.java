package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ServerSidePort
public interface CollectionsRepository {
    List<CompactCollection> getCollections() throws CollectionsFetchException;
    Optional<Collection> getCollection(CollectionId id) throws CollectionsFetchException;
    void save(Collection collection) throws CollectionsSaveException;
    void update(Collection collection) throws CollectionsSaveException;
    List<CollectionDashboardItem> getDashboard() throws CollectionsFetchException;
    List<CollectionToValidate> getToValidate() throws CollectionsFetchException;
    List<CollectionMember> getCollectionMembers(CollectionId id) throws CollectionsFetchException;
    List<String> getCollectionIdsByConceptId(String conceptId) throws CollectionsFetchException;
    Set<String> findExistingCollectionIds(List<String> ids) throws CollectionsFetchException;
    void linkConceptToCollection(CollectionId collectionId, String conceptId) throws CollectionsSaveException;
    void unlinkConceptFromCollection(CollectionId collectionId, String conceptId) throws CollectionsSaveException;
    void publishCollections(List<CollectionId> collectionIds) throws CollectionsSaveException;
    Set<String> findValidatedCollectionIds(List<String> ids) throws CollectionsFetchException;
    CollectionExport exportCollection(CollectionId id) throws CollectionsFetchException;
    CollectionExport exportCollectionByType(CollectionId id, CollectionExportType type, Language language, boolean withConcepts) throws CollectionsFetchException;
    CollectionExport exportCollectionsZip(List<CollectionId> ids, CollectionExportType type, Language language, boolean withConcepts) throws CollectionsFetchException;
}
