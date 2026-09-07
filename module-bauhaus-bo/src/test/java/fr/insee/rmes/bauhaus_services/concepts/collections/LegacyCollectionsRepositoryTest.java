package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb.GraphDBCollectionProperties;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegacyCollectionsRepositoryTest {

    @Mock
    private ConceptsPublication conceptsPublication;

    @Mock
    private RepositoryGestion repositoryGestion;

    private LegacyCollectionsRepository legacyCollectionsRepository;

    @BeforeAll
    static void initConfig() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder("http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/collection")));
    }

    @BeforeEach
    void setUp() {
        GraphDBCollectionProperties collectionProperties =
                new GraphDBCollectionProperties("http://rdf.insee.fr/graphes/concepts/definitions",
                        "http://bauhaus//concepts/definitions");
        legacyCollectionsRepository = new LegacyCollectionsRepository(conceptsPublication, repositoryGestion, collectionProperties);
    }

    @Test
    void shouldValidateCollectionsFromString() throws RmesException {
        // Given
        String body = "[\"collection1\", \"collection2\", \"collection3\"]";

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> legacyCollectionsRepository.collectionsValidation(body));

        // Verify that publication was called
        verify(conceptsPublication, times(1)).publishCollection(any(JSONArray.class));
        verify(repositoryGestion, times(1)).objectsValidation(anyList(), any());
    }

    @Test
    void shouldValidateCollectionsFromJSONArray() throws RmesException {
        // Given
        JSONArray collectionsToValidate = new JSONArray()
                .put("collection1")
                .put("collection2");

        // When
        legacyCollectionsRepository.collectionsValidation(collectionsToValidate);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(collectionsToValidate);
        verify(repositoryGestion, times(1)).objectsValidation(anyList(), any());
    }

    @Test
    void shouldValidateSingleCollection() throws RmesException {
        // Given
        String body = "[\"collection1\"]";

        // When
        legacyCollectionsRepository.collectionsValidation(body);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(any(JSONArray.class));
        verify(repositoryGestion, times(1)).objectsValidation(argThat(list -> list.size() == 1), any());
    }

    @Test
    void shouldWriteValidatedStateWhenValidating() throws RmesException {
        // Given
        String body = "[\"collection1\"]";

        // When
        legacyCollectionsRepository.collectionsValidation(body);

        // Then - validation writes validationState=Validated
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).objectsValidation(anyList(), modelCaptor.capture());
        assertEquals(ValidationStatus.VALIDATED.getValue(), validationStateOf(modelCaptor.getValue()));
    }

    private static String validationStateOf(Model model) {
        for (Statement st : model) {
            if (st.getPredicate().equals(INSEE.VALIDATION_STATE)) {
                return st.getObject().stringValue();
            }
        }
        return null;
    }

    @Test
    void shouldValidateMultipleCollections() throws RmesException {
        // Given
        JSONArray collectionsToValidate = new JSONArray()
                .put("col1")
                .put("col2")
                .put("col3")
                .put("col4");

        // When
        legacyCollectionsRepository.collectionsValidation(collectionsToValidate);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(collectionsToValidate);
        verify(repositoryGestion, times(1)).objectsValidation(argThat(list -> list.size() == 4), any());
    }

    @Test
    void shouldHandleCollectionIdsWithSpaces() throws RmesException {
        // Given - Collection IDs with spaces should be handled (spaces removed and lowercased)
        String body = "[\"Collection 1\", \"Collection 2\"]";

        // When
        legacyCollectionsRepository.collectionsValidation(body);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(any(JSONArray.class));
        verify(repositoryGestion, times(1)).objectsValidation(anyList(), any());
    }

    @Test
    void shouldValidateEmptyArrayOfCollections() throws RmesException {
        // Given
        JSONArray emptyArray = new JSONArray();

        // When
        legacyCollectionsRepository.collectionsValidation(emptyArray);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(emptyArray);
        verify(repositoryGestion, times(1)).objectsValidation(argThat(list -> list.isEmpty()), any());
    }
}
