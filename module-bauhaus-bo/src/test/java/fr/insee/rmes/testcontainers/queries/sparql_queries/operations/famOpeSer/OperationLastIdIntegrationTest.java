package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.famOpeSer;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code OperationQueries.lastId()} alimente le compteur partagé des familles, séries et
 * opérations : {@code FamOpeSerIndUtils.createId()} rend « s » + (dernier identifiant + 1).
 * Un dernier identifiant trop grand fait sauter des numéros, un dernier identifiant trop petit
 * — ou absent — provoque une collision avec un objet existant. Seule une exécution réelle
 * montre ce que la requête tire du graphe.
 */
@Tag("integration")
class OperationLastIdIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final OperationQueries operationQueries = new OperationQueries(
            new BauhausLanguagesProperties("fr", "en"),
            GraphsPropertiesStub.stub("a9-operations", "composants"));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("a9-dernier-identifiant-it.trig");
    }

    @Test
    void lastId_returns_the_greatest_identifier_of_the_operations_graph() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(operationQueries.lastId());

        assertThat(result.getString("id"))
                .as("l'opération s1207 porte le plus grand identifiant du graphe")
                .isEqualTo("1207");
    }

    @Test
    void lastId_ignores_a_resource_that_is_neither_a_family_nor_a_series_nor_an_operation() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(operationQueries.lastId());

        assertThat(result.getString("id"))
                .as("la queue numérique du rapport .../documentation/99999 n'est pas un identifiant")
                .isNotEqualTo("9999");
    }
}
