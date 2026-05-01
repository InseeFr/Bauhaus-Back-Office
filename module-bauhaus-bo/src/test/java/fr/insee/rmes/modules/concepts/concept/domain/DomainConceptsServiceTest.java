package fr.insee.rmes.modules.concepts.concept.domain;

import fr.insee.rmes.modules.concepts.concept.domain.exceptions.ConceptFetchException;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class DomainConceptsServiceTest {

    ConceptsRepository conceptsRepository;
    DomainConceptsService domainConceptsService;

    @BeforeEach
    void setUp() {
        conceptsRepository = Mockito.mock(ConceptsRepository.class);
        domainConceptsService = new DomainConceptsService(conceptsRepository);
    }

    @Test
    void should_return_collection_ids_for_concept() throws ConceptFetchException {
        when(conceptsRepository.getCollectionIdsByConceptId("c00001"))
                .thenReturn(List.of("collection-id-1", "collection-id-2"));

        var result = domainConceptsService.getCollectionIdsByConceptId("c00001");

        assertThat(result).containsExactlyInAnyOrder("collection-id-1", "collection-id-2");
    }

    @Test
    void should_return_empty_list_when_concept_belongs_to_no_collection() throws ConceptFetchException {
        when(conceptsRepository.getCollectionIdsByConceptId("c00001"))
                .thenReturn(List.of());

        var result = domainConceptsService.getCollectionIdsByConceptId("c00001");

        assertThat(result).isEmpty();
    }
}
