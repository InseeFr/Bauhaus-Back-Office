package fr.insee.rmes.modules.concepts.collections.domain;

import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.Lang;
import fr.insee.rmes.modules.concepts.collections.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.concepts.collections.domain.model.PartialCollection;
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
import static org.mockito.Mockito.when;

class DomainCollectionsServiceTest {

    static final String ID = "1";
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
            new PartialCollection("2", new LocalisedLabel("en", Lang.EN))
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

}