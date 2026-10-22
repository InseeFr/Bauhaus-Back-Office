package fr.insee.rmes.testcontainers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.util.Values;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Suppression des spécifications de composants d'une structure, rejouée sur chaque triplestore (voir
 * {@link StructureDeletionGraphDBIntegrationTest} et {@link StructureDeletionFusekiIntegrationTest}).
 *
 * <p>Les nœuds reliés à la structure par {@code qb:component}, et ceux qu'ils relient à leur tour,
 * perdent leurs triplets sortants. Le triplet {@code structure qb:component nœud} subsiste : en
 * production, il est écrasé par l'écriture qui suit la suppression.
 */
interface StructureDeletionScenario {

    String STRUCTURES_GRAPH = "http://rdf.insee.fr/graphes/structures";
    String OTHER_GRAPH = "http://rdf.insee.fr/graphes/autre";
    String STRUCTURE = "http://bauhaus/structures/struct-delete-it";
    String COMPONENT_1 = STRUCTURE + "/cs1";
    String NESTED_DIMENSION = STRUCTURE + "/cs1/dim";
    String COMPONENT_2 = STRUCTURE + "/cs2";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /** Le conteneur n'est vidé qu'entre deux classes : chaque test repart de graphes vides. */
    @BeforeEach
    default void insertTheStructure() throws RmesException {
        gestion().executeUpdate("""
                DROP SILENT GRAPH <%1$s> ;
                DROP SILENT GRAPH <%6$s> ;
                INSERT DATA { GRAPH <%1$s> {
                  <%2$s> a <http://purl.org/linked-data/cube#DataStructureDefinition> ;
                    <http://www.w3.org/2000/01/rdf-schema#label> "Structure à supprimer"@fr ;
                    <http://purl.org/linked-data/cube#component> <%3$s>, <%5$s> .
                  <%3$s> a <http://purl.org/linked-data/cube#ComponentSpecification> ;
                    <http://purl.org/linked-data/cube#component> <%4$s> .
                  <%4$s> a <http://purl.org/linked-data/cube#DimensionProperty> .
                  <%5$s> a <http://purl.org/linked-data/cube#ComponentSpecification> .
                } }
                """.formatted(
                        STRUCTURES_GRAPH, STRUCTURE, COMPONENT_1, NESTED_DIMENSION, COMPONENT_2, OTHER_GRAPH));
    }

    @Test
    default void clearStructureNodeAndComponents_removes_components_and_nested_nodes() throws RmesException {
        RepositoryGestion gestion = gestion();

        gestion.clearStructureNodeAndComponents(Values.iri(STRUCTURE), Values.iri(STRUCTURES_GRAPH));

        assertThat(hasTriples(gestion, STRUCTURES_GRAPH, COMPONENT_1))
                .as("composant 1 supprimé")
                .isFalse();
        assertThat(hasTriples(gestion, STRUCTURES_GRAPH, NESTED_DIMENSION))
                .as("dimension imbriquée supprimée")
                .isFalse();
        assertThat(hasTriples(gestion, STRUCTURES_GRAPH, COMPONENT_2))
                .as("composant 2 supprimé")
                .isFalse();
        assertThat(hasTriples(gestion, STRUCTURES_GRAPH, STRUCTURE))
                .as("structure conservée")
                .isTrue();
    }

    @Test
    default void clearStructureNodeAndComponents_keeps_what_a_component_carries_in_another_graph()
            throws RmesException {
        RepositoryGestion gestion = gestion();
        gestion.executeUpdate("""
                INSERT DATA { GRAPH <%s> {
                  <%s> <http://www.w3.org/2000/01/rdf-schema#label> "Ailleurs"@fr .
                } }
                """.formatted(OTHER_GRAPH, COMPONENT_1));

        gestion.clearStructureNodeAndComponents(Values.iri(STRUCTURE), Values.iri(STRUCTURES_GRAPH));

        assertThat(hasTriples(gestion, OTHER_GRAPH, COMPONENT_1)).isTrue();
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private static boolean hasTriples(RepositoryGestion gestion, String graph, String subject) throws RmesException {
        return gestion.getResponseAsBoolean("ASK { GRAPH <%s> { <%s> ?p ?o } }".formatted(graph, subject));
    }
}
