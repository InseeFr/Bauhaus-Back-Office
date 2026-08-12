package fr.insee.rmes.testcontainers.queries.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationLevelsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Non-régression des requêtes de niveaux de nomenclature après leur migration vers
 * {@code classifications/getClassificationLevel.ftlh} et
 * {@code getClassificationLevelMembers.ftlh}.
 *
 * <p>{@code getClassificationLevel.ftlh} navigue la liste RDF ({@code rdf:first}/{@code rdf:rest})
 * qui ordonne les niveaux pour en déduire le niveau précédent et le niveau suivant. Ces
 * {@code OPTIONAL} imbriqués sont la partie la plus fragile de la migration : ils ne sont
 * réellement exercés que contre un triplestore.
 */
@Tag("integration")
class ClassificationLevelsQueriesIntegrationTest extends WithGraphDBContainer {

    private static final String CLASSIFICATION_ID = "clsMig";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final ClassificationLevelsQueries queries =
            new ClassificationLevelsQueries(new BauhausLanguagesProperties("fr", "en"));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("classifications-ftlh-migration-it.trig");
    }

    @Test
    void levelQuery_returns_the_level_with_its_notation_and_its_narrower_level() throws RmesException {
        JSONObject level = repositoryGestion.getResponseAsObject(queries.levelQuery(CLASSIFICATION_ID, "lvlMig1"));

        assertThat(level.getString("classificationId"))
                .as("extrait de l'IRI par BIND(STRBEFORE(STRAFTER(...,'/codes/'), '/'))")
                .isEqualTo(CLASSIFICATION_ID);
        assertThat(level.getString("levelId")).isEqualTo("lvlMig1");
        assertThat(level.getString("prefLabelLg1")).isEqualTo("Niveau 1 migration (test)");
        assertThat(level.getString("prefLabelLg2")).isEqualTo("Level 1 migration (test)");
        assertThat(level.getString("depth")).isEqualTo("1");
        assertThat(level.getString("notation")).isEqualTo("N1MIG");
        assertThat(level.getString("notationPattern")).isEqualTo("[0-9]{2}");

        assertThat(level.getString("narrowerLg1"))
                .as("le niveau suivant est atteint via rdf:rest/rdf:first")
                .isEqualTo("Niveau 2 migration (test)");
        assertThat(level.getString("idNarrower")).isEqualTo("lvlMig2");
        assertThat(level.has("broaderLg1"))
                .as("le premier niveau n'a pas de niveau précédent")
                .isFalse();
    }

    @Test
    void levelQuery_returns_the_broader_level_of_the_second_level() throws RmesException {
        JSONObject level = repositoryGestion.getResponseAsObject(queries.levelQuery(CLASSIFICATION_ID, "lvlMig2"));

        assertThat(level.getString("levelId")).isEqualTo("lvlMig2");
        assertThat(level.getString("broaderLg1"))
                .as("le niveau précédent est atteint via ?previousNode rdf:rest ?node")
                .isEqualTo("Niveau 1 migration (test)");
        assertThat(level.getString("broaderLg2")).isEqualTo("Level 1 migration (test)");
        assertThat(level.getString("idBroader")).isEqualTo("lvlMig1");
        assertThat(level.has("narrowerLg1"))
                .as("le dernier niveau n'a pas de niveau suivant")
                .isFalse();
    }

    @Test
    void levelQuery_returns_nothing_for_an_unknown_level() throws RmesException {
        JSONObject level = repositoryGestion.getResponseAsObject(queries.levelQuery(CLASSIFICATION_ID, "lvlInconnu"));

        assertThat(level.isEmpty()).isTrue();
    }

    @Test
    void levelMembersQuery_returns_the_items_of_the_level_ordered_by_id() throws RmesException {
        JSONArray members = repositoryGestion.getResponseAsArray(queries.levelMembersQuery(CLASSIFICATION_ID, "lvlMig1"));

        assertThat(valuesOf(members, "id")).containsExactly("01MIG", "02MIG");
        assertThat(members.getJSONObject(0).getString("labelLg1")).isEqualTo("Poste 1 migration (test)");
        assertThat(members.getJSONObject(0).getString("labelLg2")).isEqualTo("Item 1 migration (test)");
    }

    @Test
    void levelMembersQuery_returns_nothing_for_a_level_without_member() throws RmesException {
        JSONArray members = repositoryGestion.getResponseAsArray(queries.levelMembersQuery(CLASSIFICATION_ID, "lvlMig2"));

        assertThat(members.length()).isZero();
    }

    private static List<String> valuesOf(JSONArray array, String key) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getJSONObject(i).getString(key));
        }
        return values;
    }
}
