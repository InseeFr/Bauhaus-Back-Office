package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.testcontainers.FusekiContainer;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import java.util.stream.IntStream;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@link RepositoryUtils} contre Fuseki, construit et appelé comme l'application le fait : serveur et
 * dépôt tirés de la configuration, requêtes SPARQL passées telles quelles.
 */
@Tag("integration")
class RepositoryUtilsFusekiIntegrationTest extends WithFusekiContainer {

    private static final String CODES_GRAPH = "http://rdf.insee.fr/graphes/codes";

    private final RepositoryUtils repositoryUtils =
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED, "fuseki");

    private final Repository repository = repositoryUtils.initRepository(
            getRdfGestionConnectionDetails().getUrlServer(),
            getRdfGestionConnectionDetails().repositoryId());

    @BeforeEach
    void authenticateTheWrites() {
        // L'image protège /update par mot de passe, et l'application ne sait pas encore en envoyer un à
        // Fuseki (F5) : le test pose les identifiants lui-même. À retirer en F5.
        ((SPARQLRepository) repository)
                .setUsernameAndPassword(FusekiContainer.ADMIN_USER, FusekiContainer.ADMIN_PASSWORD);
    }

    @BeforeAll
    static void initData() {
        fixtureLoader().withTrigFiles("repository-utils-fuseki-it.trig");
    }

    @Test
    void reads_the_labels_of_a_named_graph() throws RmesException {
        JSONArray labels = repositoryUtils.getResponseAsArray("""
                SELECT ?label WHERE {
                  GRAPH <%s> { <http://codelist/CL_FUSEKI_BILINGUE> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                }
                """.formatted(CODES_GRAPH), repository);

        assertThat(IntStream.range(0, labels.length())
                        .mapToObj(i -> labels.getJSONObject(i).getString("label")))
                .containsExactlyInAnyOrder("Liste de codes bilingue (Fuseki)", "Bilingual code list (Fuseki)");
    }

    @Test
    void writes_into_a_named_graph() throws RmesException {
        repositoryUtils.executeUpdate(
                "INSERT DATA { GRAPH <%s> { <http://codelist/CL_FUSEKI_ECRITE> a <http://www.w3.org/2004/02/skos/core#ConceptScheme> } }"
                        .formatted(CODES_GRAPH),
                repository);

        assertThat(repositoryUtils.getResponseForAskQuery(
                        "ASK { GRAPH <%s> { <http://codelist/CL_FUSEKI_ECRITE> ?p ?o } }".formatted(CODES_GRAPH),
                        repository))
                .isTrue();
    }

    @Test
    void clears_a_graph_that_was_never_written() throws RmesException {
        // Première publication d'un graphe : RepositoryPublication et RepositoryGestion le vident avant
        // d'y écrire. Fuseki refuse un CLEAR GRAPH sur un graphe absent, sauf en CLEAR SILENT.
        try (RepositoryConnection connection = repositoryUtils.getConnection(repository)) {
            assertThatCode(() -> connection.clear(Values.iri("http://rdf.insee.fr/graphes/jamais-ecrit")))
                    .doesNotThrowAnyException();
        }
    }
}
