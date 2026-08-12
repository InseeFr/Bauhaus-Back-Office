package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.families;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.operations.families.infrastructure.graphdb.OperationFamilyQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Non-régression de {@code OperationFamilyQueries.getSubjects} après sa migration vers
 * {@code operations/famOpeSer/getSubjects.ftlh}.
 *
 * <p>C'est la seule requête migrée qui utilise une clause {@code FROM} plutôt qu'un
 * {@code GRAPH} : le graphe passé en paramètre devient le graphe par défaut de la requête,
 * ce qui contraint aussi les triplets du sujet à s'y trouver. Ce comportement ne se vérifie
 * que contre un vrai triplestore.
 */
@Tag("integration")
class OperationFamilyQueriesIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final OperationFamilyQueries queries = new OperationFamilyQueries(
            "fr", "en", "http://rdf.insee.fr/graphes/", "operations");

    @BeforeAll
    static void initData() {
        container.withTrigFiles("ftlh-migration-misc-it.trig");
    }

    @Test
    void getSubjects_returns_the_subjects_of_the_family_with_both_labels() throws RmesException {
        JSONArray subjects = repositoryGestion.getResponseAsArray(queries.getSubjects("famOpMig"));

        assertThat(subjects.length()).isEqualTo(1);
        JSONObject subject = subjects.getJSONObject(0);
        assertThat(subject.getString("id")).isEqualTo("SUJMIG");
        assertThat(subject.getString("labelLg1")).isEqualTo("Sujet operation migration (test)");
        assertThat(subject.getString("labelLg2")).isEqualTo("Migration operation subject (test)");
    }

    @Test
    void getSubjects_returns_nothing_for_an_unknown_family() throws RmesException {
        JSONArray subjects = repositoryGestion.getResponseAsArray(queries.getSubjects("famInconnue"));

        assertThat(subjects.length()).isZero();
    }

    @Test
    void getSubjects_returns_nothing_when_the_operations_graph_is_not_the_one_holding_the_family() throws RmesException {
        OperationFamilyQueries otherGraph = new OperationFamilyQueries(
                "fr", "en", "http://rdf.insee.fr/graphes/", "concepts/");

        JSONArray subjects = repositoryGestion.getResponseAsArray(otherGraph.getSubjects("famOpMig"));

        assertThat(subjects.length())
                .as("la clause FROM restreint bien la requête au graphe passé en paramètre")
                .isZero();
    }
}
