package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.documents;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class OperationDocumentsQueriesIntegrationTest extends WithGraphDBContainer {

    RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED)
    );

    private OperationDocumentsQueries operationDocumentsQueries;

    @BeforeAll
    static void initData() {
        container.withTrigFiles("documents-url-bug.trig");
    }

    @BeforeEach
    void setUp() {
        operationDocumentsQueries = new OperationDocumentsQueries(BauhausUriPropertiesStub.stub(), new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    }

    @Test
    @DisplayName("Comportement attendu apres correction: getDocumentUriQuery devrait retourner uniquement le document avec l'URL exacte")
    void should_return_only_exact_match_expected_behavior() throws Exception {
        // Ce test montre le comportement ATTENDU apres la correction du bug
        // Pour l'instant, ce test ECHOUE car le bug existe

        // GIVEN: On recherche l'URL exacte "http://example.org/document.pdf"
        String urlToSearch = "http://example.org/document.pdf";

        // WHEN: On execute la requete
        String query = operationDocumentsQueries.getDocumentUriQuery(urlToSearch);
        JSONArray result = repositoryGestion.getResponseAsArray(query);

        // THEN: On devrait avoir UN SEUL document
        // Ce test est desactive (commentaire) car il echoue avec le bug actuel
        // Decommenter apres la correction du bug

        assertThat(result.length())
                .as("Devrait retourner exactement 1 document avec l'URL exacte")
                .isEqualTo(1);

        assertThat(result.getJSONObject(0).getString("document"))
                .as("Le document retourne devrait etre le document 1")
                .contains("document/1");


        // Pour l'instant, on verifie juste que la requete s'execute sans erreur
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getDocumentUriQuery ne retourne rien pour une URL inexistante")
    void should_return_empty_when_url_does_not_exist() throws Exception {
        // GIVEN: On recherche une URL qui n'existe pas
        String urlToSearch = "http://example.org/inexistant.pdf";

        // WHEN: On execute la requete
        String query = operationDocumentsQueries.getDocumentUriQuery(urlToSearch);
        JSONArray result = repositoryGestion.getResponseAsArray(query);

        // THEN: On ne devrait avoir aucun document
        assertThat(result.length())
                .as("Aucun document ne devrait etre retourne pour une URL inexistante")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("getDocumentUriQuery retourne le bon document pour une URL unique")
    void should_return_correct_document_for_unique_url() throws Exception {
        // GIVEN: On recherche une URL unique (autre-fichier.pdf)
        String urlToSearch = "http://example.org/autre-fichier.pdf";

        // WHEN: On execute la requete
        String query = operationDocumentsQueries.getDocumentUriQuery(urlToSearch);
        JSONArray result = repositoryGestion.getResponseAsArray(query);

        // THEN: On devrait avoir exactement un document
        assertThat(result.length())
                .as("Devrait retourner exactement 1 document")
                .isEqualTo(1);

        assertThat(result.getJSONObject(0).getString("document"))
                .as("Le document retourne devrait etre le document 3")
                .contains("document/3");
    }

    @Test
    @DisplayName("getDocumentPredicatesAndObjects retourne les triplets du document demande")
    void should_return_predicates_and_objects_of_the_requested_document() throws Exception {
        IRI document = SimpleValueFactory.getInstance().createIRI("http://bauhaus/documents/document/1");

        JSONArray result = repositoryGestion.getResponseAsArray(
                operationDocumentsQueries.getDocumentPredicatesAndObjects(document));

        assertThat(predicateObjectPairs(result))
                .as("tous les triplets du document 1, et eux seuls")
                .containsExactlyInAnyOrder(
                        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type=http://xmlns.com/foaf/0.1/Document",
                        "http://www.w3.org/2000/01/rdf-schema#label=Document de base",
                        "http://purl.org/dc/elements/1.1/language=fr",
                        "http://schema.org/url=http://example.org/document.pdf");
    }

    @Test
    @DisplayName("getDocumentPredicatesAndObjects ne retourne rien pour un document inexistant")
    void should_return_nothing_for_an_unknown_document() throws Exception {
        IRI document = SimpleValueFactory.getInstance().createIRI("http://bauhaus/documents/document/999");

        JSONArray result = repositoryGestion.getResponseAsArray(
                operationDocumentsQueries.getDocumentPredicatesAndObjects(document));

        assertThat(result.length()).isZero();
    }

    private static List<String> predicateObjectPairs(JSONArray tuples) {
        List<String> pairs = new ArrayList<>();
        JSONUtils.stream(tuples).forEach(tuple -> {
            pairs.add(tuple.getString("predicat") + "=" + tuple.getString("obj"));
        });
        return pairs;
    }
}