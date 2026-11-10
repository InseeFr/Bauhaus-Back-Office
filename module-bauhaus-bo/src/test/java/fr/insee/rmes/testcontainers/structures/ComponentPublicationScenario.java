package fr.insee.rmes.testcontainers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.structures.persistence.ComponentPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'un composant de structure, rejouée sur chaque triplestore (voir
 * {@link ComponentPublicationGraphDBIntegrationTest} et {@link ComponentPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : le composant publié se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où il arrive : sous Fuseki,
 * un triplet relu sans graphe nommé n'en porte aucun, et la publication partait dans le graphe par
 * défaut sans erreur.
 */
interface ComponentPublicationScenario {

    String COMPONENTS_GRAPH = "http://rdf.insee.fr/graphes/composants";
    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String COMPONENT = GESTION_BASE + "structuresDeDonnees/composants/dimension/d1000";
    String PUBLISHED_COMPONENT = PUBLICATION_BASE + "structuresDeDonnees/composants/dimension/d1000";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheComponentsGraph() throws RmesException {
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(COMPONENTS_GRAPH));
    }

    @Test
    default void publishes_the_component_into_the_components_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(componentLabelled("Âge"));

        componentPublication(gestion).publishComponent(component(), QB.DIMENSION_PROPERTY, componentsGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Âge");
    }

    @Test
    default void republishing_the_component_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        ComponentPublication componentPublication = componentPublication(gestion);
        gestion.executeUpdate(componentLabelled("Âge"));
        componentPublication.publishComponent(component(), QB.DIMENSION_PROPERTY, componentsGraph());

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2000/01/rdf-schema#label> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2000/01/rdf-schema#label> "Âge révolu" } }
                """.formatted(COMPONENTS_GRAPH, COMPONENT));
        componentPublication.publishComponent(component(), QB.DIMENSION_PROPERTY, componentsGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Âge révolu");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private ComponentPublication componentPublication(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        return new ComponentPublication(
                gestion, null, publication, new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null));
    }

    private static String componentLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://purl.org/linked-data/cube#DimensionProperty> ;
                    <http://www.w3.org/2000/01/rdf-schema#label> "%s" ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" .
                } }
                """.formatted(COMPONENTS_GRAPH, COMPONENT, label);
    }

    /** Les libellés du composant publié lus dans le graphe des composants, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2000/01/rdf-schema#label> ?label }
                        }
                        """.formatted(COMPONENTS_GRAPH, PUBLISHED_COMPONENT))
                .optString("labels");
    }

    private static IRI component() {
        return Values.iri(COMPONENT);
    }

    private static IRI componentsGraph() {
        return Values.iri(COMPONENTS_GRAPH);
    }
}
