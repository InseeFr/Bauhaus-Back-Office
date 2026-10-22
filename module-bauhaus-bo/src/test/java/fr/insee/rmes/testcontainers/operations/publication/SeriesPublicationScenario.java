package fr.insee.rmes.testcontainers.operations.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'une série, rejouée sur chaque triplestore (voir
 * {@link SeriesPublicationGraphDBIntegrationTest} et {@link SeriesPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : la série publiée se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrivent la série, le
 * lien {@code dcterms:hasPart} de sa famille, les liens {@code dcterms:replaces} /
 * {@code dcterms:isReplacedBy} des séries voisines et le lien vers son opération publiée : sous
 * Fuseki, un triplet relu sans graphe nommé n'en porte aucun, et la publication partait dans le
 * graphe par défaut sans erreur.
 *
 * <p>Les statuts de validation de la série et de sa famille, lus par requêtes SPARQL hors du
 * périmètre de ce test, sont simulés. Celui de l'opération est lu dans le graphe.
 */
interface SeriesPublicationScenario {

    String OPERATIONS_GRAPH = "http://rdf.insee.fr/graphes/operations";
    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String SERIES = GESTION_BASE + "operations/serie/s100";
    String FAMILY = GESTION_BASE + "operations/famille/f10";
    String PREVIOUS_SERIES = GESTION_BASE + "operations/serie/s99";
    String NEXT_SERIES = GESTION_BASE + "operations/serie/s101";
    String OPERATION = GESTION_BASE + "operations/operation/s1000";
    String PUBLISHED_SERIES = PUBLICATION_BASE + "operations/serie/s100";
    String PUBLISHED_FAMILY = PUBLICATION_BASE + "operations/famille/f10";
    String PUBLISHED_PREVIOUS_SERIES = PUBLICATION_BASE + "operations/serie/s99";
    String PUBLISHED_NEXT_SERIES = PUBLICATION_BASE + "operations/serie/s101";
    String PUBLISHED_OPERATION = PUBLICATION_BASE + "operations/operation/s1000";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheOperationsGraph() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION_BASE, GESTION_BASE, name -> Optional.of("operations/serie")));
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(OPERATIONS_GRAPH));
    }

    @Test
    default void publishes_the_series_and_its_links_into_the_operations_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(seriesLabelled("Enquête Emploi"));

        seriesPublication(gestion).publishSeries("s100", seriesJson(), operationsGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Enquête Emploi");
        assertThat(gestion.getResponseAsBoolean("""
                        ASK { GRAPH <%s> {
                          <%s> <http://purl.org/dc/terms/isPartOf> <%s> .
                          <%3$s> <http://purl.org/dc/terms/hasPart> <%2$s> .
                          <%4$s> <http://purl.org/dc/terms/isReplacedBy> <%2$s> .
                          <%5$s> <http://purl.org/dc/terms/replaces> <%2$s> .
                          <%2$s> <http://purl.org/dc/terms/hasPart> <%6$s> .
                        } }
                        """.formatted(
                                OPERATIONS_GRAPH,
                                PUBLISHED_SERIES,
                                PUBLISHED_FAMILY,
                                PUBLISHED_PREVIOUS_SERIES,
                                PUBLISHED_NEXT_SERIES,
                                PUBLISHED_OPERATION)))
                .isTrue();
    }

    @Test
    default void republishing_the_series_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        SeriesPublication seriesPublication = seriesPublication(gestion);
        gestion.executeUpdate(seriesLabelled("Enquête Emploi"));
        seriesPublication.publishSeries("s100", seriesJson(), operationsGraph());

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "Enquête Emploi en continu"@fr } }
                """.formatted(OPERATIONS_GRAPH, SERIES));
        seriesPublication.publishSeries("s100", seriesJson(), operationsGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Enquête Emploi en continu");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private SeriesPublication seriesPublication(RepositoryGestion gestion) throws RmesException {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        OperationsParentRepository statuses = mock(OperationsParentRepository.class);
        when(statuses.getFamOpSerValidationStatus("s100")).thenReturn(ValidationStatus.UNPUBLISHED.getValue());
        when(statuses.getValidationStatus("f10")).thenReturn(ValidationStatus.VALIDATED.getValue());
        return new SeriesPublication(
                statuses,
                new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null),
                gestion,
                publication,
                new OperationSeriesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub()));
    }

    private static String seriesLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://rdf.insee.fr/def/base#StatisticalOperationSeries> ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://purl.org/dc/terms/isPartOf> <%s> ;
                    <http://purl.org/dc/terms/hasPart> <%s> .
                  <%4$s> <http://purl.org/dc/terms/hasPart> <%2$s> .
                  <%5$s> <http://rdf.insee.fr/def/base#validationState> "Validated" .
                  <%6$s> <http://purl.org/dc/terms/isReplacedBy> <%2$s> .
                  <%7$s> <http://purl.org/dc/terms/replaces> <%2$s> .
                } }
                """.formatted(OPERATIONS_GRAPH, SERIES, label, FAMILY, OPERATION, PREVIOUS_SERIES, NEXT_SERIES);
    }

    private static JSONObject seriesJson() {
        return new JSONObject().put("id", "s100").put("family", new JSONObject().put("id", "f10"));
    }

    /** Les libellés de la série publiée lus dans le graphe des opérations, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                        }
                        """.formatted(OPERATIONS_GRAPH, PUBLISHED_SERIES))
                .optString("labels");
    }

    private static IRI operationsGraph() {
        return Values.iri(OPERATIONS_GRAPH);
    }
}
