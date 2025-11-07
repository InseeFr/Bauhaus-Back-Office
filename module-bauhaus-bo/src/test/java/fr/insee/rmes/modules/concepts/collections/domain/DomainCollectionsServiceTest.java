package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCreateCollectionCommandException;
import fr.insee.rmes.modules.concepts.collections.domain.model.*;
import fr.insee.rmes.modules.concepts.collections.domain.model.commands.CreateCollectionCommand;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DomainCollectionsServiceTest {

    static final CollectionId ID = new CollectionId("1");
    static final PartialCollection PARTIAL_COLLECTION = new PartialCollection(ID, new LocalisedLabel("fr", Lang.FR));
    static final Collection COLLECTION = new Collection(
            PARTIAL_COLLECTION,
            new LocalisedLabel("en", Lang.EN),
            null,
            "Contributor",
            Collections.emptyMap(),
            LocalDateTime.of(1982, 7, 19, 0, 0),
            null,
            false
    );

    static final PartialCollection[] PARTIAL_COLLECTIONS = {
           PARTIAL_COLLECTION,
            new PartialCollection(new CollectionId("2"), new LocalisedLabel("en", Lang.EN))
    };

    CollectionsRepository collectionsRepository;

    DomainCollectionsService domainCollectionsService ;

    @BeforeEach
    void resetDomainCollectionService(){
        collectionsRepository = Mockito.mock(CollectionsRepository.class);
        domainCollectionsService = new DomainCollectionsService(collectionsRepository);
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
    void collectionIdShouldBeReturnedWhenCollectionisCreated() throws CollectionsFetchException, InvalidCreateCollectionCommandException {
        CreateCollectionCommand command = new CreateCollectionCommand(
                new LocalisedLabel("label", Lang.defaultLanguage()),
                null,
                Collections.emptyMap(),
                "HIE0010",
                null,
                Collections.emptyList()
        );
        //Given
        when(collectionsRepository.save(command)).thenReturn(ID);
        //When
        CollectionId collectionId = domainCollectionsService.createCollection(command);
        verify(collectionsRepository, times(1)).save(command);

        //Then
        assertThat(collectionId).isEqualTo(ID);
    }

}