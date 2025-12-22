package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.documents;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class OperationDocumentsQueriesIntegrationTest extends WithGraphDBContainer {

    RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED)
    );

    @BeforeAll
    static void initData() {
        OperationDocumentsQueries.setConfig(new ConfigStub());
        container.withTrigFiles("documents-url-bug.trig");
    }

    @Test
    @DisplayName("Comportement attendu apres correction: getDocumentUriQuery devrait retourner uniquement le document avec l'URL exacte")
    void should_return_only_exact_match_expected_behavior() throws Exception {
        // Ce test montre le comportement ATTENDU apres la correction du bug
        // Pour l'instant, ce test ECHOUE car le bug existe

        // GIVEN: On recherche l'URL exacte "http://example.org/document.pdf"
        String urlToSearch = "http://example.org/document.pdf";

        // WHEN: On execute la requete
        String query = OperationDocumentsQueries.getDocumentUriQuery(urlToSearch);
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
        String query = OperationDocumentsQueries.getDocumentUriQuery(urlToSearch);
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
        String query = OperationDocumentsQueries.getDocumentUriQuery(urlToSearch);
        JSONArray result = repositoryGestion.getResponseAsArray(query);

        // THEN: On devrait avoir exactement un document
        assertThat(result.length())
                .as("Devrait retourner exactement 1 document")
                .isEqualTo(1);

        assertThat(result.getJSONObject(0).getString("document"))
                .as("Le document retourne devrait etre le document 3")
                .contains("document/3");
    }
}