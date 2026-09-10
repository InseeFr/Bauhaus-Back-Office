package fr.insee.rmes.modules.concepts.concept.infrastructure.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.concepts.concepts.LegacyConceptsRepository;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphDBConceptsRepositoryTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    ConceptCollectionsQueries conceptCollectionsQueries;

    @Mock
    ConceptConceptsQueries conceptConceptsQueries;

    @Mock
    LegacyConceptsRepository legacyConceptsRepository;

    GraphDBConceptsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new GraphDBConceptsRepository(
                repositoryGestion,
                conceptCollectionsQueries,
                conceptConceptsQueries,
                new BauhausLanguagesProperties("fr", "en"),
                legacyConceptsRepository);
    }

    @Test
    void getConcepts_exposes_the_alternative_label_of_each_concept() throws Throwable {
        givenConceptRows("""
                [
                  {"id": "c00001", "label": "Répertoire des personnes physiques", "altLabel": "RNIPP"}
                ]
                """);

        var concepts = repository.getConcepts();

        assertThat(concepts)
                .singleElement()
                .satisfies(concept ->
                        assertThat(concept.alternativeLabel().value()).isEqualTo("RNIPP"));
    }

    @Test
    void getConcepts_concatenates_the_alternative_labels_of_a_concept_declaring_several_of_them() throws Throwable {
        givenConceptRows("""
                [
                  {"id": "c00001", "label": "Répertoire des personnes physiques", "altLabel": "RNIPP"},
                  {"id": "c00001", "label": "Répertoire des personnes physiques", "altLabel": "Répertoire national"}
                ]
                """);

        var concepts = repository.getConcepts();

        assertThat(concepts)
                .singleElement()
                .satisfies(concept ->
                        assertThat(concept.alternativeLabel().value()).isEqualTo("RNIPP || Répertoire national"));
    }

    @Test
    void getConcepts_leaves_the_alternative_label_null_for_a_concept_without_any() throws Throwable {
        givenConceptRows("""
                [
                  {"id": "c00001", "label": "Concept sans sigle"}
                ]
                """);

        var concepts = repository.getConcepts();

        assertThat(concepts)
                .singleElement()
                .satisfies(concept -> assertThat(concept.alternativeLabel()).isNull());
    }

    @Test
    void getConcepts_keeps_one_entry_per_concept_in_the_query_order() throws Throwable {
        givenConceptRows("""
                [
                  {"id": "c00002", "label": "Bravo", "altLabel": "B"},
                  {"id": "c00001", "label": "Alpha", "altLabel": "A1"},
                  {"id": "c00001", "label": "Alpha", "altLabel": "A2"}
                ]
                """);

        var concepts = repository.getConcepts();

        assertThat(concepts).extracting(concept -> concept.id().value()).containsExactly("c00002", "c00001");
    }

    @Test
    void getConcepts_returns_an_empty_list_when_the_query_returns_nothing() throws Throwable {
        givenConceptRows("[]");

        assertThat(repository.getConcepts()).isEmpty();
    }

    private void givenConceptRows(String rows) throws Throwable {
        when(conceptConceptsQueries.conceptsQuery()).thenReturn("query");
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(new JSONArray(rows));
    }
}
