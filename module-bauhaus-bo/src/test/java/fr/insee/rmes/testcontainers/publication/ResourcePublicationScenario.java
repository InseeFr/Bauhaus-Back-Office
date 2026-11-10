package fr.insee.rmes.testcontainers.publication;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Set;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication générique d'une ressource par {@link PublicationUtils#publishResource}, celle des jeux
 * de données, de leurs entrées de catalogue et des distributions, rejouée sur chaque triplestore
 * (voir {@link ResourcePublicationGraphDBIntegrationTest} et {@link
 * ResourcePublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : la ressource publiée se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où elle arrive : sous Fuseki,
 * un triplet relu sans graphe nommé n'en porte aucun, et la publication partait dans le graphe par
 * défaut sans erreur.
 */
interface ResourcePublicationScenario {

    String CATALOGUE_GRAPH = "http://rdf.insee.fr/graphes/catalogue";

    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String DATASET = GESTION_BASE + "datasets/jd9301";
    String SERIES = GESTION_BASE + "operations/serie/s9301";
    String PUBLISHED_DATASET = PUBLICATION_BASE + "datasets/jd9301";
    String PUBLISHED_SERIES = PUBLICATION_BASE + "operations/serie/s9301";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheCatalogueGraph() throws RmesException {
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(CATALOGUE_GRAPH));
    }

    @Test
    default void publishes_the_resource_and_its_links_into_the_given_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(datasetTitled("Base permanente des équipements"));

        publish(gestion);

        assertThat(publishedTitles(gestion)).isEqualTo("Base permanente des équipements");
        assertThat(gestion.getResponseAsBoolean(
                        "ASK { GRAPH <%s> { <%s> <http://www.w3.org/ns/prov#wasGeneratedBy> <%s> } }"
                                .formatted(CATALOGUE_GRAPH, PUBLISHED_DATASET, PUBLISHED_SERIES)))
                .isTrue();
    }

    @Test
    default void leaves_the_denied_predicates_out_of_the_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(datasetTitled("Base permanente des équipements"));

        publish(gestion);

        assertThat(gestion.getResponseAsBoolean(
                        "ASK { GRAPH ?graph { <%s> <http://rdf.insee.fr/def/base#validationState> ?state } }"
                                .formatted(PUBLISHED_DATASET)))
                .isFalse();
    }

    @Test
    default void republishing_the_resource_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(datasetTitled("Base permanente des équipements"));
        publish(gestion);

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://purl.org/dc/terms/title> ?title } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://purl.org/dc/terms/title> "BPE 2026"@fr } }
                """.formatted(CATALOGUE_GRAPH, DATASET));
        publish(gestion);

        assertThat(publishedTitles(gestion)).isEqualTo("BPE 2026");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private void publish(RepositoryGestion gestion) throws RmesException {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, gestion, publication)
                .publishResource(Values.iri(DATASET), Set.of("validationState"), Values.iri(CATALOGUE_GRAPH));
    }

    private static String datasetTitled(String title) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://www.w3.org/ns/dcat#Dataset> ;
                    <http://purl.org/dc/terms/title> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://www.w3.org/ns/prov#wasGeneratedBy> <%s> .
                } }
                """.formatted(CATALOGUE_GRAPH, DATASET, title, SERIES);
    }

    /** Les titres du jeu de données publié lus dans le graphe du catalogue, séparés par « | ». */
    private static String publishedTitles(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?title; separator="|") AS ?titles) WHERE {
                          GRAPH <%s> { <%s> <http://purl.org/dc/terms/title> ?title }
                        }
                        """.formatted(CATALOGUE_GRAPH, PUBLISHED_DATASET))
                .optString("titles");
    }
}
