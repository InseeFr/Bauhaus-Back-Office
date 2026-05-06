package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionDashboardItem;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionToValidate;
import fr.insee.rmes.modules.concepts.collections.domain.model.CompactCollection;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

class DomainCollectionsServiceTest {

    static final UUID uuid1 = UUID.fromString("bb2328d8-230e-4a23-9cb5-a5c483b79378");
    static final UUID uuid2 = UUID.fromString("6c55b4db-e7f3-40ac-bb71-5f3864bef088");
    static final CollectionId ID = new CollectionId(uuid1.toString());

    static final Collection COLLECTION = new Collection(
            ID,
            List.of(LocalisedLabel.ofDefaultLanguage("fr"), LocalisedLabel.ofAlternativeLanguage("en")),
            null,
            "Contributor",
            Collections.emptyList(),
            LocalDateTime.of(1982, 7, 19, 0, 0),
            null,
            false,
            null);

    static final CompactCollection[] COMPACT_COLLECTIONS = {
            new CompactCollection(ID, LocalisedLabel.ofDefaultLanguage("fr")),
            new CompactCollection(new CollectionId(uuid2.toString()), LocalisedLabel.ofAlternativeLanguage("en"))
    };

    CollectionsRepository collectionsRepository;

    DomainCollectionsService domainCollectionsService ;

    @BeforeEach
    void resetDomainCollectionService(){
        collectionsRepository = Mockito.mock(CollectionsRepository.class);
        domainCollectionsService = new DomainCollectionsService(collectionsRepository);
    }

    @Test
    void all_collections_should_be_returned() throws CollectionsFetchException {
        // Given
        when(collectionsRepository.getCollections()).thenReturn(Arrays.asList(COMPACT_COLLECTIONS));
        //When
        List<CompactCollection> allCollections = domainCollectionsService.getAllCollections();
        //Then
        assertThat(allCollections).containsExactlyInAnyOrder(COMPACT_COLLECTIONS);
    }

    @Test
    void found_collection_should_be_returned() throws CollectionsFetchException {
        //Given
        when(collectionsRepository.getCollection(ID)).thenReturn(Optional.of(COLLECTION));
        //When
        Optional<Collection> actualCollection = domainCollectionsService.getCollection(ID);
        //Then
        assertThat(actualCollection).contains(COLLECTION);
    }

    @Test
    void collection_id_should_be_returned_when_collection_is_created() throws InvalidCreateCollectionCommandException, CollectionsSaveException, CollectionsFetchException {
        when(collectionsRepository.getCollection(ID)).thenReturn(Optional.empty());
        CreateCollectionCommand command = new CreateCollectionCommand(
                ID.value(),
                List.of(LocalisedLabel.ofDefaultLanguage("value")),
                null,
                "HIE0010",
                null,
                Collections.emptyList()
        );
        //When
        CollectionId collectionId = domainCollectionsService.createCollection(command);
        verify(collectionsRepository, times(1)).save(any());

        //Then
        assertThat(collectionId).isEqualTo(ID);
    }

    @Test
    void should_throw_when_collection_id_already_exists() throws CollectionsFetchException, InvalidCreateCollectionCommandException, CollectionsSaveException {
        when(collectionsRepository.getCollection(ID)).thenReturn(Optional.of(COLLECTION));
        CreateCollectionCommand command = new CreateCollectionCommand(
                ID.value(),
                List.of(LocalisedLabel.ofDefaultLanguage("value")),
                null,
                "HIE0010",
                null,
                Collections.emptyList()
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionAlreadyExistsException.class,
                () -> domainCollectionsService.createCollection(command));

        verify(collectionsRepository, never()).save(any());
    }

    @Test
    void dashboard_items_should_be_returned() throws CollectionsFetchException {
        // Given
        var dashboardItem = new CollectionDashboardItem(ID, "Label", "2024-01-01T10:00:00", null, false, "creator1", 2);
        when(collectionsRepository.getDashboard()).thenReturn(List.of(dashboardItem));
        // When
        var result = domainCollectionsService.getDashboard();
        // Then
        assertThat(result).containsExactly(dashboardItem);
    }

    @Test
    void collections_to_validate_should_be_returned() throws CollectionsFetchException {
        // Given
        var toValidate = new CollectionToValidate(ID, "Label", "creator1");
        when(collectionsRepository.getToValidate()).thenReturn(List.of(toValidate));
        // When
        var result = domainCollectionsService.getToValidate();
        // Then
        assertThat(result).containsExactly(toValidate);
    }

    @Test
    void collection_members_should_be_returned() throws CollectionsFetchException {
        // Given
        var member = new CollectionMember("c00001", "Label FR", "Label EN");
        when(collectionsRepository.getCollectionMembers(ID)).thenReturn(List.of(member));
        // When
        var result = domainCollectionsService.getCollectionMembers(ID);
        // Then
        assertThat(result).containsExactly(member);
    }

    @Test
    void sync_should_link_new_collections() throws CollectionsSaveException, CollectionsFetchException {
        when(collectionsRepository.getCollectionIdsByConceptId("c00001")).thenReturn(Collections.emptyList());

        domainCollectionsService.syncConceptCollections("c00001", List.of(uuid1.toString()));

        verify(collectionsRepository, times(1)).linkConceptToCollection(ID, "c00001");
        verify(collectionsRepository, never()).unlinkConceptFromCollection(any(), any());
    }

    @Test
    void sync_should_unlink_removed_collections() throws CollectionsSaveException, CollectionsFetchException {
        when(collectionsRepository.getCollectionIdsByConceptId("c00001")).thenReturn(List.of(uuid1.toString()));

        domainCollectionsService.syncConceptCollections("c00001", Collections.emptyList());

        verify(collectionsRepository, never()).linkConceptToCollection(any(), any());
        verify(collectionsRepository, times(1)).unlinkConceptFromCollection(ID, "c00001");
    }

    @Test
    void sync_should_link_and_unlink_when_collections_change() throws CollectionsSaveException, CollectionsFetchException {
        CollectionId ID2 = new CollectionId(uuid2.toString());
        when(collectionsRepository.getCollectionIdsByConceptId("c00001")).thenReturn(List.of(uuid1.toString()));

        domainCollectionsService.syncConceptCollections("c00001", List.of(uuid2.toString()));

        verify(collectionsRepository, times(1)).linkConceptToCollection(ID2, "c00001");
        verify(collectionsRepository, times(1)).unlinkConceptFromCollection(ID, "c00001");
    }

    @Test
    void sync_should_do_nothing_when_collections_unchanged() throws CollectionsSaveException, CollectionsFetchException {
        when(collectionsRepository.getCollectionIdsByConceptId("c00001")).thenReturn(List.of(uuid1.toString()));

        domainCollectionsService.syncConceptCollections("c00001", List.of(uuid1.toString()));

        verify(collectionsRepository, never()).linkConceptToCollection(any(), any());
        verify(collectionsRepository, never()).unlinkConceptFromCollection(any(), any());
    }

    @Test
    void sync_should_do_nothing_when_new_collections_is_empty_and_current_is_empty() throws CollectionsSaveException, CollectionsFetchException {
        when(collectionsRepository.getCollectionIdsByConceptId("c00001")).thenReturn(Collections.emptyList());

        domainCollectionsService.syncConceptCollections("c00001", Collections.emptyList());

        verify(collectionsRepository, never()).linkConceptToCollection(any(), any());
        verify(collectionsRepository, never()).unlinkConceptFromCollection(any(), any());
    }

    @Test
    void validate_should_succeed_when_all_collections_exist() throws CollectionsFetchException {
        when(collectionsRepository.findExistingCollectionIds(List.of(uuid1.toString())))
                .thenReturn(Set.of(uuid1.toString()));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                domainCollectionsService.validateCollections(List.of(uuid1.toString())));
    }

    @Test
    void validate_should_throw_when_collection_does_not_exist() throws CollectionsFetchException, CollectionsSaveException {
        when(collectionsRepository.findExistingCollectionIds(List.of("unknown-collection")))
                .thenReturn(Set.of());

        org.junit.jupiter.api.Assertions.assertThrows(CollectionsFetchException.class, () ->
                domainCollectionsService.validateCollections(List.of("unknown-collection")));

        verify(collectionsRepository, never()).linkConceptToCollection(any(), any());
        verify(collectionsRepository, never()).unlinkConceptFromCollection(any(), any());
    }

    @Test
    void validate_should_do_nothing_when_list_is_empty() throws CollectionsFetchException {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                domainCollectionsService.validateCollections(Collections.emptyList()));

        verify(collectionsRepository, never()).findExistingCollectionIds(any());
    }

}