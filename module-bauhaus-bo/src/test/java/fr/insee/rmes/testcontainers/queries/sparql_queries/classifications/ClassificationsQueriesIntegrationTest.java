package fr.insee.rmes.testcontainers.queries.sparql_queries.classifications;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@code classificationsUriById} résout les IRIs des nomenclatures liées par
 * xkos:before / xkos:after / xkos:variant. Seule une exécution réelle prouve que la
 * liste d'identifiants filtre bien les nomenclatures : la variante précédente du
 * template produisait une requête syntaxiquement valide mais toujours vide.
 */
@Tag("integration")
class ClassificationsQueriesIntegrationTest extends WithGraphDBContainer {

    private static final String CLASSIFICATION_A = "clsUriByIdA";
    private static final String CLASSIFICATION_B = "clsUriByIdB";
    private static final String CLASSIFICATION_C = "clsUriByIdC";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final ClassificationsQueries queries =
            new ClassificationsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("classifications-uri-by-id-it.trig");
    }

    @Test
    void classificationsUriById_resolves_the_iri_of_a_single_classification() throws RmesException {
        JSONArray result =
                repositoryGestion.getResponseAsArray(queries.classificationsUriById(new String[] {CLASSIFICATION_A}));

        assertThat(result.length()).isOne();
        JSONObject classification = result.getJSONObject(0);
        assertThat(classification.getString("uri")).isEqualTo("http://rdf.insee.fr/codes/clsUriByIdA/");
        assertThat(classification.getString("id")).isEqualTo(CLASSIFICATION_A);
    }

    @Test
    void classificationsUriById_resolves_only_the_requested_classifications() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(
                queries.classificationsUriById(new String[] {CLASSIFICATION_A, CLASSIFICATION_C}));

        assertThat(idsOf(result))
                .as("clsUriByIdB n'est pas demandée et ne doit pas remonter")
                .containsExactlyInAnyOrder(CLASSIFICATION_A, CLASSIFICATION_C);
    }

    @Test
    void classificationsUriById_returns_nothing_for_an_unknown_classification() throws RmesException {
        JSONArray result =
                repositoryGestion.getResponseAsArray(queries.classificationsUriById(new String[] {"clsInconnue"}));

        assertThat(result.length()).isZero();
    }

    @Test
    void classificationsUriById_returns_nothing_when_no_id_is_requested() throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(queries.classificationsUriById(new String[0]));

        assertThat(result.length()).isZero();
    }

    private static List<String> idsOf(JSONArray result) {
        List<String> ids = new ArrayList<>();
        JSONUtils.stream(result).map(row -> row.getString("id")).forEach(ids::add);
        return ids;
    }
}
