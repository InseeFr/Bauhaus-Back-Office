package fr.insee.rmes.testcontainers.queries.sparql_queries.code_list;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.PaginationProperties;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Non-régression de {@code CodeListsQueries.getCodeUriByNotation} après sa migration vers
 * {@code codes-list/getCodeUriByNotation.ftlh}.
 *
 * <p>La requête résout un code par le couple (notation de liste, notation de code) en
 * traversant {@code skos:inScheme} à l'intérieur du graphe des codes : c'est ce parcours,
 * et non le texte de la requête, que ce test protège.
 */
@Tag("integration")
class CodeListsQueriesIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final CodeListsQueries queries = new CodeListsQueries(
            BauhausUriPropertiesStub.stub(),
            new BauhausLanguagesProperties("fr", "en"),
            GraphsPropertiesStub.stub(),
            new PaginationProperties(5));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("ftlh-migration-misc-it.trig");
    }

    @Test
    void getCodeUriByNotation_returns_the_uri_of_the_code_of_the_given_code_list() throws RmesException {
        JSONObject result =
                repositoryGestion.getResponseAsObject(queries.getCodeUriByNotation("CL_MIG_TEST", "CODE_MIG_1"));

        assertThat(result.getString("uri")).isEqualTo("http://codelist/CL_MIG_TEST/CODE_MIG_1");
    }

    @Test
    void getCodeUriByNotation_returns_nothing_when_the_code_belongs_to_another_code_list() throws RmesException {
        JSONObject result =
                repositoryGestion.getResponseAsObject(queries.getCodeUriByNotation("CL_MIG_AUTRE", "CODE_MIG_1"));

        assertThat(result.isEmpty())
                .as("le code n'est résolu que dans la liste passée en paramètre")
                .isTrue();
    }

    @Test
    void getCodeUriByNotation_returns_nothing_for_an_unknown_code() throws RmesException {
        JSONObject result =
                repositoryGestion.getResponseAsObject(queries.getCodeUriByNotation("CL_MIG_TEST", "CODE_MIG_INCONNU"));

        assertThat(result.isEmpty()).isTrue();
    }
}
