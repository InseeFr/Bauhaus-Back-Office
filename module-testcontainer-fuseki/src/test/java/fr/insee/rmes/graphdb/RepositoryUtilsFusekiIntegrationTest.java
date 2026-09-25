package fr.insee.rmes.graphdb;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import java.util.stream.IntStream;
import org.eclipse.rdf4j.repository.Repository;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@link RepositoryUtils} contre Fuseki, construit et appelé comme l'application le fait : serveur et
 * dépôt tirés de la configuration, requêtes SPARQL passées telles quelles.
 *
 * <p>Tant qu'aucun initiator ne sait parler à Fuseki, {@code initRepository} rend un
 * {@code HTTPRepository}, qui parle le protocole REST RDF4J ({@code /repositories/<id>}) : Fuseki
 * n'en sert aucune route.
 */
@Tag("integration")
@Disabled("Rouge tant que FusekiRepositoryInitiator n'existe pas (F4) : HTTPRepository reçoit un 404 sur /protocol")
class RepositoryUtilsFusekiIntegrationTest extends WithFusekiContainer {

    private static final String CODES_GRAPH = "http://rdf.insee.fr/graphes/codes";

    private final RepositoryUtils repositoryUtils = new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED);

    private final Repository repository = repositoryUtils.initRepository(
            getRdfGestionConnectionDetails().getUrlServer(),
            getRdfGestionConnectionDetails().repositoryId());

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
}
