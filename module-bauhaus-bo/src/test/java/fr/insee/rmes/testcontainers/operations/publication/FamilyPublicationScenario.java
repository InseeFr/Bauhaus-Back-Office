package fr.insee.rmes.testcontainers.operations.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.PropertiesKeys;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.operations.families.infrastructure.graphdb.GraphDBOperationFamilyRepository;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Publication d'une famille d'opérations, rejouée sur chaque triplestore (voir {@link
 * FamilyPublicationGraphDBIntegrationTest} et {@link FamilyPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : la famille publiée se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrive sa copie publique :
 * sous Fuseki, un triplet relu sans graphe nommé n'en porte aucun, et la famille partait dans le graphe
 * par défaut sans erreur (écart relevé en F11 et F12).
 */
interface FamilyPublicationScenario {

    /** Le graphe des opérations de {@link GraphsPropertiesStub}. */
    String OPERATIONS_GRAPH = "http://rdf.insee.fr/graphes/operations";

    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String FAMILY = GESTION_BASE + "operations/famille/s9301";
    String SERIES = GESTION_BASE + "operations/serie/s9302";
    String PUBLISHED_FAMILY = PUBLICATION_BASE + "operations/famille/s9301";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheOperationsGraph() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        Map<String, String> baseUris = Map.of(PropertiesKeys.OP_FAMILIES_BASE_URI, "operations/famille");
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION_BASE, GESTION_BASE, name -> Optional.ofNullable(baseUris.get(name))));
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(OPERATIONS_GRAPH));
    }

    @Test
    default void publishes_the_family_into_the_operations_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(familyLabelled("Démographie"));

        familyRepository(gestion).publish("s9301");

        assertThat(publishedLabels(gestion)).isEqualTo("Démographie");
    }

    @Test
    default void keeps_the_management_only_triples_out_of_the_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(familyLabelled("Démographie"));

        familyRepository(gestion).publish("s9301");

        assertThat(gestion.getResponseAsBoolean("""
                        ASK { GRAPH ?graph {
                          { <%1$s> <http://rdf.insee.fr/def/base#validationState> ?value }
                          UNION { <%1$s> <http://purl.org/dc/terms/hasPart> ?value }
                        } }
                        """.formatted(PUBLISHED_FAMILY)))
                .isFalse();
    }

    @Test
    default void republishing_the_family_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        GraphDBOperationFamilyRepository familyRepository = familyRepository(gestion);
        gestion.executeUpdate(familyLabelled("Démographie"));
        familyRepository.publish("s9301");

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "Démographie et population"@fr } }
                """.formatted(OPERATIONS_GRAPH, FAMILY));
        familyRepository.publish("s9301");

        assertThat(publishedLabels(gestion)).isEqualTo("Démographie et population");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private GraphDBOperationFamilyRepository familyRepository(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        return new GraphDBOperationFamilyRepository(
                gestion,
                null,
                null,
                publication,
                new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null),
                new BauhausLanguagesProperties("fr", "en"),
                mock(ApplicationEventPublisher.class));
    }

    private static String familyLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://rdf.insee.fr/def/base#StatisticalOperationFamily> ;
                    <http://www.w3.org/2004/02/skos/core#prefLabel> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://purl.org/dc/terms/hasPart> <%s> .
                } }
                """.formatted(OPERATIONS_GRAPH, FAMILY, label, SERIES);
    }

    /** Les libellés de la famille publiée lus dans le graphe des opérations, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> ?label }
                        }
                        """.formatted(OPERATIONS_GRAPH, PUBLISHED_FAMILY))
                .optString("labels");
    }
}
