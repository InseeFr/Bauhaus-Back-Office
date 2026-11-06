package fr.insee.rmes.testcontainers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.structures.persistence.StructurePublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'une structure, rejouée sur chaque triplestore (voir
 * {@link StructurePublicationGraphDBIntegrationTest} et {@link StructurePublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : la structure publiée se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrivent la structure et
 * ses spécifications de composants : sous Fuseki, un triplet relu sans graphe nommé n'en porte aucun,
 * et la publication partait dans le graphe par défaut sans erreur.
 */
interface StructurePublicationScenario {

    String STRUCTURES_GRAPH = "http://rdf.insee.fr/graphes/structures";
    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String STRUCTURE = GESTION_BASE + "structuresDeDonnees/structure/dsd1000";
    String SPECIFICATION = STRUCTURE + "/cs1000";
    String DIMENSION = GESTION_BASE + "structuresDeDonnees/composants/dimension/d1000";
    String PUBLISHED_STRUCTURE = PUBLICATION_BASE + "structuresDeDonnees/structure/dsd1000";
    String PUBLISHED_SPECIFICATION = PUBLISHED_STRUCTURE + "/cs1000";
    String PUBLISHED_DIMENSION = PUBLICATION_BASE + "structuresDeDonnees/composants/dimension/d1000";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un graphe vide. */
    @BeforeEach
    default void dropTheStructuresGraph() throws RmesException {
        gestion().executeUpdate("DROP SILENT GRAPH <%s>".formatted(STRUCTURES_GRAPH));
    }

    @Test
    default void publishes_the_structure_and_its_component_specifications_into_the_structures_graph()
            throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate(structureLabelled("Recensement"));

        structurePublication(gestion).publish(structure(), structuresGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Recensement");
        assertThat(gestion.getResponseAsBoolean("""
                        ASK { GRAPH <%s> {
                          <%s> <http://purl.org/linked-data/cube#component> <%s> .
                          <%3$s> <http://purl.org/linked-data/cube#dimension> <%s> .
                        } }
                        """.formatted(
                                STRUCTURES_GRAPH, PUBLISHED_STRUCTURE, PUBLISHED_SPECIFICATION, PUBLISHED_DIMENSION)))
                .isTrue();
    }

    @Test
    default void republishing_the_structure_replaces_its_previous_publication() throws RmesException {
        RepositoryGestion gestion = gestion();
        StructurePublication structurePublication = structurePublication(gestion);
        gestion.executeUpdate(structureLabelled("Recensement"));
        structurePublication.publish(structure(), structuresGraph());

        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2000/01/rdf-schema#label> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2000/01/rdf-schema#label> "Recensement 2026" } }
                """.formatted(STRUCTURES_GRAPH, STRUCTURE));
        structurePublication.publish(structure(), structuresGraph());

        assertThat(publishedLabels(gestion)).isEqualTo("Recensement 2026");
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private StructurePublication structurePublication(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        return new StructurePublication(
                gestion, null, publication, new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, null, null));
    }

    private static String structureLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://purl.org/linked-data/cube#DataStructureDefinition> ;
                    <http://www.w3.org/2000/01/rdf-schema#label> "%s" ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" ;
                    <http://purl.org/linked-data/cube#component> <%s> .
                  <%4$s> a <http://purl.org/linked-data/cube#ComponentSpecification> ;
                    <http://purl.org/dc/terms/identifier> "cs1000" ;
                    <http://purl.org/linked-data/cube#dimension> <%s> .
                } }
                """.formatted(STRUCTURES_GRAPH, STRUCTURE, label, SPECIFICATION, DIMENSION);
    }

    /** Les libellés de la structure publiée lus dans le graphe des structures, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion) throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2000/01/rdf-schema#label> ?label }
                        }
                        """.formatted(STRUCTURES_GRAPH, PUBLISHED_STRUCTURE))
                .optString("labels");
    }

    private static IRI structure() {
        return Values.iri(STRUCTURE);
    }

    private static IRI structuresGraph() {
        return Values.iri(STRUCTURES_GRAPH);
    }
}
