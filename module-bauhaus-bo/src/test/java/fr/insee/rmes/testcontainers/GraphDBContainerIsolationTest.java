package fr.insee.rmes.testcontainers;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le conteneur GraphDB est un singleton partagé par toutes les classes de tests d'intégration : ce
 * n'est plus son redémarrage qui isole les classes les unes des autres, mais le reset explicite
 * déclenché avant chaque classe. Ce test verrouille ce contrat — sans lui, la première classe qui
 * écrit dans le dépôt polluerait toutes les suivantes.
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GraphDBContainerIsolationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion =
            new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @Test
    @Order(1)
    void should_start_each_test_class_on_an_empty_repository() throws RmesException {
        assertThat(triplesCount()).isZero();
    }

    @Test
    @Order(2)
    void should_wipe_every_named_graph_when_test_data_is_reset() throws RmesException {
        container.withTrigFiles("organizations.trig");
        assertThat(triplesCount()).isPositive();

        container.resetTestData();

        assertThat(triplesCount()).isZero();
    }

    @Test
    @Order(3)
    void should_restore_the_default_fixture_folder_when_test_data_is_reset() throws RmesException {
        container.withInitFolder("fr/insee/rmes/modules/commons");

        container.resetTestData();

        container.withTrigFiles("organizations.trig");
        assertThat(triplesCount()).isPositive();
        container.resetTestData();
    }

    private long triplesCount() throws RmesException {
        return repositoryGestion.getResponseAsObject("SELECT (COUNT(*) AS ?n) WHERE { GRAPH ?g { ?s ?p ?o } }")
                .getLong("n");
    }
}
