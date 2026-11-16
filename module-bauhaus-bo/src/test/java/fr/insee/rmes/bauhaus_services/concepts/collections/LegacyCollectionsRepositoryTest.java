package fr.insee.rmes.bauhaus_services.concepts.collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.config.BauhausIriFactoryStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.modules.shared_kernel.infrastructure.publication.ObjectPublished;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class LegacyCollectionsRepositoryTest {

    @Mock
    private ConceptsPublication conceptsPublication;

    @Mock
    private ApplicationEventPublisher events;

    private GraphDBCollectionProperties collectionProperties;

    private LegacyCollectionsRepository legacyCollectionsRepository;

    private final BauhausIriFactory iriFactory = BauhausIriFactoryStub.stub("/collection");

    @BeforeEach
    void setUp() {
        collectionProperties = new GraphDBCollectionProperties(
                "http://rdf.insee.fr/graphes/concepts/definitions", "http://bauhaus//concepts/definitions");
        legacyCollectionsRepository =
                new LegacyCollectionsRepository(conceptsPublication, collectionProperties, events, iriFactory);
    }

    @Test
    void publishes_each_collection_then_announces_its_publication_with_the_concepts_graph() throws RmesException {
        legacyCollectionsRepository.collectionsValidation(
                new JSONArray().put("col1").put("col2"));

        InOrder inOrder = inOrder(conceptsPublication, events);
        for (String id : new String[] {"col1", "col2"}) {
            inOrder.verify(conceptsPublication).publishCollection(onlyId(id), eq(iriFactory.conceptGraph()));
            inOrder.verify(events).publishEvent(published(id));
        }
    }

    @Test
    void does_not_announce_a_collection_whose_publication_failed() throws RmesException {
        doNothing()
                .doThrow(new RmesException(500, "publication down", ""))
                .when(conceptsPublication)
                .publishCollection(any(JSONArray.class), any());

        assertThatThrownBy(() -> legacyCollectionsRepository.collectionsValidation(
                        new JSONArray().put("col1").put("col2")))
                .isInstanceOf(RmesException.class);

        verify(events).publishEvent(published("col1"));
        verify(events, never()).publishEvent(published("col2"));
    }

    @Test
    void neither_publishes_nor_announces_an_empty_batch() throws RmesException {
        legacyCollectionsRepository.collectionsValidation(new JSONArray());

        verifyNoInteractions(conceptsPublication, events);
    }

    private ObjectPublished published(String id) {
        return new ObjectPublished(collectionProperties.getResourceIRI(id), iriFactory.conceptGraph());
    }

    private static JSONArray onlyId(String id) {
        return argThat(ids -> ids.length() == 1 && id.equals(ids.getString(0)));
    }
}
