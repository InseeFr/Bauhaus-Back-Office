package fr.insee.rmes.testcontainers.operations.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'une opération, rejouée sur chaque triplestore (voir
 * {@link OperationPublicationGraphDBIntegrationTest} et {@link OperationPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : l'opération publiée se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrivent l'opération et
 * le lien {@code dcterms:hasPart} de sa série : sous Fuseki, un triplet relu sans graphe nommé n'en
 * porte aucun, et la publication partait dans le graphe par défaut sans erreur.
 *
 * <p>Les statuts de validation, lus par requêtes SPARQL hors du périmètre de ce test, sont simulés.
 */
interface OperationPublicationScenario {

    String OPERATIONS_GRAPH = "http://rdf.insee.fr/graphes/operations";
    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String OPERATION = GESTION_BASE + "operations/operation/s1000";
    String SERIES = GESTION_BASE + "operations/serie/s100";
    String PUBLISHED_OPERATION = PUBLICATION_BASE + "operations/operation/s1000";
    String PUBLISHED_SERIES = PUBLICATION_BASE + "operations/serie/s100";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheOperationsGraph() throws RmesException {
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION_BASE, GESTION_BASE, name -> Optional.of("operations/operation")));
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(OPERATIONS_GRAPH));
    }

    @Test
    default void publishes_the_operation_and_the_link_from_its_series_into_the_operations_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(operationLabelled("Enquête Emploi 2026"));

        operationPublication(gestion).publishOperation("s1000", operationJson(), operationsGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Enquête Emploi 2026");
        assertThat(gestion.getResponseAsBoolean("""
                        ASK { GRAPH <%s> {
                          <%s> <http://purl.org/dc/terms/isPartOf> <%s> .
                          <%3$s> <http://purl.org/dc/terms/hasPart> <%2$s> .
                        } }
                        """.formatted(OPERATIONS_GRAPH, PUBLISHED_OPERATION, PUBLISHED_SERIES)))
                .isTrue();
    }

    @Test
    default void republishing_the_operation_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        OperationPublication operationPublication = operationPublication(gestion);
        gestion.executeUpdate(operationLabelled("Enquête Emploi 2026"));
        operationPublication.publishOperation("s1000", operationJson(), operationsGraph());

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "Enquête Emploi en continu 2026"@fr } }
                """.formatted(OPERATIONS_GRAPH, OPERATION));
        operationPublication.publishOperation("s1000", operationJson(), operationsGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Enquête Emploi en continu 2026");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private OperationPublication operationPublication(RepositoryGestion gestion) throws RmesException {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        OperationsParentRepository statuses = mock(OperationsParentRepository.class);
        when(statuses.getFamOpSerValidationStatus("s1000")).thenReturn(ValidationStatus.UNPUBLISHED.getValue());
        when(statuses.getValidationStatus("s100")).thenReturn(ValidationStatus.VALIDATED.getValue());
        return new OperationPublication(
                gestion, null, publication, new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null), statuses);
    }

    private static String operationLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://rdf.insee.fr/def/base#StatisticalOperation> ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://purl.org/dc/terms/isPartOf> <%s> .
                  <%4$s> <http://purl.org/dc/terms/hasPart> <%2$s> .
                } }
                """.formatted(OPERATIONS_GRAPH, OPERATION, label, SERIES);
    }

    private static JSONObject operationJson() {
        return new JSONObject().put("id", "s1000").put("series", new JSONObject().put("id", "s100"));
    }

    /** Les libellés de l'opération publiée lus dans le graphe des opérations, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                        }
                        """.formatted(OPERATIONS_GRAPH, PUBLISHED_OPERATION))
                .optString("labels");
    }

    private static IRI operationsGraph() {
        return Values.iri(OPERATIONS_GRAPH);
    }
}
