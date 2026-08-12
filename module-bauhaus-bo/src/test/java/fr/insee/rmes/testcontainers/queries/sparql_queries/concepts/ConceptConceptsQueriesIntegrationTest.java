package fr.insee.rmes.testcontainers.queries.sparql_queries.concepts;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Non-régression de {@code ConceptConceptsQueries.checkIfExists} après sa migration vers
 * {@code concepts/checkIfConceptExists.ftlh}.
 *
 * <p>C'est la seule requête migrée de type {@code ASK} : elle est consommée par
 * {@code getResponseAsBoolean}, un chemin d'exécution différent des {@code SELECT}.
 */
@Tag("integration")
class ConceptConceptsQueriesIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final ConceptConceptsQueries queries = new ConceptConceptsQueries(
            new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("ftlh-migration-misc-it.trig");
    }

    @Test
    void checkIfExists_returns_true_when_the_concept_exists() throws RmesException {
        boolean exists = repositoryGestion.getResponseAsBoolean(queries.checkIfExists("cMigExistant"));

        assertThat(exists).isTrue();
    }

    @Test
    void checkIfExists_returns_false_when_the_concept_does_not_exist() throws RmesException {
        boolean exists = repositoryGestion.getResponseAsBoolean(queries.checkIfExists("cMigInconnu"));

        assertThat(exists).isFalse();
    }

    @Test
    void checkIfExists_does_not_match_on_a_prefix_of_an_existing_id() throws RmesException {
        boolean exists = repositoryGestion.getResponseAsBoolean(queries.checkIfExists("cMigExist"));

        assertThat(exists)
                .as("le FILTER STRENDS impose une fin d'IRI exacte, pas un préfixe d'identifiant")
                .isFalse();
    }
}
