package fr.insee.rmes.bauhaus_services.concepts.collections;

import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionsUtilsTest {

    @Mock
    private ConceptsPublication conceptsPublication;

    @Mock
    private RepositoryGestion repositoryGestion;

    private CollectionsUtils collectionsUtils;

    @BeforeAll
    static void initConfig() {
        GenericQueries.setConfig(new ConfigStub());
        RdfUtils.setConfig(new ConfigStub());
        RdfUtils.setUriUtils(new UriUtils("http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/collection")));
    }

    @BeforeEach
    void setUp() {
        collectionsUtils = new CollectionsUtils(conceptsPublication, repositoryGestion);
    }

    @Test
    void shouldValidateCollectionsFromString() throws RmesException {
        // Given
        String body = "[\"collection1\", \"collection2\", \"collection3\"]";

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> collectionsUtils.collectionsValidation(body));

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
        collectionsUtils.collectionsValidation(collectionsToValidate);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(collectionsToValidate);
        verify(repositoryGestion, times(1)).objectsValidation(anyList(), any());
    }

    @Test
    void shouldValidateSingleCollection() throws RmesException {
        // Given
        String body = "[\"collection1\"]";

        // When
        collectionsUtils.collectionsValidation(body);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(any(JSONArray.class));
        verify(repositoryGestion, times(1)).objectsValidation(argThat(list -> list.size() == 1), any());
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
        collectionsUtils.collectionsValidation(collectionsToValidate);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(collectionsToValidate);
        verify(repositoryGestion, times(1)).objectsValidation(argThat(list -> list.size() == 4), any());
    }

    @Test
    void shouldHandleCollectionIdsWithSpaces() throws RmesException {
        // Given - Collection IDs with spaces should be handled (spaces removed and lowercased)
        String body = "[\"Collection 1\", \"Collection 2\"]";

        // When
        collectionsUtils.collectionsValidation(body);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(any(JSONArray.class));
        verify(repositoryGestion, times(1)).objectsValidation(anyList(), any());
    }

    @Test
    void shouldValidateEmptyArrayOfCollections() throws RmesException {
        // Given
        JSONArray emptyArray = new JSONArray();

        // When
        collectionsUtils.collectionsValidation(emptyArray);

        // Then
        verify(conceptsPublication, times(1)).publishCollection(emptyArray);
        verify(repositoryGestion, times(1)).objectsValidation(argThat(list -> list.isEmpty()), any());
    }
}
