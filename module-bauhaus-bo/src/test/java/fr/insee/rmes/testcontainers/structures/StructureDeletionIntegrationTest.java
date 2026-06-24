package fr.insee.rmes.testcontainers.structures;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Filet de sécurité sur la suppression structurée d'une {@code qb:DataStructureDefinition}
 * (chemin critique « suppression » du lot A de la migration GraphDB → Fuseki).
 *
 * <p>{@link RepositoryGestion#clearStructureNodeAndComponents} s'appuie sur des opérations
 * RDF4J « niveau modèle » ({@code getStatements}/{@code remove}) qui ne se traduisent pas
 * 1-pour-1 en SPARQL Protocol : c'est exactement le genre d'appel à valider contre le futur
 * backend. Ce test vérifie le comportement réel contre un vrai GraphDB : les nœuds de
 * composants (et le nœud imbriqué relié par {@code qb:component}) et leurs triplets sortants
 * sont bien supprimés.
 *
 * <p>NB : la suppression efface les triplets <em>sortants</em> des nœuds de composants ; le
 * triplet {@code structure qb:component composant} subsiste (en prod il est écrasé par le
 * {@code loadSimpleObject} qui suit la suppression). On vérifie donc la disparition des nœuds
 * de composants eux-mêmes, pas celle de la référence portée par la structure.
 */
@Tag("integration")
class StructureDeletionIntegrationTest extends WithGraphDBContainer {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    private static final String STRUCTURES_GRAPH = "http://rdf.insee.fr/graphes/structures";
    private static final String STRUCTURE = "http://bauhaus/structures/struct-delete-it";
    private static final String COMPONENT_1 = "http://bauhaus/structures/struct-delete-it/cs1";
    private static final String NESTED_DIMENSION = "http://bauhaus/structures/struct-delete-it/cs1/dim";
    private static final String COMPONENT_2 = "http://bauhaus/structures/struct-delete-it/cs2";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("structure-deletion-it.trig");
    }

    @Test
    void clearStructureNodeAndComponents_removes_components_and_nested_nodes() throws RmesException {
        // Garde-fou : les composants et le nœud imbriqué existent avant la suppression.
        assertThat(componentExists(COMPONENT_1)).as("composant 1 présent avant suppression").isTrue();
        assertThat(componentExists(NESTED_DIMENSION)).as("dimension imbriquée présente avant suppression").isTrue();
        assertThat(componentExists(COMPONENT_2)).as("composant 2 présent avant suppression").isTrue();

        repositoryGestion.clearStructureNodeAndComponents(VF.createIRI(STRUCTURE));

        // Les nœuds de composants et leurs triplets sortants ont disparu (récursion incluse).
        assertThat(componentExists(COMPONENT_1)).as("composant 1 supprimé").isFalse();
        assertThat(componentExists(NESTED_DIMENSION)).as("dimension imbriquée supprimée").isFalse();
        assertThat(componentExists(COMPONENT_2)).as("composant 2 supprimé").isFalse();
    }

    private boolean componentExists(String componentIri) throws RmesException {
        String ask = "ASK { GRAPH <%s> { <%s> ?p ?o } }".formatted(STRUCTURES_GRAPH, componentIri);
        return repositoryGestion.getResponseAsBoolean(ask);
    }
}
