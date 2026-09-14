package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.families;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
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

/**
 * Non-régression des requêtes de {@code OperationFamilyQueries} qui ne se vérifient que
 * contre un vrai triplestore.
 *
 * <p>{@code getSubjects} est la seule requête migrée qui utilise une clause {@code FROM}
 * plutôt qu'un {@code GRAPH} : le graphe passé en paramètre devient le graphe par défaut de
 * la requête, ce qui contraint aussi les triplets du sujet à s'y trouver.
 *
 * <p>{@code familyQuery} reconstitue la famille en empilant des {@code OPTIONAL} : une
 * variable mal appariée y transforme silencieusement une jointure en produit cartésien.
 */
@Tag("integration")
class OperationFamilyQueriesIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final OperationFamilyQueries queries = new OperationFamilyQueries(
            new BauhausLanguagesProperties("fr", "en"), "http://rdf.insee.fr/graphes/", "operations");

    @BeforeAll
    static void initData() {
        container.withTrigFiles("ftlh-migration-misc-it.trig");
        container.withTrigFiles("a6-appariement-variables-it.trig");
    }

    @Test
    void familyQuery_returns_both_abstracts_when_they_are_carried_by_versioned_notes() throws RmesException {
        JSONObject family = repositoryGestion.getResponseAsObject(queries.familyQuery("famA6"));

        assertThat(family.getString("abstractLg1")).isEqualTo("Résumé A6 (fr)");
        assertThat(family.getString("abstractLg2"))
                .as("le bloc du résumé Lg1 ne doit pas contraindre l'URI de la note Lg2")
                .isEqualTo("Résumé A6 (en)");
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
    void getSubjects_returns_nothing_when_the_operations_graph_is_not_the_one_holding_the_family()
            throws RmesException {
        OperationFamilyQueries otherGraph = new OperationFamilyQueries(
                new BauhausLanguagesProperties("fr", "en"), "http://rdf.insee.fr/graphes/", "concepts/");

        JSONArray subjects = repositoryGestion.getResponseAsArray(otherGraph.getSubjects("famOpMig"));

        assertThat(subjects.length())
                .as("la clause FROM restreint bien la requête au graphe passé en paramètre")
                .isZero();
    }
}
