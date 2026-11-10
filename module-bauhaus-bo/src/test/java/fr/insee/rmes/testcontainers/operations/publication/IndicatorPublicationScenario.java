package fr.insee.rmes.testcontainers.operations.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.PropertiesKeys;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'un indicateur, rejouée sur chaque triplestore (voir {@link
 * IndicatorPublicationGraphDBIntegrationTest} et {@link IndicatorPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : l'indicateur publié se distingue par son IRI,
 * réécrite dans la base de publication. L'indicateur est réparti sur deux graphes : sa description
 * dans celui des produits, ses dates dans celui des opérations. Ce qui est vérifié est que chaque
 * triplet publié arrive dans le graphe d'où il vient : sous Fuseki, un triplet relu sans graphe nommé
 * n'en porte aucun, et la publication partait dans le graphe par défaut sans erreur.
 */
interface IndicatorPublicationScenario {

    /** Les graphes de {@link GraphsPropertiesStub}. */
    String PRODUCTS_GRAPH = "http://rdf.insee.fr/graphes/produits";

    String OPERATIONS_GRAPH = "http://rdf.insee.fr/graphes/operations";

    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String INDICATOR = GESTION_BASE + "produits/indicateur/p9301";
    String SERIES = GESTION_BASE + "operations/serie/s9301";
    String PUBLISHED_INDICATOR = PUBLICATION_BASE + "produits/indicateur/p9301";
    String PUBLISHED_SERIES = PUBLICATION_BASE + "operations/serie/s9301";
    String CREATED = "2026-01-15T10:00:00";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart de graphes vides. */
    @BeforeEach
    default void dropTheProductsAndOperationsGraphs() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        Map<String, String> baseUris = Map.of(PropertiesKeys.PRODUCTS_BASE_URI, "produits/indicateur");
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION_BASE, GESTION_BASE, name -> Optional.ofNullable(baseUris.get(name))));
        gestion()
                .executeUpdate(
                        "DROP SILENT GRAPH <%s> ; DROP SILENT GRAPH <%s>".formatted(PRODUCTS_GRAPH, OPERATIONS_GRAPH));
    }

    @Test
    default void publishes_the_indicator_and_its_links_into_the_products_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        insertTheIndicator(gestion, "Indice des prix");

        indicatorPublication(gestion).publish("p9301");

        assertThat(publishedLabels(gestion)).isEqualTo("Indice des prix");
        assertThat(gestion.getResponseAsBoolean(
                        "ASK { GRAPH <%s> { <%s> <http://www.w3.org/ns/prov#wasGeneratedBy> <%s> } }"
                                .formatted(PRODUCTS_GRAPH, PUBLISHED_INDICATOR, PUBLISHED_SERIES)))
                .isTrue();
        assertThat(gestion.getResponseAsBoolean(
                        "ASK { GRAPH ?graph { <%s> <http://rdf.insee.fr/def/base#validationState> ?state } }"
                                .formatted(PUBLISHED_INDICATOR)))
                .isFalse();
    }

    @Test
    default void publishes_the_dates_of_the_indicator_into_the_operations_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        insertTheIndicator(gestion, "Indice des prix");

        indicatorPublication(gestion).publish("p9301");

        assertThat(gestion.getResponseAsBoolean("""
                        ASK { GRAPH <%s> {
                          <%s> <http://purl.org/dc/terms/created> "%s"^^<http://www.w3.org/2001/XMLSchema#dateTime>
                        } }
                        """.formatted(OPERATIONS_GRAPH, PUBLISHED_INDICATOR, CREATED)))
                .isTrue();
    }

    @Test
    default void republishing_the_indicator_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        IndicatorPublication indicatorPublication = indicatorPublication(gestion);
        insertTheIndicator(gestion, "Indice des prix");
        indicatorPublication.publish("p9301");

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "Indice des prix à la consommation"@fr } }
                """.formatted(PRODUCTS_GRAPH, INDICATOR));
        indicatorPublication.publish("p9301");

        assertThat(publishedLabels(gestion)).isEqualTo("Indice des prix à la consommation");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private IndicatorPublication indicatorPublication(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        return new IndicatorPublication(
                mock(OperationsParentRepository.class),
                gestion,
                publication,
                new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null));
    }

    private static void insertTheIndicator(RepositoryGestion gestion, String label) throws RmesException {
        gestion.executeUpdate("""
                INSERT DATA {
                  GRAPH <%s> {
                    <%s> a <http://rdf.insee.fr/def/base#StatisticalIndicator> ;
                      <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                      <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                      <http://www.w3.org/ns/prov#wasGeneratedBy> <%s> .
                  }
                  GRAPH <%s> {
                    <%2$s> <http://purl.org/dc/terms/created> "%s"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
                  }
                }
                """.formatted(PRODUCTS_GRAPH, INDICATOR, label, SERIES, OPERATIONS_GRAPH, CREATED));
    }

    /** Les libellés de l'indicateur publié lus dans le graphe des produits, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                        }
                        """.formatted(PRODUCTS_GRAPH, PUBLISHED_INDICATOR))
                .optString("labels");
    }
}
