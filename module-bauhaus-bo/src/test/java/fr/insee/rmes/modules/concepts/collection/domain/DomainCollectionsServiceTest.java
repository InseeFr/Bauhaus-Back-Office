package fr.insee.rmes.modules.concepts.collection.domain;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.CollectionsSaveException;
import fr.insee.rmes.modules.concepts.collection.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collection.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collection.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.collection.domain.model.PartialCollection;
import fr.insee.rmes.modules.concepts.collection.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collection.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.modules.concepts.collection.domain.port.serverside.RandomIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DomainCollectionsServiceTest {

    static final UUID uuid1 = UUID.fromString("bb2328d8-230e-4a23-9cb5-a5c483b79378");
    static final UUID uuid2 = UUID.fromString("6c55b4db-e7f3-40ac-bb71-5f3864bef088");
    static final CollectionId ID = new CollectionId(uuid1.toString());
    static final PartialCollection PARTIAL_COLLECTION = new PartialCollection(ID, LocalisedLabel.ofDefaultLanguage("fr"));
    static final Collection COLLECTION = new Collection(
            PARTIAL_COLLECTION,
            LocalisedLabel.ofAlternativeLanguage("en"),
            null,
            "Contributor",
            Collections.emptyMap(),
            LocalDateTime.of(1982, 7, 19, 0, 0),
            null,
            false,
            null);

    static final PartialCollection[] PARTIAL_COLLECTIONS = {
           PARTIAL_COLLECTION,
            new PartialCollection(new CollectionId(uuid2.toString()), LocalisedLabel.ofAlternativeLanguage("en"))
    };

    CollectionsRepository collectionsRepository;

    DomainCollectionsService domainCollectionsService ;

    RandomIdGenerator randomIdGenerator;

    @BeforeEach
    void resetDomainCollectionService(){
        collectionsRepository = Mockito.mock(CollectionsRepository.class);
        randomIdGenerator = Mockito.mock(RandomIdGenerator.class);
        domainCollectionsService = new DomainCollectionsService(collectionsRepository, randomIdGenerator);
    }

    @Test
    void allCollectionsShouldBeReturned() throws CollectionsFetchException {
        // Given
        when(collectionsRepository.getCollections()).thenReturn(Arrays.asList(PARTIAL_COLLECTIONS));
        //When
        List<PartialCollection> allCollections = domainCollectionsService.getAllCollections();
        //Then
        assertThat(allCollections).containsExactlyInAnyOrder(PARTIAL_COLLECTIONS);
    }

    @Test
    void foundCollectionShouldBeReturned() throws CollectionsFetchException {
        //Given
        when(collectionsRepository.getCollection(ID)).thenReturn(Optional.of(COLLECTION));
        //When
        Optional<Collection> actualCollection = domainCollectionsService.getCollection(ID);
        //Then
        assertThat(actualCollection).contains(COLLECTION);
    }

    @Test
    void collectionIdShouldBeReturnedWhenCollectionisCreated() throws InvalidCreateCollectionCommandException, CollectionsSaveException {
        when(randomIdGenerator.generateCollectionId()).thenReturn(ID);
        CreateCollectionCommand command = new CreateCollectionCommand(
                LocalisedLabel.ofDefaultLanguage("value"),
                null,
                Collections.emptyMap(),
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

}